#!/usr/bin/env node
/**
 * 跨库 API 契约校验：后端实现 ⇄ 前端 docs/api/*.openapi.json
 *
 * 背景：契约机器可读真源在前端库 frontend-scaffold/docs/api/，
 *       后端作为实现方不得复制第二份（禁止平行体系），只做实现对齐校验。
 *
 * 两层校验：
 *   1) 路由层（原）：后端 Controller 的 (method, path) ⇄ 契约 paths。
 *   2) schema 层（新增·契约真 diff）：后端具名 DTO 字段 ⇄ 契约 components.schemas
 *      属性（字段名 + 基础类型族）。捕获「加字段 / 删字段 / 改类型」漂移，
 *      无需引入 springdoc 等后端依赖，复用同一份契约真源即可自动发现。
 *
 * 用法：
 *   node scripts/check-api-contract.mjs
 *   node scripts/check-api-contract.mjs --contracts ../frontend-scaffold/docs/api
 *   node scripts/check-api-contract.mjs --strict        # 有差异时退出码 1（CI / 守门用）
 *   node scripts/check-api-contract.mjs --no-schema     # 仅跑路由层
 *
 * 退出码：默认 0（仅报告）；--strict 且存在差异时为 1。
 */

import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const REPO_ROOT = path.resolve(__dirname, '..');

const HTTP_METHODS = ['get', 'post', 'put', 'delete', 'patch'];

/** 健康检查等运维端点不入户契约，豁免比对 */
const IGNORE_IMPL_PATHS = new Set(['/api/v1/health', '/actuator/health']);

function parseArgs(argv) {
  const args = {
    contracts: path.resolve(REPO_ROOT, '../frontend-scaffold/docs/api'),
    strict: false,
    schema: true,
  };
  for (let i = 2; i < argv.length; i += 1) {
    if (argv[i] === '--contracts') args.contracts = path.resolve(argv[++i]);
    else if (argv[i] === '--strict') args.strict = true;
    else if (argv[i] === '--no-schema') args.schema = false;
    else if (argv[i] === '--help' || argv[i] === '-h') {
      console.log('用法: node scripts/check-api-contract.mjs [--contracts <dir>] [--strict] [--no-schema]');
      process.exit(0);
    }
  }
  return args;
}

function walk(dir, out = []) {
  if (!fs.existsSync(dir)) return out;
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) walk(full, out);
    else if (entry.name.endsWith('.java')) out.push(full);
  }
  return out;
}

function normPath(p) {
  return `/${String(p || '').replace(/^\/+/, '').replace(/\/+/g, '/').replace(/\/$/, '')}`;
}

function joinPath(base, sub) {
  return normPath(`${normPath(base)}/${normPath(sub)}`);
}

/** 取注解括号内的第一个字符串字面量 */
function firstStringLiteral(attrText) {
  const m = /"([^"]*)"/.exec(attrText || '');
  return m ? m[1] : '';
}

/* ============================ 路由层（原逻辑） ============================ */

/** 扫描后端 Controller，返回 [{ method, path, file }] */
function scanImplementation() {
  const root = path.join(REPO_ROOT, 'src/main/java');
  const files = walk(root).filter(
    (f) => /(^|[\\/])controller[\\/][^\\/]+\.java$/i.test(f) || /Controller\.java$/i.test(f),
  );

  const endpoints = [];
  for (const file of files) {
    const text = fs.readFileSync(file, 'utf8');
    const classIdx = text.search(/\bclass\s+\w+/);
    const header = classIdx >= 0 ? text.slice(0, classIdx) : '';
    const body = classIdx >= 0 ? text.slice(classIdx) : text;

    const classAnn = /@RequestMapping\s*\(([^)]*)\)/.exec(header);
    const basePath = classAnn ? firstStringLiteral(classAnn[1]) : '';

    const withParens = /@(Get|Post|Put|Delete|Patch|Request)Mapping\s*\(([^)]*)\)/g;
    for (let m = withParens.exec(body); m !== null; m = withParens.exec(body)) {
      const method = m[1] === 'Request' ? 'GET' : m[1].toUpperCase();
      endpoints.push({ method, path: joinPath(basePath, firstStringLiteral(m[2])), file });
    }

    const noParens = /@(Get|Post|Put|Delete|Patch)Mapping\s*(?!\()/g;
    for (let m = noParens.exec(body); m !== null; m = noParens.exec(body)) {
      endpoints.push({ method: m[1].toUpperCase(), path: joinPath(basePath, ''), file });
    }
  }

  for (const file of walk(path.join(REPO_ROOT, 'src/main/java'))) {
    if (!/WebSocketConfig\.java$/i.test(file)) continue;
    const text = fs.readFileSync(file, 'utf8');
    const re = /addHandler\s*\([^,]+,\s*"([^"]+)"/g;
    for (let m = re.exec(text); m !== null; m = re.exec(text)) {
      endpoints.push({ method: 'WS', path: normPath(m[1]), file });
    }
  }

  return endpoints;
}

/** 扫描前端 OpenAPI 契约，返回 [{ method, path, file, external, alias }] */
function scanContracts(dir) {
  if (!fs.existsSync(dir)) return { endpoints: [], missing: true };
  const files = fs
    .readdirSync(dir)
    .filter((f) => f.endsWith('.openapi.json') && !f.startsWith('_'))
    .map((f) => path.join(dir, f));

  const endpoints = [];
  for (const file of files) {
    let doc;
    try {
      doc = JSON.parse(fs.readFileSync(file, 'utf8'));
    } catch (err) {
      console.warn(`[warn] 解析失败，已跳过 ${path.basename(file)}: ${err.message}`);
      continue;
    }
    const serverUrl = Array.isArray(doc.servers) && doc.servers[0] ? String(doc.servers[0].url || '') : '';
    const isWs = serverUrl.startsWith('/ws') || serverUrl.startsWith('ws');
    const isExternal = /^https?:\/\//i.test(serverUrl);
    for (const [p, item] of Object.entries(doc.paths || {})) {
      const raw = normPath(p);
      if (isWs) {
        endpoints.push({ method: 'WS', path: raw, file });
        continue;
      }
      for (const method of HTTP_METHODS) {
        if (!item || !item[method]) continue;
        const full = isExternal ? `${serverUrl.replace(/\/+$/, '')}${raw}` : joinPath(serverUrl, raw);
        endpoints.push({ method: method.toUpperCase(), path: full, file, external: isExternal });
        if (!isExternal) {
          endpoints.push({ method: method.toUpperCase(), path: raw, file, alias: true });
        }
      }
    }
  }
  return { endpoints, missing: false };
}

/* ============================ schema 层（契约真 diff） ============================ */

/** Java 基础类型 → OpenAPI 类型族 */
function javaTypeFamily(raw) {
  const t = String(raw || '')
    .replace(/java\.lang\./g, '')
    .replace(/java\.util\./g, '')
    .replace(/java\.time\./g, '')
    .replace(/java\.math\./g, '')
    .trim();
  if (/^(List|Set|Collection|ArrayList|LinkedList)</.test(t) || /\b\w+\[\]$/.test(t)) return 'array';
  if (/^(String|Character|char|UUID|LocalDateTime|LocalDate|LocalTime|Date|Instant|ZonedDateTime|OffsetDateTime|YearMonth)$/.test(t))
    return 'string';
  if (/^(Integer|int|Long|long|Short|short|Byte|byte|BigInteger)$/.test(t)) return 'integer';
  if (/^(Boolean|boolean)$/.test(t)) return 'boolean';
  if (/^(Double|double|Float|float|BigDecimal|Number|BigInteger)$/.test(t)) return 'number';
  return 'object';
}

/** OpenAPI 属性 type → 类型族（array/object/$ref 归一） */
function oasFamily(t) {
  if (t === 'string' || t === 'integer' || t === 'number' || t === 'boolean' || t === 'array' || t === 'object')
    return t;
  return t ? 'object' : null; // $ref 或未知 → 视作 object，不比对族
}

const FIELD_RE =
  /\b(private|protected)\s+(?:static\s+|final\s+|transient\s+)*((?:@\w+(?:\([^)]*\))?\s*)*)([^;=]+?)\s+([A-Za-z_]\w*)\s*(?:=[^;]*)?;/g;

/** 扫描后端 dto/entity 下的具名 POJO，返回 Map<ClassName, { fields:[{name,type,required}], file }> */
function scanBackendDtos() {
  const root = path.join(REPO_ROOT, 'src/main/java');
  const files = walk(root).filter((f) => /[\\/](dto|entity|domain)[\\/]/i.test(f) && /\.java$/.test(f));

  const dtos = new Map();
  for (const file of files) {
    const text = fs.readFileSync(file, 'utf8');
    const cls = /(?:public\s+final\s+|public\s+)(?:class|record)\s+(\w+)/.exec(text);
    if (!cls) continue;
    const name = cls[1];
    const fields = [];
    FIELD_RE.lastIndex = 0;
    for (let m = FIELD_RE.exec(text); m !== null; m = FIELD_RE.exec(text)) {
      const ann = m[2] || '';
      const typeRaw = m[3].replace(/\s+/g, ' ').trim();
      const fieldName = m[4];
      if (fieldName === 'serialVersionUID') continue;
      if (/^(logger|log|LOG)$/i.test(fieldName)) continue;
      const required = /@(NotNull|NotBlank|NotEmpty)/.test(ann);
      fields.push({ name: fieldName, type: typeRaw, required });
    }
    if (fields.length) dtos.set(name, { fields, file });
  }
  return dtos;
}

/**
 * 合并各 openapi.json 的 components.schemas。
 * 返回 Map<SchemaName, { properties, required, file, external }>。
 */
function loadContractSchemas(dir) {
  if (!fs.existsSync(dir)) return new Map();
  const files = fs
    .readdirSync(dir)
    .filter((f) => f.endsWith('.openapi.json') && !f.startsWith('_'))
    .map((f) => path.join(dir, f));

  const schemas = new Map();
  for (const file of files) {
    let doc;
    try {
      doc = JSON.parse(fs.readFileSync(file, 'utf8'));
    } catch (err) {
      console.warn(`[warn] 解析失败，已跳过 ${path.basename(file)}: ${err.message}`);
      continue;
    }
    const serverUrl = Array.isArray(doc.servers) && doc.servers[0] ? String(doc.servers[0].url || '') : '';
    const external = /^https?:\/\//i.test(serverUrl);
    const sc = (doc.components && doc.components.schemas) || {};
    for (const [sname, def] of Object.entries(sc)) {
      schemas.set(sname, {
        properties: (def && def.properties) || {},
        required: (def && def.required) || [],
        file: path.basename(file),
        external,
      });
    }
  }
  return schemas;
}

/** 比对单个同名 DTO：返回 { onlyContract, onlyBackend, typeMismatch } */
function diffSchema(contractDef, backendDto) {
  const cProps = contractDef.properties || {};
  const bByName = new Map(backendDto.fields.map((f) => [f.name, f]));
  const onlyContract = [];
  const typeMismatch = [];
  for (const [pname, cp] of Object.entries(cProps)) {
    const bf = bByName.get(pname);
    if (!bf) {
      onlyContract.push(pname);
      continue;
    }
    const cf = oasFamily(cp && cp.type);
    const bfam = javaTypeFamily(bf.type);
    if (cf && bfam && cf !== bfam) typeMismatch.push({ name: pname, contract: cf, backend: bfam });
  }
  const onlyBackend = backendDto.fields.map((f) => f.name).filter((n) => !cProps[n]);
  return { onlyContract, onlyBackend, typeMismatch };
}

/* ============================ 主流程 ============================ */

function main() {
  const args = parseArgs(process.argv);
  const impl = scanImplementation();
  const { endpoints: contract, missing } = scanContracts(args.contracts);

  if (missing) {
    console.error(`[error] 契约目录不存在：${args.contracts}`);
    console.error('        用 --contracts <dir> 指定 frontend-scaffold/docs/api 的位置。');
    process.exit(args.strict ? 1 : 0);
  }

  /* ---- 路由层 ---- */
  const comparable = impl.filter((e) => !IGNORE_IMPL_PATHS.has(e.path));
  const implKeys = new Set(comparable.map((e) => `${e.method} ${e.path}`));
  const contractPrimary = contract.filter((e) => !e.alias && !e.external);
  const contractExternal = contract.filter((e) => !e.alias && e.external);
  const contractKeys = new Set(contract.map((e) => `${e.method} ${e.path}`));

  const missingImpl = contractPrimary.filter((e) => !implKeys.has(`${e.method} ${e.path}`));
  const missingContract = comparable.filter((e) => !contractKeys.has(`${e.method} ${e.path}`));
  const matched = comparable.filter((e) => contractKeys.has(`${e.method} ${e.path}`));

  const rel = (f) => path.relative(REPO_ROOT, f).replace(/\\/g, '/');

  console.log('=== 后端实现端点 (%d) ===', impl.length);
  for (const e of impl.sort((a, b) => a.path.localeCompare(b.path))) {
    console.log('  %s %s   [%s]', e.method.padEnd(6), e.path, rel(e.file));
  }

  console.log('\n=== 契约端点 (%d, 来自 %s) ===', contractPrimary.length, rel(args.contracts));
  for (const e of contractPrimary.sort((a, b) => a.path.localeCompare(b.path))) {
    console.log('  %s %s   [%s]', e.method.padEnd(6), e.path, path.basename(e.file));
  }

  console.log('\n=== 契约有 / 实现无 (%d) ===', missingImpl.length);
  if (!missingImpl.length) console.log('  （无）');
  for (const e of missingImpl.sort((a, b) => a.path.localeCompare(b.path))) {
    console.log('  %s %s   [%s]', e.method.padEnd(6), e.path, path.basename(e.file));
  }

  console.log('\n=== 外部网关契约，不由本服务实现 (%d) ===', contractExternal.length);
  if (!contractExternal.length) console.log('  （无）');
  for (const e of contractExternal.sort((a, b) => a.path.localeCompare(b.path))) {
    console.log('  %s %s   [%s]', e.method.padEnd(6), e.path, path.basename(e.file));
  }

  console.log('\n=== 实现有 / 契约无 (%d) ===', missingContract.length);
  if (!missingContract.length) console.log('  （无）');
  for (const e of missingContract.sort((a, b) => a.path.localeCompare(b.path))) {
    console.log('  %s %s   [%s]', e.method.padEnd(6), e.path, rel(e.file));
  }

  console.log('\n=== 已对齐 (%d) ===', matched.length);
  for (const e of matched.sort((a, b) => a.path.localeCompare(b.path))) {
    console.log('  %s %s', e.method.padEnd(6), e.path);
  }

  let routeDiff = missingImpl.length + missingContract.length;
  console.log(
    '\n路由层摘要：实现 %d / 契约 %d / 已对齐 %d / 差异 %d%s',
    impl.length,
    contractPrimary.length,
    matched.length,
    routeDiff,
    args.strict ? '（strict 模式）' : '',
  );

  /* ---- schema 层（契约真 diff） ---- */
  let schemaDrift = 0;
  if (args.schema) {
    const schemas = loadContractSchemas(args.contracts);
    const dtos = scanBackendDtos();

    const comparableNames = [];
    const exemptEnums = [];
    const exemptExternal = [];
    const exemptWrapper = [];

    for (const [sname, def] of schemas) {
      if (def.external) {
        exemptExternal.push(sname);
        continue;
      }
      const propCount = Object.keys(def.properties || {}).length;
      if (propCount === 0) {
        exemptEnums.push(sname); // 枚举 / 纯 $ref 别名 / 组合 schema，无属性可比
        continue;
      }
      if (!dtos.has(sname)) {
        exemptWrapper.push(sname); // 后端无同名类（内部包装类 / record 异名）
        continue;
      }
      comparableNames.push(sname);
    }

    console.log('\n=== schema 字段级对拍（契约真 diff）===');
    console.log(
      '可比具名 DTO %d；豁免：枚举/别名 %d、外部 gis %d、后端内部包装类 %d',
      comparableNames.length,
      exemptEnums.length,
      exemptExternal.length,
      exemptWrapper.length,
    );

    for (const sname of comparableNames.sort()) {
      const def = schemas.get(sname);
      const dto = dtos.get(sname);
      const d = diffSchema(def, dto);
      const n = d.onlyContract.length + d.onlyBackend.length + d.typeMismatch.length;
      if (n === 0) {
        console.log('  ✓ %s  对齐（%d 字段）', sname.padEnd(22), dto.fields.length);
      } else {
        schemaDrift += n;
        const parts = [];
        if (d.onlyContract.length) parts.push(`契约有后端无[${d.onlyContract.join(',')}]`);
        if (d.onlyBackend.length) parts.push(`后端有契约无[${d.onlyBackend.join(',')}]`);
        if (d.typeMismatch.length)
          parts.push(
            `类型不一致[${d.typeMismatch.map((t) => `${t.name}:${t.contract}≠${t.backend}`).join(', ')}]`,
          );
        console.log('  ✗ %s  %s', sname.padEnd(22), parts.join('; '));
      }
    }

    if (exemptEnums.length) console.log('\n  豁免·枚举/别名（无属性，跳过）：%s', exemptEnums.join(', '));
    if (exemptExternal.length) console.log('  豁免·外部 gis（server 绝对 URL，跳过）：%s', exemptExternal.join(', '));
    if (exemptWrapper.length) console.log('  豁免·后端内部包装类（契约无同名，跳过）：%s', exemptWrapper.join(', '));

    console.log('\nschema 层摘要：可比 %d / 漂移 %d', comparableNames.length, schemaDrift);
  }

  const totalDiff = routeDiff + schemaDrift;
  console.log(
    '\n总摘要：路由差异 %d / schema 漂移 %d / 合计 %d%s',
    routeDiff,
    schemaDrift,
    totalDiff,
    args.strict ? '（strict 模式，任一>0 即退出 1）' : '',
  );

  if (totalDiff > 0) {
    console.log('提示：差异属技术债，需在 openspec Change 中消化；改接口时按 AGENTS.md §11 走跨库四同步（含跑本脚本）。');
  }

  process.exit(args.strict && totalDiff > 0 ? 1 : 0);
}

main();

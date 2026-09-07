#!/usr/bin/env node
/**
 * 跨库 API 契约校验：后端 Controller 端点 ⇄ 前端 docs/api/*.openapi.json
 *
 * 背景：契约机器可读真源在前端库 frontend-scaffold/docs/api/，
 *       后端作为实现方不得复制第二份（禁止平行体系），只做实现对齐校验。
 *
 * 用法：
 *   node scripts/check-api-contract.mjs
 *   node scripts/check-api-contract.mjs --contracts ../frontend-scaffold/docs/api
 *   node scripts/check-api-contract.mjs --strict        # 有差异时退出码 1（CI / 守门用）
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
  const args = { contracts: path.resolve(REPO_ROOT, '../frontend-scaffold/docs/api'), strict: false };
  for (let i = 2; i < argv.length; i += 1) {
    if (argv[i] === '--contracts') args.contracts = path.resolve(argv[++i]);
    else if (argv[i] === '--strict') args.strict = true;
    else if (argv[i] === '--help' || argv[i] === '-h') {
      console.log('用法: node scripts/check-api-contract.mjs [--contracts <dir>] [--strict]');
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

    // 带括号的 @XxxMapping("/foo")
    const withParens = /@(Get|Post|Put|Delete|Patch|Request)Mapping\s*\(([^)]*)\)/g;
    for (let m = withParens.exec(body); m !== null; m = withParens.exec(body)) {
      const method = m[1] === 'Request' ? 'GET' : m[1].toUpperCase();
      endpoints.push({ method, path: joinPath(basePath, firstStringLiteral(m[2])), file });
    }

    // 无括号的 @GetMapping
    const noParens = /@(Get|Post|Put|Delete|Patch)Mapping\s*(?!\()/g;
    for (let m = noParens.exec(body); m !== null; m = noParens.exec(body)) {
      endpoints.push({ method: m[1].toUpperCase(), path: joinPath(basePath, ''), file });
    }
  }

  // WebSocket 端点（config/WebSocketConfig.java 的 registry.addHandler(handler, "/ws/xxx")）
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

/** 扫描前端 OpenAPI 契约，返回 [{ method, path, file }] */
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
    // server 为绝对 URL（如 https://gateway.example.com/gis）→ 外部网关，不由本服务实现
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

function main() {
  const args = parseArgs(process.argv);
  const impl = scanImplementation();
  const { endpoints: contract, missing } = scanContracts(args.contracts);

  if (missing) {
    console.error(`[error] 契约目录不存在：${args.contracts}`);
    console.error('        用 --contracts <dir> 指定 frontend-scaffold/docs/api 的位置。');
    process.exit(args.strict ? 1 : 0);
  }

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

  const diff = missingImpl.length + missingContract.length;
  console.log(
    '\n摘要：实现 %d / 契约 %d / 已对齐 %d / 差异 %d%s',
    impl.length,
    contractPrimary.length,
    matched.length,
    diff,
    args.strict ? '（strict 模式）' : '',
  );

  if (diff > 0) {
    console.log('提示：差异属技术债，需在 openspec Change 中消化；改接口时按 AGENTS.md §11 走跨库四同步。');
  }

  process.exit(args.strict && diff > 0 ? 1 : 0);
}

main();

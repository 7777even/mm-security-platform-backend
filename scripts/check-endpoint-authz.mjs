#!/usr/bin/env node
/**
 * 端点授权覆盖守门：写端点必须声明「角色或权限码」约束。
 *
 * 规则（对应 AGENTS.md §6.3 安全红线 / §8 L4 硬门禁）：
 *   Controller 中的**写端点**（`@PostMapping` / `@PutMapping` / `@DeleteMapping` / `@PatchMapping`）
 *   必须能满足其一：
 *     1) 方法级 `@RequireAuth` 带 `role=` 或 `perm=`；或
 *     2) 类级 `@RequireAuth` 带 `role=` 或 `perm=`；或
 *     3) 在下方 ALLOWLIST 中**显式豁免并写明理由**。
 *
 *   为什么不能只靠「登录即可」：全局 `JwtFilter` 覆盖所有 Controller，
 *   裸 `@RequireAuth`（或类级裸注解）只保证「已登录」，任何账号都能调用。
 *   写端点缺 role/perm 即等于对所有登录用户开放——对安全管控系统是越权面。
 *
 * 用法：
 *   node scripts/check-endpoint-authz.mjs          # 守门（有违规即退出 1）
 *   node scripts/check-endpoint-authz.mjs --report # 只打印分类，不判失败
 *
 * 退出码：违规时 1，否则 0。
 */

import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const REPO_ROOT = path.resolve(__dirname, '..');
const CONTROLLER_DIR = path.join(REPO_ROOT, 'src/main/java/com/sinopec/mmsecurity/controller');

const WRITE_MAPPINGS = ['PostMapping', 'PutMapping', 'DeleteMapping', 'PatchMapping'];

/**
 * 显式豁免名单。**每条必须写理由**，否则门禁形同虚设。
 * 键为 `<ControllerSimpleName>#<methodName>`（方法名解析失败时用 `<ControllerSimpleName>#@<Verb>@<行号>`）。
 */
const ALLOWLIST = new Map([
  // —— 认证自助与令牌交换（不能用 role/perm 卡，卡了就登不进来 / 改不了自己的密码）——
  ['Auth#login', 'JwtFilter 白名单：未登录也得能调'],
  ['Auth#refresh', 'JwtFilter 白名单：用 rt Cookie 换新令牌'],
  ['Auth#logout', 'JwtFilter 白名单：幂等清除 rt Cookie'],
  ['Auth#changePassword', '本人改密，AuthorizationService.assertSelfOrAdmin 兜底；要求额外权限会锁死改密'],
  ['Auth#updateProfile', '本人改资料，assertSelfOrAdmin 兜底'],

  // —— 入站上报（客户端/设备侧上报自己的数据，语义上不能要求管理权限）——
  ['Uplink#submitFieldReport',
    '防爆手机现场采集回传，代码注释明确「仍受 @RequireAuth 鉴权（401）」为设计口径；加管理权限会挡死一线终端'],
  ['Uplink#reportAudit',
    '⚠️待安全确认：前端上报自身操作审计（POST /audit/log）。现状登录即可提交，'
    + '意味着任意登录账号可伪造审计记录。若产品/安全认为需要约束，应改为「仅允许上报与自己相关的动作」'
    + '或在 Service 侧校验，而不是简单加 role=ADMIN（那会挡死普通用户的审计上报）。'],

  // —— 大屏自助操作（值守/指挥人员非 ADMIN，卡管理权限会挡死一线使用）——
  ['EmergencyEvent#create',
    '大屏应急指挥页「新增事件/演练/极端天气」（POST /emergency-events）：由登录态的值守/指挥人员自助创建，'
    + '按变更决策取「仅登录态」（非 ADMIN）；Service 同事务写 fac_emergency_event + fac_accident_incident。'
    + '若后续要收紧，应引入 emergency:event:write 之类 perm 码，而非简单 role=ADMIN。'],
]);

/** 去掉块注释与行注释，避免把注释里的注解当成真注解 */
function stripComments(src) {
  return src.replace(/\/\*[\s\S]*?\*\//g, '').replace(/\/\/[^\n]*/g, '');
}

/** 取文件里所有「注解块 + 方法声明」 */
function parseMethods(src) {
  const out = [];
  // 注解块：连续若干「独占一行的 @Annotation(...)」；随后是方法声明
  const re = /((?:^[ \t]*@[A-Za-z][\w.]*(?:\([^)]*\))?[ \t]*\r?\n)+)[ \t]*(?:public|protected|private)\s+[\w<>\[\],.\s?]*?([A-Za-z_]\w*)\s*\(/gm;
  let m;
  while ((m = re.exec(src)) !== null) {
    out.push({
      annotations: m[1],
      name: m[2],
      line: src.slice(0, m.index).split('\n').length,
    });
  }
  return out;
}

function hasAuthWithConstraint(annotations) {
  // 单个 @RequireAuth(...) 内是否含 role= 或 perm=
  const re = /@RequireAuth\s*(\([^)]*\))?/g;
  let m;
  while ((m = re.exec(annotations)) !== null) {
    const args = m[1] || '';
    if (/\brole\s*=/.test(args) || /\bperm\s*=/.test(args)) return true;
  }
  return false;
}

function writeVerbOf(annotations) {
  for (const v of WRITE_MAPPINGS) {
    if (new RegExp('@' + v + '\\b').test(annotations)) return v.replace('Mapping', '');
  }
  return null;
}

function main() {
  const reportOnly = process.argv.includes('--report');

  if (!fs.existsSync(CONTROLLER_DIR)) {
    console.error(`[endpoint-authz] 找不到 Controller 目录：${CONTROLLER_DIR}`);
    process.exit(1);
  }

  const files = fs.readdirSync(CONTROLLER_DIR).filter((f) => f.endsWith('Controller.java')).sort();
  const violations = [];
  const rows = [];
  const seen = new Set();
  let writeCount = 0;
  let allowCount = 0;

  for (const file of files) {
    const simple = file.replace('Controller.java', '');
    const raw = fs.readFileSync(path.join(CONTROLLER_DIR, file), 'utf8');
    const src = stripComments(raw);

    // 类级注解：到 `class ` 之前
    const classIdx = src.indexOf('class ');
    const classAnnotations = classIdx > 0 ? src.slice(0, classIdx) : '';
    const classProtected = hasAuthWithConstraint(classAnnotations);

    for (const meth of parseMethods(src)) {
      const verb = writeVerbOf(meth.annotations);
      if (!verb) continue;
      writeCount++;

      const methodProtected = hasAuthWithConstraint(meth.annotations);
      const ok = methodProtected || classProtected;
      const key = `${simple}#${meth.name}`;
      const allowed = ALLOWLIST.has(key);
      seen.add(key);

      rows.push({
        key,
        verb,
        scope: methodProtected ? 'method' : classProtected ? 'class' : 'none',
        allowed,
        line: meth.line,
      });

      if (!ok && !allowed) {
        violations.push(
          `${file}:${meth.line}  ${key}  ${verb} —— 写端点无 role/perm 约束（仅登录即可调用）。` +
            `\n      修法：加 @RequireAuth(role="ADMIN") 或 @RequireAuth(perm="<域>:<对象>:<动作>")；` +
            `\n      确属自助/白名单端点，请在本脚本 ALLOWLIST 显式登记并写明理由。`
        );
      }
      if (allowed && !ok) allowCount++;
    }
  }

  // 豁免名单腐烂检查：名单里的键必须仍然对应一个**写端点**，
  // 否则说明端点已改名/下线/改成只读，而豁免仍挂在名单上——门禁会静默漏检。
  const stale = [...ALLOWLIST.keys()].filter((k) => !seen.has(k));

  if (reportOnly) {
    console.log(`${'Controller#method'.padEnd(38)} ${'verb'.padEnd(7)} ${'约束来源'.padEnd(8)} 豁免`);
    for (const r of rows) {
      console.log(`${r.key.padEnd(38)} ${r.verb.padEnd(7)} ${r.scope.padEnd(8)} ${r.allowed ? '是' : ''}`);
    }
    if (ALLOWLIST.size > 0) {
      console.log('\n豁免名单及理由：');
      for (const [k, why] of ALLOWLIST) console.log(`  - ${k}：${why}`);
    }
    console.log('');
  }

  if (stale.length > 0) {
    console.error(`[endpoint-authz] 豁免名单已失效 ${stale.length} 条（对应写端点不存在或已改名）：`);
    for (const k of stale) console.error(`  - ${k}`);
    console.error('  请删除该条目，或修正键名——挂着无效豁免会让门禁静默漏检。');
    process.exit(1);
  }

  if (violations.length > 0) {
    console.error(`[endpoint-authz] 发现 ${violations.length} 处违规（写端点缺角色/权限约束）：`);
    for (const v of violations) console.error('  - ' + v);
    console.error('');
    console.error('  说明：全局 JwtFilter 只保证「已登录」。写端点缺 role/perm 等于对全部登录用户开放。');
    process.exit(1);
  }

  console.log(
    `[endpoint-authz] OK：写端点 ${writeCount} 个全部具备角色/权限约束` +
      `（其中显式豁免并写明理由 ${allowCount} 个）。`
  );
}

main();

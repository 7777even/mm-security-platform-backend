#!/usr/bin/env node
/**
 * OpenSpec 卫生检查：openspec/changes/ 与 archive/ 的闭环纪律
 *
 * 规则（对应根 AGENTS.md §7.1「全勾必归档」）：
 *   1) openspec/changes/ 下不允许存在 tasks.md 全部勾选（无未勾项）的 Change
 *      ——已完结的 Change 必须完成 spec 回填并移入 openspec/archive/。【硬失败】
 *   2) openspec/changes/ 下的每个 Change 应四件套齐全
 *      （proposal.md / design.md / tasks.md / spec-delta.md）。【告警】
 *      存量 Change（早于四件套纪律）允许只有 proposal+tasks；新提案必须齐全。
 *   3) openspec/archive/ 下的目录必须带日期前缀（YYYY-MM-DD-<name>）。【硬失败】
 *   4) openspec/changes/ 下的目录建议也带日期前缀（YYYY-MM-DD-<name>），与归档命名一致。【告警】
 *   5) openspec/changes/ 下的每个目录建议含 .openspec.yaml（schema: spec-driven + created: <YYYY-MM-DD>）。【告警】
 *
 * 用法：
 *   node scripts/check-openspec-hygiene.mjs
 *
 * 退出码：存在违规时为 1（CI / 守门用），否则 0。
 */

import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const REPO_ROOT = path.resolve(__dirname, '..');
const OPENSPEC = path.join(REPO_ROOT, 'openspec');
const CHANGES = path.join(OPENSPEC, 'changes');
const ARCHIVE = path.join(OPENSPEC, 'archive');

const REQUIRED_FILES = ['proposal.md', 'design.md', 'tasks.md', 'spec-delta.md'];
const DATE_PREFIX = /^\d{4}-\d{2}-\d{2}-/;

const violations = [];
const warnings = [];

function listDirs(dir) {
  if (!fs.existsSync(dir)) return [];
  return fs
    .readdirSync(dir, { withFileTypes: true })
    .filter((e) => e.isDirectory() && !e.name.startsWith('.'))
    .map((e) => e.name);
}

// 1) 全勾未归档【硬失败】 + 2) 四件套齐全【告警，存量豁免】
for (const name of listDirs(CHANGES)) {
  const dir = path.join(CHANGES, name);
  for (const f of REQUIRED_FILES) {
    if (!fs.existsSync(path.join(dir, f))) {
      warnings.push(`changes/${name}: 缺少四件套文件 ${f}（新提案必须齐全，存量 Change 建议补齐）`);
    }
  }
  const tasksPath = path.join(dir, 'tasks.md');
  if (fs.existsSync(tasksPath)) {
    const text = fs.readFileSync(tasksPath, 'utf8');
    const done = (text.match(/^\s*- \[x\]/gm) || []).length;
    const todo = (text.match(/^\s*- \[ \]/gm) || []).length;
    if (done > 0 && todo === 0) {
      violations.push(
        `changes/${name}: tasks.md 已全部勾选（${done} 项）但仍未归档 —— 请先回填 openspec/specs/ 再 git mv 到 openspec/archive/<YYYY-MM-DD>-${name}`
      );
    }
  }
}

// 3) 归档目录日期前缀
for (const name of listDirs(ARCHIVE)) {
  if (!DATE_PREFIX.test(name)) {
    violations.push(`archive/${name}: 目录名缺少日期前缀（应为 YYYY-MM-DD-<name>）`);
  }
}

// 4) changes/ 目录建议带日期前缀【告警】 + 5) 建议含 .openspec.yaml【告警】
for (const name of listDirs(CHANGES)) {
  if (!DATE_PREFIX.test(name)) {
    warnings.push(`changes/${name}: 目录名建议带日期前缀（YYYY-MM-DD-<name>，与归档命名一致）`);
  }
  const yamlPath = path.join(CHANGES, name, '.openspec.yaml');
  if (!fs.existsSync(yamlPath)) {
    warnings.push(`changes/${name}: 建议含 .openspec.yaml（schema: spec-driven + created: <YYYY-MM-DD>）`);
  }
}

if (warnings.length > 0) {
  console.warn(`[openspec-hygiene] ${warnings.length} 处告警（不阻塞）：`);
  for (const w of warnings) console.warn(`  - ${w}`);
}

if (violations.length > 0) {
  console.error(`[openspec-hygiene] 发现 ${violations.length} 处违规：`);
  for (const v of violations) console.error(`  - ${v}`);
  process.exit(1);
}

const nChanges = listDirs(CHANGES).length;
const nArchive = listDirs(ARCHIVE).length;
console.log(`[openspec-hygiene] OK：changes/ 进行中 ${nChanges} 个，archive/ 已归档 ${nArchive} 个。`);

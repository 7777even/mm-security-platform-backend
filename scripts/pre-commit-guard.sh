#!/usr/bin/env sh
# pre-commit 守门：拒绝把构建产物与临时输出提交入库（AGENTS.md 工程约定「禁止提交构建产物与临时输出」）。
# 用法：pre-commit-guard.sh   （无参数，直接读暂存区）
# 命名与两仓同源；改 DENY/ALLOW 须同步另一仓与 AGENTS.md。
set -eu

# 拒绝模式（暂存区路径命中任一即拒）
DENY='(^|/)(target|dist|coverage|node_modules)/|(^|/)[^/]*[-_]out\.txt$|(^|/)nohup\.out$|\.(log|tmp|bak|orig|rej|swp)$|~$'
# 允许例外（如 openspec 归档里的 QA 证据附件）
ALLOW='^openspec/'

staged=$(git diff --cached --name-only --diff-filter=ACMR)
if [ -z "$staged" ]; then
  exit 0
fi

bad=$(printf '%s\n' "$staged" | grep -Ev "$ALLOW" | grep -E "$DENY" || true)

if [ -n "$bad" ]; then
  echo "❌ 禁止提交构建产物或临时输出（AGENTS.md 工程约定）：" >&2
  printf '%s\n' "$bad" | sed 's/^/   /' >&2
  echo "   临时输出请写入系统临时目录或加进 .gitignore，不要入库。" >&2
  echo "   确需入库的证据附件，请在 scripts/pre-commit-guard.sh 的 ALLOW 加例外并说明理由。" >&2
  exit 1
fi

exit 0

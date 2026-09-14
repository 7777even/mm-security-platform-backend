#!/usr/bin/env sh
# commit-msg 守门：type(scope): 中文描述 + scope 固定枚举 + 提交信息单行（禁正文）
# 用法：commit-msg-lint.sh <commit-msg-file>
# 返回 0 通过；非 0 拒绝提交。
# 规则出处：AGENTS.md §6.5（提交格式与 scope 枚举）、§3.4（提交信息单行成句、禁止分点列表）。
set -eu

# scope 固定枚举（AGENTS.md §6.5）。改枚举须同步改 AGENTS.md，禁止在代码里单方面放宽。
# 域 scope 之外，openspec（变更流程与归档）与 ci（CI 流水线）为 2026-09-14 收编的正式枚举。
ALLOWED_SCOPES="auth device alarm dashboard security common db config docs chore openspec ci"

msg_file="${1:-}"
if [ -z "$msg_file" ] || [ ! -f "$msg_file" ]; then
  echo "❌ commit-msg-lint: 未收到提交信息文件" >&2
  exit 1
fi

first_line=$(head -n 1 "$msg_file" | tr -d '\r')

# 1) conventional commits 格式：type(scope): 描述 或 type: 描述
if ! printf '%s\n' "$first_line" | grep -qE '^(feat|fix|docs|style|refactor|perf|test|build|ci|chore)(\([^)]+\))?[：:].+'; then
  echo "❌ 提交信息不符合 'type(scope): 描述' 格式" >&2
  echo "   正确示例: feat(alarm): 告警推送增加重试上限" >&2
  echo "   允许 type: feat|fix|docs|style|refactor|perf|test|build|ci|chore" >&2
  exit 1
fi

# 2) 描述必须含中文（整行仅含 ASCII 字节视为未用中文，拒绝）
#    用 od 逐字节检测高半字节(>=0x80)——UTF-8 中文必含此类字节，且与 locale 无关。
if printf '%s' "$first_line" | od -An -tx1 | grep -qE ' [89a-f][0-9a-f]'; then
  : # 含非 ASCII 字节（中文），通过
else
  echo "❌ 提交描述必须使用中文（未检测到多字节字符，疑似纯英文）" >&2
  exit 1
fi

# 3) scope 必须取自本仓固定枚举，禁止自造（多 scope 允许，逗号分隔逐个校验）
scope=$(printf '%s' "$first_line" | sed -n 's/^[a-z]*(\([^)]*\))[：:].*/\1/p')
if [ -n "$scope" ]; then
  for one in $(printf '%s' "$scope" | tr ',' ' '); do
    case " $ALLOWED_SCOPES " in
      *" $one "*) : ;;
      *)
        echo "❌ 自造 scope '($one)'：本仓仅允许 $ALLOWED_SCOPES" >&2
        echo "   无合适 scope 时写 'type: 描述'（不带括号）；确需新 scope 先在 AGENTS.md §6.5 扩枚举。" >&2
        exit 1
        ;;
    esac
  done
fi

# 4) 提交信息只允许「一行标题」：body 非空即拒（AGENTS.md『提交信息单行成句』）
#    正文会把一条提交写成描述段落，细节应写进 docs/、openspec/ 或代码注释。
body=$(tail -n +2 "$msg_file" | tr -d '\r' | sed '/^[[:space:]]*$/d')
if [ -n "$body" ]; then
  echo "❌ 提交信息只能有一行标题（'type(scope): 描述'），禁止正文/body：" >&2
  printf '%s\n' "$body" | sed 's/^/   /' >&2
  echo "   细节请写进 docs/、openspec/ 或代码注释，确需说明的内容并进标题那一句。" >&2
  exit 1
fi

exit 0

#!/usr/bin/env sh
# commit-msg 守门：type(scope): 中文描述 + scope 固定枚举 + 正文单句（禁列表/禁分号问叹/限逗号/限长）
# 用法：commit-msg-lint.sh <commit-msg-file>
# 返回 0 通过；非 0 拒绝提交。
# 规则出处：AGENTS.md §6.5（提交格式与 scope 枚举）、§3.4（提交信息单行成句、禁止分点列表）。
set -eu

# scope 固定枚举（AGENTS.md §6.5）。改枚举须同步改 AGENTS.md，禁止在代码里单方面放宽。
ALLOWED_SCOPES="auth device alarm dashboard security common db config docs chore"

# 正文长度上限（字节）。中文 UTF-8 三字节/字，180 字节约合 60 个中文字，足以容纳一句总结。
BODY_MAX_BYTES=180

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

# 4) body 纪律：正文须单句、禁列表、禁长段落（与 AGENTS.md『正文单句禁列表（hook 拒）』一致）
#    仅校验首行(header)之后的 body，避免误伤 header。
body=$(tail -n +2 "$msg_file" | tr -d '\r' | sed '/^[[:space:]]*$/d')
if [ -n "$body" ]; then
  # 4a) 禁止 '- ' / '* ' 分点列表
  if printf '%s\n' "$body" | grep -qE '^[[:space:]]*[-*] '; then
    echo "❌ 提交 body 禁止用 '- ' / '* ' 分点列表，只写一句总结性语句" >&2
    exit 1
  fi
  # 4b) 禁枚举/列表符号『、』（顿号即列表）
  if printf '%s\n' "$body" | grep -qF '、'; then
    echo "❌ 提交 body 禁止出现顿号'、'枚举（正文禁列表），改为单句描述" >&2
    exit 1
  fi
  # 4c) 单句：禁止分号『；』及感叹/疑问符『！？』
  if printf '%s\n' "$body" | grep -qE '；|[！？]'; then
    echo "❌ 提交 body 须为单句，禁止分号'；'/感叹/疑问符（正文单句）" >&2
    exit 1
  fi
  # 4d) 句号『。』不得超过一个
  if printf '%s\n' "$body" | grep -q '。.*。'; then
    echo "❌ 提交 body 须为单句，句号'。'不得超过一个" >&2
    exit 1
  fi
  # 4e) 逗号分句不得超过一处（两处即三句以上，属长段落）
  commas=$(printf '%s' "$body" | grep -o '[，,]' | wc -l | tr -d ' ')
  if [ "$commas" -gt 1 ]; then
    echo "❌ 提交 body 逗号分句过多（$commas 处）：正文须一句总结，禁止长段落，细节写进代码注释或 openspec" >&2
    exit 1
  fi
  # 4f) 长度上限，兜住无标点堆砌的长句
  bytes=$(printf '%s' "$body" | wc -c | tr -d ' ')
  if [ "$bytes" -gt "$BODY_MAX_BYTES" ]; then
    echo "❌ 提交 body 过长（$bytes 字节 > $BODY_MAX_BYTES）：正文只写一句总结性语句" >&2
    exit 1
  fi
fi

exit 0

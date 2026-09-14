#!/usr/bin/env sh
# commit-msg 守门：强制 type(scope): 中文描述、单行无分点列表
# 用法：commit-msg-lint.sh <commit-msg-file>
# 返回 0 通过；非 0 拒绝提交。
set -eu

msg_file="${1:-}"
if [ -z "$msg_file" ] || [ ! -f "$msg_file" ]; then
  echo "❌ commit-msg-lint: 未收到提交信息文件" >&2
  exit 1
fi

first_line=$(head -n 1 "$msg_file")

# 1) conventional commits 格式：type(scope): 描述 或 type: 描述
if ! printf '%s\n' "$first_line" | grep -qE '^(feat|fix|docs|style|refactor|perf|test|build|ci|chore)(\([^)]+\))?[：:].+'; then
  echo "❌ 提交信息不符合 'type(scope): 描述' 格式" >&2
  echo "   正确示例: feat(screen): 视频网格改由后端抓拍图渲染静态画面" >&2
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

# 3) body 纪律：正文须单句、禁列表（与 AGENTS.md『正文单句禁列表（hook 拒）』一致）
#    仅校验首行(header)之后的 body，避免误伤 header。
body=$(tail -n +2 "$msg_file" | sed '/^[[:space:]]*$/d')
if [ -n "$body" ]; then
  # 3a) 禁止 '- ' / '* ' 分点列表
  if printf '%s\n' "$body" | grep -qE '^[[:space:]]*[-*] '; then
    echo "❌ 提交 body 禁止用 '- ' / '* ' 分点列表，只写一句总结性语句" >&2
    exit 1
  fi
  # 3b) 禁枚举/列表符号『、』（顿号即列表）
  if printf '%s\n' "$body" | grep -qF '、'; then
    echo "❌ 提交 body 禁止出现顿号'、'枚举（正文禁列表），改为单句描述" >&2
    exit 1
  fi
  # 3c) 单句：禁止分号『；』及感叹/疑问符『！？』
  if printf '%s\n' "$body" | grep -qE '；|[！？]'; then
    echo "❌ 提交 body 须为单句，禁止分号'；'/感叹/疑问符（正文单句）" >&2
    exit 1
  fi
  # 3d) 句号『。』不得超过一个
  if printf '%s\n' "$body" | grep -q '。.*。'; then
    echo "❌ 提交 body 须为单句，句号'。'不得超过一个" >&2
    exit 1
  fi
fi

exit 0

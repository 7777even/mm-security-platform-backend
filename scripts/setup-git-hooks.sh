#!/usr/bin/env sh
# 一次性启用本仓库的 git 钩子（commit-msg 中文/单行规范校验）。
# 用法：bash scripts/setup-git-hooks.sh
# 作用：将 core.hooksPath 指向 scripts/git-hooks（版本库内，随仓库分发），
#       之后所有提交都会经过 commit-msg-lint.sh 校验。
set -eu
git config core.hooksPath scripts/git-hooks
echo "✅ 已启用 commit-msg 钩子：core.hooksPath=scripts/git-hooks"
echo "   提交信息须符合 'type(scope): 中文描述' 且单行无分点列表，否则被拒绝。"

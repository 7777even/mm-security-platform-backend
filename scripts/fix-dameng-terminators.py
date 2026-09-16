#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
给达梦(DM8) 回填迁移文件中「缺 ';' 终结符」的文件补 ';'，
对齐本仓 V1/V2 与 h2/pg 的仓库惯例（标准 Flyway 默认 ';' 分隔符需要）。

仅处理「含 CREATE TABLE 且 CREATE TABLE 数 > (';'数 + '/'数)」的文件
（即 check-dialect-migration-consistency.py 报告的 [TERMINATOR] 文件），
不动 V1/V2 等已有 ';' 的文件。

算法：按「空行(含纯空白行)」切分为语句块；每块取最后一条非空、非纯注释
(`--`) 的行，若其不以 ';' 结尾则补 ';'。多行语句(内部无空行)安全。

注意：本机无 DM8 实例，补 ';' 后无法实跑验证，须到 DM8 实例复核。

用法：
  python fix-dameng-terminators.py --migration-dir <dir> --dialect dameng --dry-run
  python fix-dameng-terminators.py --migration-dir <dir> --dialect dameng
"""
import argparse
import os
import re
import sys

COMMENT_RE = re.compile(r"^\s*--")
BLANK_RE = re.compile(r"^\s*$")


def needs_fix(raw):
    n_ct = len(re.findall(r"CREATE\s+TABLE", raw, re.IGNORECASE))
    n_semi = raw.count(";")
    n_slash = len(re.findall(r"^\s*/\s*$", raw, re.MULTILINE))
    return n_ct > 0 and n_ct > (n_semi + n_slash)


def fix_text(raw):
    """按空行切分语句块，每块末行补 ';'。保持注释/空行位置不变。"""
    lines = raw.split("\n")
    out = []
    block = []
    changed = False

    def flush():
        nonlocal block, changed
        if not block:
            return
        # 找到最后一条非空、非纯注释行
        last = None
        for i in range(len(block) - 1, -1, -1):
            if block[i].strip() and not COMMENT_RE.match(block[i]):
                last = i
                break
        if last is not None and not block[last].rstrip().endswith(";"):
            block[last] = block[last].rstrip() + ";"
            changed = True
        out.extend(block)

    for ln in lines:
        if BLANK_RE.match(ln):
            flush()
            out.append(ln)  # 保留空行
            block = []
        else:
            block.append(ln)
    flush()  # 文件末尾的块
    return "\n".join(out), changed


def main():
    ap = argparse.ArgumentParser(description="给达梦缺终结符的迁移文件补 ';'")
    ap.add_argument("--migration-dir", required=True)
    ap.add_argument("--dialect", default="dameng")
    ap.add_argument("--dry-run", action="store_true")
    args = ap.parse_args()

    d = os.path.join(args.migration_dir, args.dialect)
    files = sorted(f for f in os.listdir(d) if f.lower().endswith(".sql"))
    total_changed = 0
    for f in files:
        p = os.path.join(d, f)
        with open(p, "r", encoding="utf-8") as fh:
            raw = fh.read()
        if not needs_fix(raw):
            continue
        new, changed = fix_text(raw)
        if not changed:
            print(f"[SKIP-NOCHANGE] {f}")
            continue
        total_changed += 1
        if args.dry_run:
            # 仅展示前后 ';' 计数变化
            before = raw.count(";")
            after = new.count(";")
            print(f"[DRY-RUN] {f}: ';' {before} -> {after}")
        else:
            with open(p, "w", encoding="utf-8") as fh:
                fh.write(new)
            print(f"[FIXED] {f}: ';' {raw.count(';')} -> {new.count(';')}")
    print(f"\n共处理 {total_changed} 个文件" + (" (dry-run)" if args.dry_run else ""))


if __name__ == "__main__":
    main()

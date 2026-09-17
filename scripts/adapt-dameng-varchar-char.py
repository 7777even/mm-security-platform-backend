#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
adapt-dameng-varchar-char.py — DM8 列宽「字节语义 -> 字符语义」适配器

背景
----
达梦自 2024 Q2 起**废弃**了全局参数 `LENGTH_IN_CHAR`（实例初始化时无法再设置，
所有库统一按 **字节** 计算 VARCHAR 长度）。而本项目 h2 / PostgreSQL 两个方言
的 `VARCHAR(n)` / `VARCHAR2(n)` 天然是 **字符** 语义。

后果（已在真机 DM8 实跑抓到）：
  `VARCHAR2(8)` 存 '未核实'（3 汉字 = 9 字节）→ `[-6169] 列[false_alarm]长度超出定义`

官方替代方案 = 字段级 `VARCHAR(n CHAR)` 语法（按字符计数，不依赖全局参数）。

本脚本把 dameng 方言迁移里的 `VARCHAR(n)` / `VARCHAR2(n)` 统一改写为
`VARCHAR(n CHAR)` / `VARCHAR2(n CHAR)`，使其与 h2 / PG 的字符语义对齐。

只改 dameng —— h2 / PG 本就是字符语义，不得触碰。

用法
----
    # 预演（默认，不写文件）
    python scripts/adapt-dameng-varchar-char.py

    # 落盘
    python scripts/adapt-dameng-varchar-char.py --apply

改完请务必重跑三方言静态校验：
    python scripts/check-dialect-migration-consistency.py \
        --migration-dir src/main/resources/db/migration --dialects h2,dameng,postgresql
"""
from __future__ import annotations

import argparse
import re
import sys
from collections import Counter
from pathlib import Path

# 只匹配「数字 + 可选空白 + 右括号」的裸写法；
# 已经带 CHAR / BYTE 后缀的（如 `VARCHAR2(8 CHAR)`）数字后跟的是空格+CHAR，
# 不满足 `\s*\)`，天然被排除，重复执行幂等。
PATTERN = re.compile(r"\b(VARCHAR2?)\s*\(\s*(\d+)\s*\)", re.IGNORECASE)

# CHAR(n) 不动：项目里仅 4 处 `device_code CHAR(20)`，存纯 ASCII 不受字节语义影响。
EXCLUDE_CAST = re.compile(r"\bCAST\s*\(", re.IGNORECASE)


def read_raw(path: Path) -> str:
    """按原样读取（newline='' 保留 CRLF/LF，避免整文件行尾被改写）。"""
    with open(path, "r", encoding="utf-8", newline="") as fh:
        return fh.read()


def write_raw(path: Path, text: str) -> None:
    with open(path, "w", encoding="utf-8", newline="") as fh:
        fh.write(text)


def scan_file(path: Path) -> tuple[str, int, Counter]:
    """返回 (原始文本, 替换处数, 长度分布)"""
    raw = read_raw(path)
    hits = PATTERN.findall(raw)
    lengths = Counter(int(n) for _, n in hits)
    return raw, len(hits), lengths


def rewrite(raw: str) -> str:
    def repl(m: re.Match) -> str:
        return f"{m.group(1)}({m.group(2)} CHAR)"

    return PATTERN.sub(repl, raw)


def main() -> int:
    ap = argparse.ArgumentParser()
    ap.add_argument(
        "--migration-dir",
        default="src/main/resources/db/migration/dameng",
        help="dameng 迁移目录（默认 src/main/resources/db/migration/dameng）",
    )
    ap.add_argument("--apply", action="store_true", help="真正写入文件（默认 dry-run）")
    args = ap.parse_args()

    root = Path(args.migration_dir)
    if not root.is_dir():
        print(f"[ERROR] not a directory: {root}", file=sys.stderr)
        return 2

    files = sorted(root.glob("V*.sql"), key=lambda p: int(re.match(r"V(\d+)", p.name).group(1)))
    if not files:
        print(f"[ERROR] no V*.sql under {root}", file=sys.stderr)
        return 2

    total_hits = 0
    touched: list[tuple[Path, int]] = []
    all_lengths: Counter = Counter()
    cast_hits: list[str] = []

    for f in files:
        raw, n, lengths = scan_file(f)
        if CAST_EXCLUDE := [ln for ln in raw.splitlines() if EXCLUDE_CAST.search(ln) and PATTERN.search(ln)]:
            cast_hits.extend(f"{f.name}: {ln.strip()}" for ln in CAST_EXCLUDE[:3])
        all_lengths.update(lengths)
        if n:
            total_hits += n
            touched.append((f, n))
            if args.apply:
                write_raw(f, rewrite(raw))

    mode = "APPLY" if args.apply else "DRY-RUN"
    print("=" * 64)
    print(f"DM8 列宽字节->字符语义适配  [{mode}]")
    print(f"目录: {root}")
    print("-" * 64)
    print(f"扫描文件     : {len(files)}")
    print(f"命中文件     : {len(touched)}")
    print(f"改写处数     : {total_hits}")
    print("-" * 64)
    print("列宽长度分布 (原 n，改写后即 n CHAR)：")
    for n, c in sorted(all_lengths.items()):
        flag = "  <-- 宽列，留意 DM8 行上限" if n > 4000 else ""
        print(f"  VARCHAR2({n:<5}) x {c}{flag}")
    print("-" * 64)
    print("改动最多的 10 个文件：")
    for f, n in sorted(touched, key=lambda t: -t[1])[:10]:
        print(f"  {n:>4}  {f.name}")

    if cast_hits:
        print("-" * 64)
        print("⚠️ 检测到 CAST/VARCHAR2 同行，请人工确认是否应改写：")
        for h in cast_hits[:10]:
            print(f"  {h}")

    print("=" * 64)
    if args.apply:
        print("✅ 已写入。下一步：重跑三方言静态校验 + 清库重跑 DM8 迁移。")
    else:
        print("ℹ️  dry-run，未改动任何文件。确认无误后加 --apply。")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

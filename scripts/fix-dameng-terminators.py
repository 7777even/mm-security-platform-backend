#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
给 Flyway 迁移文件回填缺失的「语句终结符 ';'」（默认方言 dameng）。

背景：Flyway 默认以 ';' 切分迁移脚本。达梦方言多份迁移里，种子块**只有最后一条语句带
';'**，其余（单行 INSERT/UPDATE/ALTER，以及跨多行的 `INSERT ... ) VALUES (...)`）都缺
—— 相邻多条会被当成**一整条非法 SQL**，DM8 实跑必失败。dev 只跑 h2 目录，故长期不暴露。

判据与补全均由 `sql_stmt_scan.py`（括号深度状态机）提供：能正确识别多行语句、且不会
误断多行 `CREATE TABLE` 表头 / `INSERT INTO t (cols)`+`SELECT` 表头 / PG `ON CONFLICT`。
（旧版本按「空行」切块、每块只补最后一行，且跳过不含 CREATE TABLE 的文件 —— 达梦种子
连续无空行，故只补上 1/N，属失效实现。）

用法：
  python scripts/fix-dameng-terminators.py --migration-dir <dir> --dialect dameng --dry-run   # 只看
  python scripts/fix-dameng-terminators.py --migration-dir <dir> --dialect dameng --apply      # 写入
"""
import argparse
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import sql_stmt_scan  # noqa: E402


def main():
    ap = argparse.ArgumentParser(description="给 Flyway 迁移文件回填缺失的 ';' 终结符")
    ap.add_argument("--migration-dir", required=True)
    ap.add_argument("--dialect", default="dameng")
    ap.add_argument("--dry-run", action="store_true", help="只打印，不写文件（默认行为）")
    ap.add_argument("--apply", action="store_true", help="实际写入文件")
    args = ap.parse_args()

    if args.dry_run and args.apply:
        print("--dry-run 与 --apply 互斥")
        sys.exit(2)
    do_write = args.apply

    d = os.path.join(args.migration_dir, args.dialect)
    if not os.path.isdir(d):
        print(f"[ERR] 目录不存在: {d}")
        sys.exit(2)

    n_files, n_lines = 0, 0
    print(f"== {'APPLY' if do_write else 'DRY-RUN'} : {args.dialect} ==")
    for fname in sorted(f for f in os.listdir(d) if f.lower().endswith(".sql")):
        p = os.path.join(d, fname)
        with open(p, "rb") as fh:
            raw = fh.read().decode("utf-8")
        new, fixed = sql_stmt_scan.fix(raw)
        if not fixed:
            continue
        n_files += 1
        n_lines += len(fixed)
        preview = ", ".join(str(x) for x in fixed[:6]) + ("..." if len(fixed) > 6 else "")
        if do_write:
            with open(p, "wb") as fh:
                fh.write(new.encode("utf-8"))
            print(f"  [FIXED] {fname:42s} 补 {len(fixed):4d} 处 (行 {preview})")
        else:
            print(f"  [DRY]   {fname:42s} 待补 {len(fixed):4d} 处 (行 {preview})")

    if n_files == 0:
        print("  无需修改（语句终结符已完整）")
    else:
        print(f"\n共 {n_files} 个文件 / {n_lines} 处" + ("   已写入" if do_write else "   待写入（加 --apply 生效）"))
    print("注意：本机无 DM8 实例，补 ';' 后仍须到达梦实例实跑复核。")


if __name__ == "__main__":
    main()

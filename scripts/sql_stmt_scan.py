#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Flyway 迁移 SQL 的「语句终结符」扫描与补全（h2 / dameng / postgresql 共用）。

为什么需要
---------
Flyway 默认以 ';' 切分迁移脚本。达梦方言多份迁移里，种子块**只有最后一条语句带 ';'**，
其余（单行 INSERT/UPDATE/ALTER，以及跨多行的 `INSERT ... ) VALUES (...)`）都缺 —— 相邻多条
被当成**一整条非法 SQL**，DM8 实跑必失败。H2 宽松、且 dev 只跑 `db/migration/h2`，
所以这个坑在本地长期不暴露。

算法（括号深度状态机）
---------------------
  1. 语句开始：累计深度==0 且行首匹配 INSERT/UPDATE/DELETE/MERGE/TRUNCATE/ALTER/CREATE/DROP/COMMENT
  2. 逐行累加括号深度（忽略字符串字面量内的括号）与累计文本
  3. 语句「形已完成」须同时满足：
     - 累计深度 <= 0
     - 末 token 不是续行关键字（VALUES / SELECT / SET / FROM / WHERE / AND / ...）
     - 不以 ',' 或 '(' 结尾
     - **关键字闸门**：INSERT 须已出现 VALUES、UPDATE 须已出现 SET
       （否则 `INSERT INTO t (cols)` + 下一行 SELECT 的多行表头会被误断）
     - **前瞻**：下一条有效行不以 ON/DO/SELECT/FROM/... 或 ( , ; | 开头
       （否则 PG 的 `INSERT ... VALUES (..),(..) ON CONFLICT ... DO NOTHING;` 会被误断）
  4. 若该行尚未以 ';' 结尾 → 记为一处待补

保留原行尾风格（CRLF / LF）。仅依赖标准库；显式 utf-8（中文 Windows 下必填）。

用法（独立体检）:
  python scripts/sql_stmt_scan.py --migration-dir src/main/resources/db/migration --dialects h2,dameng,postgresql
退出码：0 = 无缺终结符；1 = 有（便于进 CI）。
"""
import argparse
import os
import re
import sys

STMT_START = re.compile(
    r"(?i)^\s*(INSERT|UPDATE|DELETE|MERGE|TRUNCATE|ALTER|CREATE|DROP|COMMENT)\b"
)
CONT_TAIL = {"values", "select", "set", "from", "where", "and", "or", "not", "on",
             "into", "as", "when", "then", "else", "by", "using", "join", "left",
             "right", "inner", "outer", "union", "all", "distinct", "returning",
             "begin", "declare", "if", "loop", "case"}
CONT_START = re.compile(
    r"(?i)^(ON|DO|WHERE|AND|OR|SELECT|FROM|VALUES|SET|RETURNING|UNION|EXCEPT|"
    r"INTERSECT|GROUP|ORDER|HAVING|LIMIT|OFFSET|JOIN|LEFT|RIGHT|INNER|OUTER|FULL|"
    r"CROSS|USING|WHEN|THEN|ELSE|END)\b|^\s*[(),;|]")


def split_trailing_comment(line):
    """切出行尾 '--' 注释（引号内的 '--' 不算）。返回 (code, comment)。"""
    in_str = False
    for i, ch in enumerate(line):
        if ch == "'":
            in_str = not in_str
        elif not in_str and ch == "-" and i + 1 < len(line) and line[i + 1] == "-":
            return line[:i], line[i:]
    return line, ""


def strip_str_literals(text):
    return re.sub(r"'(?:[^']|'')*'", "''", text)


def _stmt_completes(kind, acc, code_part, depth):
    if depth > 0:
        return False
    body = strip_str_literals(code_part).rstrip().rstrip(";")
    toks = re.split(r"[\s(]+", body)
    last = toks[-1].rstrip(",;").lower() if toks else ""
    if code_part.rstrip().endswith(",") or code_part.rstrip().endswith("("):
        return False
    if last in CONT_TAIL:
        return False
    if kind == "INSERT":
        return "VALUES" in acc
    if kind == "UPDATE":
        return " SET " in acc
    return True


def _next_code_line(lines, i):
    j = i + 1
    while j < len(lines):
        cp, _ = split_trailing_comment(lines[j])
        if cp.strip():
            return cp.strip()
        j += 1
    return ""


def scan_lines(lines):
    """返回**待补 ';' 的行下标（0 基）**列表。"""
    out = []
    in_stmt = False
    depth = 0
    kind = ""
    acc = ""
    for i, raw in enumerate(lines):
        code_part, _ = split_trailing_comment(raw)
        if not code_part.strip():
            continue
        if not in_stmt:
            m = STMT_START.match(code_part) if depth == 0 else None
            if not m:
                continue
            in_stmt, kind, acc = True, m.group(1).upper(), ""
        s = strip_str_literals(code_part)
        depth += s.count("(") - s.count(")")
        acc += " " + s.upper()
        if code_part.rstrip().endswith(";"):
            in_stmt, depth, acc = False, 0, ""
            continue
        if _stmt_completes(kind, acc, code_part, depth) \
                and not CONT_START.match(_next_code_line(lines, i)):
            out.append(i)
            in_stmt, depth, acc = False, 0, ""
    return out


def scan(text):
    """扫描文本，返回待补 ';' 的行号（1 基）列表。"""
    return [i + 1 for i in scan_lines(text.replace("\r\n", "\n").split("\n"))]


def fix(text):
    """补全文本中缺失的 ';'。返回 (新文本, 补全行号[1 基])，保留原行尾风格。"""
    nl = "\r\n" if "\r\n" in text else "\n"
    lines = text.replace("\r\n", "\n").split("\n")
    idx = scan_lines(lines)
    for i in idx:
        code, comment = split_trailing_comment(lines[i])
        lines[i] = code.rstrip() + ";" + comment
    return nl.join(lines), [i + 1 for i in idx]


def main():
    ap = argparse.ArgumentParser(description="Flyway 迁移语句终结符体检")
    ap.add_argument("--migration-dir", required=True)
    ap.add_argument("--dialects", default="h2,dameng,postgresql")
    args = ap.parse_args()

    bad = 0
    for d in [x.strip() for x in args.dialects.split(",")]:
        folder = os.path.join(args.migration_dir, d)
        if not os.path.isdir(folder):
            continue
        for fname in sorted(f for f in os.listdir(folder) if f.lower().endswith(".sql")):
            p = os.path.join(folder, fname)
            with open(p, "r", encoding="utf-8") as fh:
                miss = scan(fh.read())
            if miss:
                bad += len(miss)
                preview = ", ".join(str(x) for x in miss[:8]) + ("..." if len(miss) > 8 else "")
                print(f"[TERMINATOR] {d}/{fname} 待补 {len(miss)} 处 (行 {preview})")

    if bad:
        print(f"\n❌ 共 {bad} 处语句缺 ';' 终结符 —— Flyway 默认分隔符将无法切分多语句")
        print("   修复：python scripts/fix-dameng-terminators.py --migration-dir <dir> --dialect <dialect>")
    else:
        print("✅ 三方言迁移语句终结符完整")
    sys.exit(1 if bad else 0)


if __name__ == "__main__":
    main()

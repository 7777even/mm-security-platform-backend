#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
三方言迁移静态一致性校验 (Three-dialect Flyway migration static consistency check)

解析 h2 / dameng / postgresql 三套 Flyway 迁移 SQL（按 V* 顺序累加
CREATE TABLE + ALTER TABLE ADD COLUMN），报告：

  1. 版本文件 parity（三方言须有同一组 Vn）
  2. 表存在 parity（某方言有某表，其他方言也须有）
  3. 每表列名 parity（列名与方言无关；DM 缺列会在运行时破坏
     Java @TableField 映射，即使 H2 通过也会在 DM 炸）
  4. 类型族 parity（软警告，容忍已知方言映射）

仅依赖标准库；显式 utf-8（中文 Windows 下必填）。
退出码：0 = 无结构性错误（clean 或仅有 warning）；1 = 发现结构性错误。

用法：
  python check-dialect-migration-consistency.py \
      --migration-dir src/main/resources/db/migration \
      --dialects h2,dameng,postgresql
"""
import argparse
import json
import os
import re
import sys
from collections import defaultdict

SKIP_COLUMN_KEYWORDS = {
    "constraint", "primary", "foreign", "unique", "check", "key", "index", ")",
}


def read_sql_files(base, dialect):
    d = os.path.join(base, dialect)
    if not os.path.isdir(d):
        return []
    files = sorted([f for f in os.listdir(d) if f.lower().endswith(".sql")])
    out = []
    for f in files:
        p = os.path.join(d, f)
        with open(p, "r", encoding="utf-8") as fh:
            out.append((f, fh.read()))
    return out


def strip_comments(sql):
    sql = re.sub(r"/\*.*?\*/", " ", sql, flags=re.DOTALL)
    sql = re.sub(r"--[^\n]*", " ", sql)
    return sql


def split_statements(sql):
    parts = []
    buf = []
    for ch in sql:
        if ch == ";":
            s = "".join(buf).strip()
            if s:
                parts.append(s)
            buf = []
        else:
            buf.append(ch)
    tail = "".join(buf).strip()
    if tail:
        parts.append(tail)
    return parts


def normalize_ident(name):
    return name.strip().strip("`\" ").strip().lower()


def extract_table_name(stmt):
    m = re.match(r"create\s+table\s+(if\s+not\s+exists\s+)?([^\s(]+)", stmt, re.IGNORECASE)
    if not m:
        return None
    raw = m.group(2).split("(")[0]
    if "." in raw:
        raw = raw.split(".")[-1]
    return normalize_ident(raw)


def parse_create_columns(body):
    # body = text inside the outer parentheses of CREATE TABLE
    depth = 0
    cols = []
    cur = []
    i = 0
    while i < len(body):
        ch = body[i]
        if ch == "(":
            depth += 1
            cur.append(ch)
        elif ch == ")":
            if depth == 0:
                break
            depth -= 1
            cur.append(ch)
        elif ch == "," and depth == 0:
            cols.append("".join(cur))
            cur = []
        else:
            cur.append(ch)
        i += 1
    if cur:
        cols.append("".join(cur))
    result = []
    for c in cols:
        c = c.strip()
        if not c:
            continue
        m = re.match(r"([^\s(]+)", c)
        if not m:
            continue
        name = normalize_ident(m.group(1))
        if not name or name in SKIP_COLUMN_KEYWORDS:
            continue
        low = c.lower()
        # 仅当片段首词即约束关键字（表级约束）才跳过；
        # 行内主键（如 `id BIGINT ... PRIMARY KEY`）首词是列名，必须保留。
        if re.match(r"(constraint|primary|foreign|unique|check|key|index)\b", low):
            continue
        rest = c[m.end():].strip()
        result.append((name, rest))
    return result


def parse_alter_add(stmt):
    m = re.match(r"alter\s+table\s+([^\s]+)\s+add\s+(column\s+)?(.+)", stmt, re.IGNORECASE)
    if not m:
        return None
    raw = m.group(1)
    if "." in raw:
        raw = raw.split(".")[-1]
    table = normalize_ident(raw)
    rest = m.group(3).strip()
    rest = re.sub(r"^IF\s+NOT\s+EXISTS\s+", "", rest, flags=re.IGNORECASE)
    depth = 0
    cur = []
    cols = []
    for ch in rest:
        if ch == "(":
            depth += 1
            cur.append(ch)
        elif ch == ")":
            depth -= 1
            cur.append(ch)
        elif ch == "," and depth == 0:
            cols.append("".join(cur))
            cur = []
        else:
            cur.append(ch)
    if cur:
        cols.append("".join(cur))
    added = []
    for c in cols:
        c = c.strip()
        mm = re.match(r"([^\s(]+)", c)
        if not mm:
            continue
        name = normalize_ident(mm.group(1))
        if name in SKIP_COLUMN_KEYWORDS:
            continue
        added.append((name, c[mm.end():].strip()))
    return (table, added)


def extract_create_blocks(sql):
    """按平衡括号提取每个 CREATE TABLE 的 (表名, 内部列定义体)。
    不依赖语句终结符（';' 或 '/'），对缺终结符的方言文件同样可用。"""
    blocks = []
    for m in re.finditer(
        r"CREATE\s+TABLE\s+(?:IF\s+NOT\s+EXISTS\s+)?([^\s(]+)\s*\(", sql, re.IGNORECASE
    ):
        name = normalize_ident(m.group(1))
        if "." in name:
            name = name.split(".")[-1]
        depth = 1
        i = m.end()
        while i < len(sql) and depth > 0:
            ch = sql[i]
            if ch == "(":
                depth += 1
            elif ch == ")":
                depth -= 1
            i += 1
        body = sql[m.end():i - 1]
        blocks.append((name, body))
    return blocks


def extract_alter_adds(sql):
    adds = []
    for m in re.finditer(
        r"ALTER\s+TABLE\s+([^\s]+)\s+ADD\s+(?:COLUMN\s+)?", sql, re.IGNORECASE
    ):
        tbl = normalize_ident(m.group(1))
        if "." in tbl:
            tbl = tbl.split(".")[-1]
        rest = sql[m.end():]
        nm = re.search(
            r"\n\s*(?:CREATE|ALTER|INSERT|UPDATE|DELETE|DROP|--|GRANT|COMMENT)\b",
            rest,
            re.IGNORECASE,
        )
        chunk = rest[: nm.start()] if nm else rest
        chunk = chunk.strip().rstrip(";").strip()
        if chunk:
            adds.append((tbl, f"ALTER TABLE {tbl} ADD {chunk}"))
    return adds


def accumulate(dialect_files):
    schema = defaultdict(dict)  # table -> {colname: type}
    for _fname, sql in dialect_files:
        sql = strip_comments(sql)
        for (name, body) in extract_create_blocks(sql):
            for (col, typ) in parse_create_columns(body):
                schema[name][col] = typ
        for (tbl, syn) in extract_alter_adds(sql):
            r = parse_alter_add(syn)
            if r:
                t, added = r
                for (col, typ) in added:
                    schema[t][col] = typ
    return schema


def type_family(t):
    """把方言物理类型归一到逻辑族；已知等价映射（BIGINT↔NUMBER(19)↔BIGSERIAL、
    INT↔NUMBER(9)、BOOLEAN↔NUMBER(1)、TINYINT/SMALLINT↔NUMBER(3) 等）视为同一族，
    避免把方言语法差异误报为不一致。仅在语义族真正不同时才告警。"""
    if not t:
        return "unknown"
    s = t.lower()
    # 先去掉所有括号内容（尺寸 / 精度），再去掉约束/默认值关键词
    s = re.sub(r"\([^)]*\)", " ", s)
    s = re.sub(
        r"(primary key|not null|null|default [^ ]*|auto_increment|"
        r"generated by default as identity|generated|identity|unique|"
        r"comment[^,]*|references[^,]*|on update[^,]*|using [a-z]+|"
        r"constraint|check\s*\([^)]*\))",
        " ",
        s,
    )
    toks = s.split()
    if not toks:
        return "unknown"
    t0 = toks[0]
    if t0 == "double" and len(toks) > 1 and toks[1] == "precision":
        t0 = "double precision"
    elif t0 == "character" and len(toks) > 1:
        t0 = "character " + toks[1]
    # number(p) 按精度归族（原串可能大写 NUMBER，须忽略大小写搜索）
    if "number" in t0:
        mm = re.search(r"number\s*\(?\s*(\d+)", t, re.IGNORECASE)
        if mm:
            p = int(mm.group(1))
            if p <= 1:
                return "bool"
            if p <= 4:
                return "smallint"
            if p >= 19:
                return "longint"
            return "int"
    fam = {
        "varchar": "string", "varchar2": "string", "char": "string",
        "nchar": "string", "nvarchar": "string", "nvarchar2": "string",
        "text": "longtext", "clob": "longtext", "longvarchar": "longtext",
        "boolean": "bool", "bit": "bool",
        "tinyint": "smallint", "smallint": "smallint", "int": "int", "integer": "int",
        "bigint": "longint", "bigserial": "longint", "serial": "int",
        "decimal": "decimal", "numeric": "decimal", "dec": "decimal",
        "float": "float", "real": "float", "double": "float", "double precision": "float",
        "date": "date", "time": "time", "timestamp": "timestamp", "datetime": "timestamp",
        "blob": "blob", "bytea": "blob", "binary": "blob", "raw": "blob",
        "json": "json", "jsonb": "json",
    }
    return fam.get(t0, t0)


def main():
    ap = argparse.ArgumentParser(description="三方言迁移静态一致性校验")
    ap.add_argument("--migration-dir", required=True)
    ap.add_argument("--dialects", default="h2,dameng,postgresql")
    ap.add_argument("--report", default=None, help="可选：写出 JSON 报告路径")
    args = ap.parse_args()

    dialects = [d.strip() for d in args.dialects.split(",")]
    data = {}
    version_sets = {}
    for d in dialects:
        files = read_sql_files(args.migration_dir, d)
        version_sets[d] = [f for f, _ in files]
        data[d] = accumulate(files)

    errors = []
    warnings = []

    # 1. 版本文件 parity
    base = set(version_sets[dialects[0]])
    for d in dialects[1:]:
        missing = sorted(base - set(version_sets[d]))
        extra = sorted(set(version_sets[d]) - base)
        for m in missing:
            errors.append(f"[VERSION] {d} 缺少 {m}（其他方言存在）")
        for e in extra:
            errors.append(f"[VERSION] {d} 多出 {e}（其他方言无）")

    # 1b. 语句终结符 parity（Flyway 默认 ';' 分隔；缺终结符会导致多语句无法切分）
    for d in dialects:
        for fname, raw in read_sql_files(args.migration_dir, d):
            n_ct = len(re.findall(r"CREATE\s+TABLE", raw, re.IGNORECASE))
            n_semi = raw.count(";")
            n_slash = len(re.findall(r"^\s*/\s*$", raw, re.MULTILINE))
            if n_ct > 0 and n_ct > (n_semi + n_slash):
                warnings.append(
                    f"[TERMINATOR] {d}/{fname} 含 {n_ct} 个 CREATE TABLE 但仅 {n_semi} 个 ';'/{n_slash} 个 '/'，"
                    f"Flyway 默认分隔符将无法切分多语句（须在 {d} 实例复核/补终结符）"
                )

    # 2. 表存在 parity
    all_tables = set()
    for d in dialects:
        all_tables |= set(data[d].keys())
    for t in sorted(all_tables):
        present = [d for d in dialects if t in data[d]]
        if len(present) < len(dialects):
            missing = [d for d in dialects if t not in data[d]]
            errors.append(
                f"[TABLE] 表 `{t}` 缺失于: {', '.join(missing)} "
                f"（存在于: {', '.join(present)}）"
            )

    # 3. 列名 parity + 4. 类型族 parity
    for t in sorted(all_tables):
        if t not in data[dialects[0]]:
            continue
        ref_cols = set(data[dialects[0]][t].keys())
        for d in dialects[1:]:
            if t not in data[d]:
                continue
            d_cols = set(data[d][t].keys())
            for c in sorted(ref_cols - d_cols):
                errors.append(f"[COLUMN] 表 `{t}` 在 `{d}` 缺失列 `{c}`（{dialects[0]} 有）")
            for c in sorted(d_cols - ref_cols):
                errors.append(f"[COLUMN] 表 `{t}` 在 `{d}` 多出列 `{c}`（{dialects[0]} 无）")
        # 类型族软警告：每个（表, 列）只报一次（遍历所有方言取类型，按列去重）
        cols_all = set()
        for d in dialects:
            cols_all |= set(data.get(d, {}).get(t, {}).keys())
        for c in sorted(cols_all):
            fams = {}
            for d2 in dialects:
                if c in data.get(d2, {}).get(t, {}):
                    fams[d2] = type_family(data[d2][t][c])
            if len(set(fams.values())) > 1:
                detail = " / ".join(f"{k}={data[k][t][c]}" for k in fams)
                warnings.append(
                    f"[TYPE] 表 `{t}` 列 `{c}` 类型族不一致（{detail}）"
                )

    has_errors = len(errors) > 0
    print("=" * 64)
    print("三方言迁移静态一致性校验")
    print(f"方言: {', '.join(dialects)}")
    print(f"版本文件数: " + ", ".join(f"{d}={len(version_sets[d])}" for d in dialects))
    print(f"表总数(去重): {len(all_tables)}")
    print("-" * 64)
    if errors:
        print(f"❌ 结构性错误 {len(errors)} 处：")
        for e in errors:
            print("  " + e)
    else:
        print("✅ 无结构性错误（版本/表/列名三方言对齐）")
    if warnings:
        print(f"\n⚠️ 类型族软警告 {len(warnings)} 处（容忍已知方言映射，不阻断）：")
        for w in warnings:
            print("  " + w)
    print("=" * 64)

    if args.report:
        with open(args.report, "w", encoding="utf-8") as fh:
            json.dump(
                {
                    "dialects": dialects,
                    "version_counts": {d: len(version_sets[d]) for d in dialects},
                    "table_count": len(all_tables),
                    "errors": errors,
                    "warnings": warnings,
                },
                fh,
                ensure_ascii=False,
                indent=2,
            )
        print(f"\n报告已写出: {args.report}")

    sys.exit(1 if has_errors else 0)


if __name__ == "__main__":
    main()

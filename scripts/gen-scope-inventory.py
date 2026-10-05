#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
生成《需求交付基线 · 交付范围追溯清单》的三张表（§1 总览 / §2 全量矩阵 / §3 spec 覆盖缺口）。

为什么用脚本而不是手抄：追溯清单一旦靠人工维护，新增端点必然漏登记（本文 2026-10-06 修订前
只登记 37 条，而后端实际 288 个端点）。脚本从**代码与契约真源**反推，保证「实现 = 清单」。

数据来源（全部只读，不写任何文件）：
  1. 后端 `src/main/java/.../controller/*.java`    —— 端点、控制器、@RequireAuth 约束
  2. 前端 `docs/api/*.openapi.json`                —— 契约域归属（唯一契约真源）
  3. 后端 `openspec/specs/<capability>/spec.md`    —— 能力域 spec 是否提及该端点
  4. 前端 `src/` + `apps/`                         —— 消费模块（排除 *.spec.ts 与 src/mocks/）

用法（在 backend-scaffold 目录下）：
    python scripts/gen-scope-inventory.py            # 打印三张表（Markdown）
    python scripts/gen-scope-inventory.py --matrix   # 只打印 §2 全量矩阵

退出码恒为 0；本脚本是**审计工具**，不做门禁判定。
"""

import os
import re
import sys
import json
from collections import defaultdict

REPO_ROOT = os.path.abspath(os.path.join(os.path.dirname(os.path.abspath(__file__)), ".."))
FE_ROOT = os.path.abspath(os.path.join(REPO_ROOT, "..", "frontend-scaffold"))
CTRL_DIR = os.path.join(REPO_ROOT, "src", "main", "java", "com", "sinopec", "mmsecurity", "controller")
SPEC_DIR = os.path.join(REPO_ROOT, "openspec", "specs")
API_DIR = os.path.join(FE_ROOT, "docs", "api")

# JwtFilter 中真正免鉴权的业务端点（其余无 @RequireAuth 的端点仍被 JwtFilter 强制令牌）
JWT_WHITELIST = ("/api/v1/auth/login", "/api/v1/auth/refresh", "/api/v1/auth/logout")

MAPPING_RE = re.compile(r'@(Get|Post|Put|Delete|Patch|Request)Mapping(\((.*?)\))?\s*$')
METHOD_RE = re.compile(r'(?:public|protected)\s+[\w\.<>\[\],\s\?]+\s+(\w+)\s*\(')
WRITE_VERBS = ("POST", "PUT", "DELETE", "PATCH")


# --------------------------------------------------------------------------- 端点扫描
def parse_args(s):
    if not s:
        return ""
    m = re.search(r'(?:value|path)\s*=\s*(\{[^}]*\}|"[^"]*")', s)
    if m:
        raw = m.group(1)
        if raw.startswith("{"):
            first = re.search(r'"([^"]*)"', raw)
            return first.group(1) if first else ""
        return raw.strip('"')
    m = re.search(r'^\s*"([^"]*)"', s)
    return m.group(1) if m else ""


def parse_auth(ann_args):
    """@RequireAuth -> 展示用授权标签。"""
    if ann_args is None:
        return "登录"
    r = re.search(r'role\s*=\s*"([^"]+)"', ann_args)
    p = re.search(r'perm\s*=\s*"([^"]+)"', ann_args)
    if r:
        return "role:" + r.group(1)
    if p:
        return "perm:" + p.group(1)
    return "登录"


def scan_endpoints():
    rows = []
    for fn in sorted(os.listdir(CTRL_DIR)):
        if not fn.endswith(".java"):
            continue
        src = open(os.path.join(CTRL_DIR, fn), encoding="utf-8").read()
        lines = src.split("\n")
        cls = fn[:-5]

        # 类级 @RequireAuth：必须取「类声明行之前」的注解块，
        # 否则会把第一个方法级注解误当类级默认（读端点被错标成写权限）。
        cls_auth = ""
        cls_line = next((i for i, ln in enumerate(lines)
                         if re.search(r'\bclass\s+' + cls + r'\b', ln)), None)
        if cls_line is not None:
            for ln in lines[max(0, cls_line - 10):cls_line]:
                m = re.search(r'@RequireAuth(\((.*?)\))?\s*$', ln)
                if m:
                    cls_auth = parse_auth(m.group(2))
                    break

        base, class_map_line = "", -1
        for i, ln in enumerate(lines):
            if re.match(r"\s*@RestController", ln):
                for j in range(i, min(i + 6, len(lines))):
                    mm = re.search(r'@RequestMapping\((.*?)\)\s*$', lines[j])
                    if mm:
                        base, class_map_line = parse_args(mm.group(1)), j
                        break
                break
        if not base:
            mm = re.search(r'@RequestMapping\(\s*(?:(?:value|path)\s*=\s*)?"([^"]*)"\s*\)', src)
            if mm:
                base = mm.group(1)

        i = 0
        while i < len(lines):
            m = MAPPING_RE.search(lines[i].rstrip())
            if m and i != class_map_line and not re.search(r"class\s+" + cls, lines[i]):
                http, args = m.group(1).upper(), (m.group(3) or "")
                sub = parse_args(args)
                auth, java_method = "", ""
                j = i + 1
                while j < len(lines) and j < i + 25:
                    nxt = lines[j]
                    am = re.search(r'@RequireAuth(\((.*?)\))?\s*$', nxt)
                    if am:
                        auth = parse_auth(am.group(2))
                    mm2 = METHOD_RE.search(nxt)
                    if mm2:
                        java_method = mm2.group(1)
                        break
                    if nxt.strip() == "}":
                        break
                    j += 1
                if http == "REQUEST":
                    rm = re.search(r"RequestMethod\.(\w+)", args)
                    http = (rm.group(1) if rm else "GET").upper()
                full = ((base + sub) if sub else base).replace("//", "/")
                eff = auth or cls_auth
                if not auth and not cls_auth:
                    eff = "公开" if full in JWT_WHITELIST else "登录"
                rows.append({"controller": cls, "http": http, "path": full,
                             "java": java_method, "auth": eff})
                i = j + 1
                continue
            i += 1
    return rows


# --------------------------------------------------------------------------- 契约域
def load_contracts():
    contracts = {}
    if not os.path.isdir(API_DIR):
        return contracts
    for fn in sorted(os.listdir(API_DIR)):
        if not fn.endswith(".openapi.json"):
            continue
        j = json.load(open(os.path.join(API_DIR, fn), encoding="utf-8"))
        base = ""
        for s in j.get("servers", []) or []:
            mm = re.search(r"(/api/v\d+)", s.get("url", ""))
            if mm:
                base = mm.group(1)
        entries = []
        for p, ops in (j.get("paths") or {}).items():
            for meth in ops:
                if meth.lower() in ("get", "post", "put", "delete", "patch"):
                    entries.append((meth.upper(), base + p))
        contracts[fn.replace(".openapi.json", "")] = entries
    return contracts


def path_regex(rel, fe_style):
    parts = re.split(r"(\{[^}]+\})", rel)
    out = ""
    for seg in parts:
        if seg.startswith("{"):
            # fe_style：前端可能是 ${id} / {id} / 拼接写法（'/x/' + id，字面量止于引号）
            out += (r'(?:\$\{[^}]*\}|\{[^}]+\}|[A-Za-z0-9_\-\.%]+|(?=[\'"`]))' if fe_style
                    else r'(?:\{[^}]+\}|[^/\s`\'"，。、）)]*)')
        else:
            out += re.escape(seg)
    return re.compile(out)


def map_contract(rows, contracts):
    hits = {}
    for r in rows:
        rel = r["path"].replace("/api/v1", "")
        pat = path_regex(rel, False)
        found = [n for n, es in contracts.items()
                 if any(m == r["http"] and pat.fullmatch(p.replace("/api/v1", "")) for m, p in es)]
        hits[r["http"] + " " + r["path"]] = sorted(set(found))
    return hits


# --------------------------------------------------------------------------- spec / 前端
def load_specs():
    specs = {}
    if not os.path.isdir(SPEC_DIR):
        return specs
    for d in sorted(os.listdir(SPEC_DIR)):
        p = os.path.join(SPEC_DIR, d, "spec.md")
        if os.path.exists(p):
            specs[d] = open(p, encoding="utf-8", errors="ignore").read()
    return specs


def map_spec(rows, specs):
    hits = {}
    for r in rows:
        pat = path_regex(r["path"].replace("/api/v1", ""), False)
        hits[r["http"] + " " + r["path"]] = [n for n, t in specs.items() if pat.search(t)]
    return hits


def load_fe_files():
    files = []
    for d in ("src", "apps"):
        base = os.path.join(FE_ROOT, d)
        if not os.path.isdir(base):
            continue
        for root, dirs, fns in os.walk(base):
            dirs[:] = [x for x in dirs if x not in ("node_modules", "dist", "coverage")]
            for fn in fns:
                if fn.endswith((".ts", ".vue", ".js")):
                    rel = os.path.relpath(os.path.join(root, fn), FE_ROOT).replace("\\", "/")
                    if "/dist/" in rel or rel.startswith("src/types/generated"):
                        continue
                    if ".spec." in rel or rel.startswith("src/mocks/"):
                        continue
                    files.append((rel, os.path.join(root, fn)))
    return files


def map_fe(rows, files):
    cache = {}
    for rel, p in files:
        try:
            cache[rel] = open(p, encoding="utf-8", errors="ignore").read()
        except Exception:
            cache[rel] = ""
    hits = {}
    for r in rows:
        pat = path_regex(r["path"].replace("/api/v1", ""), True)
        hits[r["http"] + " " + r["path"]] = [f for f, t in cache.items() if pat.search(t)]
    return hits


def fe_label(f):
    if f.startswith("src/services/"):
        return "services/" + f.split("/")[-1]
    if f.startswith("src/screen/"):
        return "screen/" + f[len("src/screen/"):]
    if f.startswith("apps/mgmt/"):
        return "mgmt/" + f[len("apps/mgmt/"):]
    if f.startswith("apps/mobile/"):
        return "mobile/" + f[len("apps/mobile/"):]
    if f.startswith("src/"):
        return "src/" + f[4:]
    return f


def fe_module(key, fe_hits):
    files = fe_hits.get(key) or []
    if not files:
        return "**⚠️ 前端零引用**"
    svc = sorted({fe_label(f) for f in files if f.startswith("src/services/")})
    others = []
    for f in files:
        if f.startswith("src/services/"):
            continue
        l = fe_label(f)
        if l not in others:
            others.append(l)
    parts = []
    if svc:
        parts.append("、".join(svc[:2]) + ("…" if len(svc) > 2 else ""))
    if others:
        parts.append("、".join(others[:2]) + ("…" if len(others) > 2 else ""))
    return " / ".join(parts)


# --------------------------------------------------------------------------- 输出
def main():
    only_matrix = "--matrix" in sys.argv
    only_gaps = "--gaps" in sys.argv
    rows = scan_endpoints()
    contracts = load_contracts()
    specs = load_specs()
    ct_hits = map_contract(rows, contracts)
    sp_hits = map_spec(rows, specs)
    fe_hits = map_fe(rows, load_fe_files())

    groups = defaultdict(list)
    for r in rows:
        groups[(ct_hits.get(r["http"] + " " + r["path"]) or ["—"])[0]].append(r)

    out = []
    if only_gaps:
        gap = defaultdict(lambda: [0, 0])
        for r in rows:
            d = (ct_hits.get(r["http"] + " " + r["path"]) or ["—"])[0]
            gap[d][0] += 1
            if not sp_hits.get(r["http"] + " " + r["path"]):
                gap[d][1] += 1
        out.append("| 契约域 | 端点 | 无 spec 引用 | 缺口率 |")
        out.append("| ------ | ---: | ----------: | -----: |")
        for d in sorted(gap, key=lambda x: -gap[x][1]):
            tot, miss = gap[d]
            if miss == 0:
                continue
            out.append(f"| {d} | {tot} | {miss} | {miss * 100 // tot}% |")
        t_all = sum(v[0] for v in gap.values())
        m_all = sum(v[1] for v in gap.values())
        out.append(f"| **合计** | **{t_all}** | **{m_all}** | **{m_all * 100 // t_all}%** |")
        out.append("")
        out.append("已完整覆盖（0 缺口）的域："
                   + "、".join(sorted(d for d in gap if gap[d][1] == 0)))
        print("\n".join(out))
        return

    if not only_matrix:
        tot_w = sum(1 for r in rows if r["http"] in WRITE_VERBS)
        out.append("| 契约域 | 端点 | 读 | 写 | 控制器 | 前端主 service |")
        out.append("| ------ | ---: | -: | -: | ------ | -------------- |")
        for dom in sorted(groups, key=lambda d: -len(groups[d])):
            rs = groups[dom]
            w = sum(1 for r in rs if r["http"] in WRITE_VERBS)
            ctrls = "、".join(sorted({r["controller"][:-10] for r in rs}))
            svc = set()
            for r in rs:
                for f in (fe_hits.get(r["http"] + " " + r["path"]) or []):
                    if f.startswith("src/services/"):
                        svc.add("services/" + f.split("/")[-1])
            out.append(f"| {dom} | {len(rs)} | {len(rs)-w} | {w} | {ctrls} "
                       f"| {'、'.join(sorted(svc)[:2]) or '—'} |")
        out.append(f"| **合计** | **{len(rows)}** | **{len(rows)-tot_w}** | **{tot_w}** "
                   f"| **{len({r['controller'] for r in rows})}** | — |")
        out.append("")

    idx = 0
    for dom in sorted(groups):
        rs = sorted(groups[dom], key=lambda r: (r["path"], r["http"]))
        out.append(f"### {dom}（{len(rs)}）")
        out.append("")
        out.append("| # | 方法 | 端点 | 控制器 | 能力域 spec | 鉴权 | 前端消费模块 |")
        out.append("| - | ---- | ---- | ------ | ----------- | ---- | ------------ |")
        for r in rs:
            idx += 1
            key = r["http"] + " " + r["path"]
            sp = sp_hits.get(key) or []
            sp_txt = ("、".join(sp[:2]) + ("…" if len(sp) > 2 else "")) if sp else "⚠️ 未归属"
            out.append(f"| {idx} | {r['http']} | `{r['path']}` | {r['controller'][:-10]} | {sp_txt} "
                       f"| {r['auth']} | {fe_module(key, fe_hits)} |")
        out.append("")
    print("\n".join(out))


if __name__ == "__main__":
    main()

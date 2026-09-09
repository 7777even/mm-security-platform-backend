# -*- coding: utf-8 -*-
"""全端点冒烟：登录换 JWT 后遍历所有数据端点，打印 HTTP 状态 + B3 包络 code + 数据摘要。

用途：联调阻塞诊断（例如"数据端点全 500"），以及切真实后端后的回归自检。
运行：python scripts/smoke-all-endpoints.py [base_url]
"""
import json
import sys
import urllib.error
import urllib.request

BASE = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8787/api/v1"
USER = "admin"
PWD = "admin@2026"

# (method, path) —— 覆盖 docs/api/*.openapi.json 声明的全部数据端点
ENDPOINTS = [
    ("GET", "/auth/me"),
    ("GET", "/auth/menus"),
    ("GET", "/dashboard/overview"),
    ("GET", "/dashboard/alarm-trend"),
    ("GET", "/dashboard/workstations"),
    ("GET", "/dashboard/risk-heatmap"),
    ("GET", "/alarms?page=1&size=5"),
    ("GET", "/devices?page=1&size=5"),
    ("GET", "/fire-alarms?page=1&size=5"),
    ("GET", "/hazards"),
    ("GET", "/monitoring/points"),
    ("GET", "/monitoring/alarms"),
    ("GET", "/facilities/detail?facilityCode=TK-001"),
    ("GET", "/map/alarms"),
    ("GET", "/map/devices"),
    ("GET", "/security/patrol-cameras"),
    ("GET", "/security/gate-controls"),
    ("GET", "/security/bollards"),
    ("GET", "/security/events"),
    ("GET", "/security/search/vehicle?keyword=A"),
    ("GET", "/security/search/person?keyword=A"),
    ("GET", "/emergency/strength"),
    ("GET", "/emergency/closed-cases"),
    ("GET", "/emergency/duty"),
    ("GET", "/emergency/phones"),
    ("GET", "/emergency/knowledge"),
]


def call(method, path, token=None, body=None):
    url = BASE + path
    data = json.dumps(body).encode("utf-8") if body is not None else None
    req = urllib.request.Request(url, data=data, method=method)
    req.add_header("Content-Type", "application/json; charset=utf-8")
    if token:
        req.add_header("Authorization", "Bearer " + token)
    try:
        with urllib.request.urlopen(req, timeout=15) as resp:
            return resp.status, resp.read().decode("utf-8", errors="replace")
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode("utf-8", errors="replace")
    except Exception as e:  # noqa: BLE001
        return -1, "{}: {}".format(type(e).__name__, e)


def summarize(raw):
    """提取 B3 包络 code/message 与 data 规模摘要。"""
    try:
        obj = json.loads(raw)
    except Exception:  # noqa: BLE001
        return "non-json: " + raw[:160].replace("\n", " ")
    code = obj.get("code")
    msg = obj.get("message")
    data = obj.get("data")
    if isinstance(data, list):
        shape = "list[{}]".format(len(data))
    elif isinstance(data, dict):
        if "records" in data and isinstance(data["records"], list):
            shape = "page(records={}, total={})".format(len(data["records"]), data.get("total"))
        elif "features" in data and isinstance(data["features"], list):
            shape = "geojson(features={})".format(len(data["features"]))
        else:
            shape = "obj(keys={})".format(len(data))
    elif data is None:
        shape = "null"
    else:
        shape = type(data).__name__
    return "code={} msg={} data={}".format(code, msg, shape)


def main():
    print("=" * 100)
    print("BASE =", BASE)
    print("=" * 100)

    st, raw = call("POST", "/auth/login", body={"username": USER, "password": PWD})
    print("[login] HTTP {} {}".format(st, summarize(raw)))
    token = None
    if st == 200:
        try:
            token = json.loads(raw)["data"]["accessToken"]
        except Exception as e:  # noqa: BLE001
            print("  !! 无法解析 accessToken:", e, raw[:300])
    if not token:
        print("!! 登录失败，后续端点将以匿名身份调用（预期 401）")

    ok, fail = [], []
    print("-" * 100)
    for method, path in ENDPOINTS:
        st, raw = call(method, path, token=token)
        flag = "OK  " if st == 200 else "FAIL"
        line = "[{}] {:<4} HTTP {:<4} {:<42} {}".format(flag, method, st, path, summarize(raw))
        print(line)
        if st == 200:
            ok.append(path)
        else:
            fail.append((path, st, raw[:400]))

    print("=" * 100)
    print("PASS {} / {}   FAIL {}".format(len(ok), len(ENDPOINTS), len(fail)))
    if fail:
        print("-" * 100)
        print("失败明细：")
        for path, st, body in fail:
            print("  {} -> HTTP {}".format(path, st))
            print("     {}".format(body.replace("\n", " ")[:380]))
    return 1 if fail else 0


if __name__ == "__main__":
    sys.exit(main())

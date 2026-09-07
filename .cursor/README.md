# `.cursor/` 适配层与 opsx-* 命令安装说明

本目录让 **Cursor / Claude Code / Codex / WorkBuddy** 等任意 AI 编码工具，都能读到根 `AGENTS.md` 定义的那套工程化约束（L0–L4 分级、openspec 唯一规格源、跨库契约真源、验证矩阵、安全与数据库红线、提交规范）。

```
.cursor/
├── README.md                     ← 本文件：安装与排错说明
├── rules/
│   ├── backend-scaffold-core.mdc      # 入口/分级/唯一规格源/跨库契约/验证矩阵/提交/权限边界
│   ├── api-contract.mdc               # 零下行控制/B3 Result 包络/20位MDM/HMAC/无状态JWT/错误码分段/四同步
│   └── backend-engineering.mdc        # 分层职责/过滤器链/密钥与日志脱敏/DB 变更/环境差异/Windows 工程注记
├── commands/                    # 显式触发：用户在 Cursor 里敲 /opsx:<name> 才跑
│   ├── opsx-propose.md           # 创建 change 并生成 proposal/design/tasks/specs-delta 四件套
│   ├── opsx-apply.md             # 按 tasks.md 逐项实施（TDD + 验收）
│   ├── opsx-archive.md           # 验收通过后归档到 openspec/archive
│   ├── opsx-explore.md           # 只读探索模式（只思考不写码）
│   ├── opsx-sync.md              # 把 delta spec 合并回 openspec/specs 主规格
│   └── opsx-update.md            # 修订规划产物（proposal/design/tasks）
└── skills/                      # 自动触发：AI 检测到意图匹配 description 即主动调用
    ├── openspec-propose/SKILL.md       # = opsx-propose 的技能版
    ├── openspec-apply-change/SKILL.md  # = opsx-apply 的技能版（含 L3/L4 人工确认闸）
    ├── openspec-archive-change/SKILL.md# = opsx-archive 的技能版
    ├── openspec-explore/SKILL.md       # = opsx-explore 的技能版
    ├── openspec-sync-specs/SKILL.md    # = opsx-sync 的技能版
    └── openspec-update-change/SKILL.md # = opsx-update 的技能版
```

> 这些 rules / commands / skills 都是**对根 `AGENTS.md` 的镜像**，不是第二套规范。若 AGENTS.md 改了，这里要同步改（见末尾"一致性约定"）。
> 本库与前端库 `frontend-scaffold` 的 `.cursor/` 结构同构，章节编号（§1 分级、§2 验证矩阵、§3 契约、§4 记录闭环）保持一致，便于跨库复用同一套 opsx 流程。

---

## 一、opsx-* 命令依赖什么

`opsx-*` 命令是对 **OpenSpec CLI** 的封装，命令体里调用的是裸二进制 `openspec`（例如 `openspec list`、`openspec validate`、`openspec archive`）。

**前置条件：本机必须能直接执行 `openspec` 命令。**

- 没装 → 任何 `/opsx:` 命令都会报 `openspec: command not found`。
- 装了但不在仓库根目录运行 → 读不到 `openspec/config.yaml`，会报 schema / 找不到 changes 目录。

---

## 二、安装 OpenSpec CLI

### 方式 A（推荐）：全局安装

```bash
npm install -g @fission-ai/openspec
# 或 pnpm add -g @fission-ai/openspec
```

### 方式 B：不全局安装，用 npx 包一层

命令体里的 `openspec` 改成 `npx @fission-ai/openspec`（首次会临时下载）：

```bash
npx @fission-ai/openspec validate --strict --change "<change-id>"
```

### 版本说明

- 当前最新：`@fission-ai/openspec@1.12.0`（bin 名 `openspec`）。
- 本工作区已预装 `openspec` 于 `C:\nvm4w\nodejs\openspec`，版本 `1.5.0`，可直接使用。
- 团队统一建议 pin 一个版本，避免不同人 CLI 版本差异导致 `openspec validate` 规则不一致。

---

## 三、验证安装

```bash
openspec --version          # 应输出版号，而不是 command not found

cd <仓库根>/backend-scaffold
openspec list               # 列出当前 active changes
openspec list --specs       # 列出稳定规格
```

能正常列出，说明 CLI 与本仓库 `openspec/config.yaml` 已对上。

---

## 四、使用前提（必读）

1. **从仓库根目录运行**：`openspec` 以"当前工作目录"定位 `openspec/` 与 `config.yaml`。执行 `/opsx:*` 时，确保打开的工作区根就是 `backend-scaffold/`（含 `AGENTS.md` 与 `openspec/` 的那一层）。
2. **只用于 L3 / L4 改动**：L0/L1/L2 不要走这套（直接改 + 跑最小验证矩阵即可）。
3. **命令是脚手架，不是规范**：命令帮你建目录、填四件套、跑校验，但最终内容（proposal 措辞、tasks 拆分、验收标准）仍需人审，且必须回到 `AGENTS.md` 的红线。
4. **跨库契约**：后端改接口时，四件套里必须写明"前端契约文件同步"这件事；本库不复制第二份 OpenAPI 主契约。

---

## 四-2、技能（skills）与命令（commands）的关系

| 形式                              | 目录                | 触发方式                                           | 适用场景                                             |
| --------------------------------- | ------------------- | -------------------------------------------------- | ---------------------------------------------------- |
| 命令 `commands/opsx-*.md`         | `.cursor/commands/` | 用户在 Cursor 里**显式敲** `/opsx:propose` 等      | 想手动点名执行某一步                                 |
| 技能 `skills/openspec-*/SKILL.md` | `.cursor/skills/`   | AI 检测到**意图匹配** `description` 时**自动调用** | 用户说"帮我提个变更""把这个 change 归档了"等自然语言 |

- 技能与命令内容保持一致（同一流程、同一 CLI 子命令）。改其一要同步改其二，避免漂移。
- 技能里额外固化了两条硬约束：
  1. **L3/L4 人工确认闸**：`openspec-apply-change` 在动代码前必须先确认用户已批准该 change。
  2. **doc-only 用 `--skip-specs`**：`openspec-archive-change` 对 `chore`/`docs` 类无 spec 影响的 change 归档时跳过规格合并。

---

## 五、常见排错

| 现象                            | 原因                                | 处理                                                              |
| ------------------------------- | ----------------------------------- | ----------------------------------------------------------------- |
| `openspec: command not found`   | CLI 未装 / 未进 PATH                | 按"二"安装；或把命令体 `openspec` 换成 `npx @fission-ai/openspec` |
| `No changes found` / 找不到目录 | 不在仓库根目录运行                  | `cd` 到 `backend-scaffold/` 再执行                                |
| `validate` 报 schema 错误       | CLI 版本与仓库 `config.yaml` 不兼容 | 统一 pin 到同一版本（建议 1.12.0）                                |
| `./mvnw` 起不来                 | 用了裸 `mvn` 或 JDK 版本不对        | 用仓库自带 `./mvnw`，确认 JDK 17                                  |
| 接口改了前端没跟上              | 漏走跨库四同步                      | 同步 `frontend-scaffold/docs/api/*.openapi.json` 并重生成类型     |

---

## 六、一致性约定（维护者必看）

- `CLAUDE.md`、`.cursor/rules/*.mdc` 是根 `AGENTS.md` 的**投影**，不是独立规范。
- 改 `AGENTS.md`（尤其 §1 分级、§3 契约、§6 红线、§8 L4 门禁、§11 跨库协作）后，**必须同步**更新本目录的对应 rules，否则跨工具行为会漂移。
- `opsx-*` 命令体跟随 OpenSpec CLI 官方子命令；CLI 升级改名时同步改这里。
- `skills/openspec-*/SKILL.md` 与 `commands/opsx-*.md` 是同一流程的两种触发形态，改其一须同步其二。
- 前后端两库的 `.cursor/` 结构同构，规则**内容不互相覆盖**：契约真源在前端库，后端库只做实现映射与同步纪律。
- 不要把业务规则写进 `.cursor/` 而绕过 `AGENTS.md`——单一真源永远在根 `AGENTS.md`。

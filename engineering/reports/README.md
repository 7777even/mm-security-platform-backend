# engineering/reports/ — 阶段简报（后端）

面向**干系人**（甲方 / 团队 / 双端）的阶段进展简报。与 `qa/`、`retro/` 的区别：

| 目录 | 面向 | 回答的问题 |
| --- | --- | --- |
| `qa/` | 工程内部 | 「这个变更验证了什么、跑了什么命令、还有哪些没跑」 |
| `retro/` | 工程内部 | 「这次哪里做得好、哪里出问题、怎么改」 |
| **`reports/`** | **干系人** | **「本阶段交付了什么、判据达标了没、风险在哪、下一步是什么」** |

## 写法

1. 从 `../../templates/_stage_report_template.md` 复制填充。
2. 落点命名：`YYYY-MM-DD-stage-<n>-report.md`（例 `2026-10-08-stage-7-report.md`）。
3. **严格对照** `../architecture/roadmap.md` 的阶段完成判据逐条打勾，**用证据说话**（命令、数字、链接），不用形容词。
4. 数字以**当次实跑**为准：后端单测跑 `mvn -s ci-settings.xml test` 取 surefire 汇总，契约对拍跑 `node scripts/check-api-contract.mjs --strict`，不要照抄历史数字。

## 纪律

- 不写第二套任务：本目录只做**汇报**，实施任务永远以 `openspec/changes/<name>/tasks.md` 为准。
- 未执行项必须列出（例：达梦/PG 未实跑、IT 因无 Docker 未跑、甲方《功能项清单》未到导致需求追溯挂账），**禁止把未执行写成通过**。
- 阶段简报不承载长期知识：随阶段产生的稳定事实应回写 `docs/`（尤其 `docs/system-facts.md`）。

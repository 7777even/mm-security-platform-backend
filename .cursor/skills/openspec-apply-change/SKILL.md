---
name: openspec-apply-change
description: Implement the tasks of an OpenSpec change against the codebase. Use when the user wants to start, continue, or work through a confirmed change's tasks. Only invoke after the change proposal has been human-confirmed (L3/L4 gate).
allowed-tools: Bash(openspec:*)
license: MIT
compatibility: Requires openspec CLI (this repo pins @fission-ai/openspec; bare `openspec` must be on PATH — see ../README.md).
metadata:
  author: backend-scaffold
  version: '1.0'
---

Implement tasks from an OpenSpec change.

> Companion slash command: `/opsx:apply` (see ../commands/opsx-apply.md). Single source of truth: `AGENTS.md` §1 (L0–L4) and §4 (工程记录闭环). This skill is the Cursor auto-trigger projection.

This repo uses the **local `openspec/` root** (no stores). Omit `--store`.

**HUMAN-CONFIRMATION GATE (critical):** Per AGENTS.md §1.1, L3/L4 changes require explicit human confirmation of the proposal _before_ any code is written. Before editing files, confirm the user approved this change (e.g. "go ahead", "implement it", "approved"). If not confirmed, STOP and ask — do not implement on a merely-proposed change.

**Input**: Optionally a change name. If omitted, infer from context; if ambiguous, run `openspec list --json` and use **AskUserQuestion** to pick.

**Steps**

1. **Select the change** — announce "Using change: <name>" and how to override (`/opsx:apply <other>`).

2. **Check status / schema**

   ```bash
   openspec status --change "<name>" --json
   ```

   Note `schemaName` (e.g. "spec-driven") and which artifact holds tasks (typically `tasks`).

3. **Get apply instructions**

   ```bash
   openspec instructions apply --change "<name>" --json
   ```

   Returns `contextFiles` (concrete paths: proposal/specs/design/tasks for spec-driven), progress, and task list. Handle states: `blocked` → suggest revising artifacts; `all_done` → suggest archive; otherwise proceed.

4. **Read context files** — read every path under `contextFiles` before coding.

5. **Show progress** — schema, "N/M tasks complete", remaining overview.

6. **Implement tasks (loop until done/blocked)**

   For each pending task: show it, make minimal scoped code changes, mark `- [ ]` → `- [x]` in `tasks.md` immediately after. Keep changes focused to the task.

   Pause if: task unclear (ask), design issue surfaced (suggest updating artifacts), error/blocker (report and wait), or user interrupts.

7. **On completion/pause, show status** — tasks done this session, overall "N/M", suggest archive if all done.

**Output during implementation**

```
## Implementing: <change-name> (schema: <schema-name>)
Working on task 3/7: <task>
✓ Task complete
...
```

**Guardrails**

- Keep going until done or blocked.
- Always read context files first.
- If task ambiguous → pause and ask.
- Keep code changes minimal and scoped to each task.
- Update task checkbox immediately after completing each task.
- Pause on errors/blockers/unclear requirements — don't guess.
- Use `contextFiles` from CLI, don't assume file names.
- **Never implement a change that has not been human-confirmed** (L3/L4 gate).
- Respect AGENTS.md red lines while coding: zero downlink control (HardControlInterceptor), B3 Result envelope, 20-bit MDM device codes, HMAC replay protection, stateless JWT, layered packages. Sync frontend-scaffold/docs/api/*.openapi.json in the same delivery when the contract changes.

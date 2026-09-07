---
name: openspec-archive-change
description: Archive a completed OpenSpec change and fold its delta specs into the main specs. Use when implementation is done and the user wants to finalize/archive a change. For doc-only or tooling changes, syncing specs is skipped.
allowed-tools: Bash(openspec:*)
license: MIT
compatibility: Requires openspec CLI (this repo pins @fission-ai/openspec; bare `openspec` must be on PATH — see ../README.md).
metadata:
  author: backend-scaffold
  version: '1.0'
---

Archive a completed change and update main specs.

> Companion slash command: `/opsx:archive` (see ../commands/opsx-archive.md). Single source of truth: `AGENTS.md` §1 and §4. This skill is the Cursor auto-trigger projection.

This repo uses the **local `openspec/` root** (no stores). Omit `--store`.

**Input**: Optionally a change name. If omitted/inferable-ambiguous, run `openspec list --json` and use **AskUserQuestion** to pick. Show only active (non-archived) changes. **Do NOT auto-select — let the user choose.**

**Steps**

1. **Confirm selection** (if not provided) via `openspec list --json` + AskUserQuestion.

2. **Check completion**

   ```bash
   openspec status --change "<name>" --json
   ```

   - If artifacts incomplete: warn, confirm with user before proceeding.
   - Read `tasks.md`; count `- [ ]` vs `- [x]`. If incomplete tasks: warn, confirm before proceeding.

3. **Decide spec sync**

   Inspect delta specs under `openspec/changes/<name>/specs/`:
   - **None** → archive directly (no sync needed).
   - **Present** → the CLI `archive` will attempt to fold them into `openspec/specs/`. Show a brief summary of what would sync, then proceed.

4. **Perform the archive**

   For normal changes (behavioral/docs that affect specs):

   ```bash
   openspec archive "<name>"
   ```

   For **doc-only / tooling / chore** changes with no spec impact (e.g. `chore`/`docs` scope, no delta specs), skip the spec update:

   ```bash
   openspec archive "<name>" --skip-specs
   ```

   Respond to any confirmation prompts, or pass `-y` only after the user has explicitly approved archiving.

5. **Show summary** — change name, schema, archive location, spec-sync status (synced / skipped / none), and any warnings (incomplete artifacts/tasks).

**Output On Success**

```
## Archive Complete
**Change:** <change-name>
**Archived to:** openspec/archive/<date>-<name>/
**Specs:** ✓ Synced to main specs (or "skipped — doc-only" / "none")
```

**Guardrails**

- Always let the user pick the change if not named.
- Use `openspec status --json` for completion checking; warn (don't block) on incomplete artifacts/tasks — confirm and proceed.
- Prefer the CLI `archive` (it handles the move + spec fold) over manual `mv` to avoid drift.
- Use `--skip-specs` for doc-only/tooling changes.
- Show a clear summary of what happened.

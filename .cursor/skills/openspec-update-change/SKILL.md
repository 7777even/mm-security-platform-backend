---
name: openspec-update-change
description: Update an OpenSpec change by revising its existing planning artifacts and keeping them coherent with one another. Use when the user wants to revise a change's plan, fold new decisions in, or reconcile artifacts after an edit. Never edits code.
allowed-tools: Bash(openspec:*)
license: MIT
compatibility: Requires openspec CLI (this repo pins @fission-ai/openspec; bare `openspec` must be on PATH — see ../README.md).
metadata:
  author: backend-scaffold
  version: '1.0'
---

Revise a change's existing planning artifacts and keep them coherent. **Never edit code.**

> Companion slash command: `/opsx:update` (see ../commands/opsx-update.md). Single source of truth: `AGENTS.md` §4. This skill is the Cursor auto-trigger projection.

This repo uses the **local `openspec/` root** (no stores). Omit `--store`.

**Input**: Optionally a change name. If omitted/inferable-ambiguous, run `openspec list --json` (sorted by most-recently-modified) and use **AskUserQuestion** to pick, marking the most recent as "(Recommended)". **Do NOT auto-select — let the user choose.**

**Steps**

1. **Get the change's artifacts**

   ```bash
   openspec status --change "<name>" --json
   ```

   Parse `schemaName`, `artifacts` (status), `isComplete`, `changeRoot`, `artifactPaths`, `actionContext`. Use the reported artifact ids/paths — never hardcode names. Edit only `artifactPaths.<id>.existingOutputPaths` (concrete files; for glob artifacts these are glob-expanded, NOT the pattern in `resolvedOutputPath`).

2. **Understand the request**
   - Specific revision ("design now uses X") → start there.
   - Bare "update"/"make coherent" → coherence review: read artifacts, check for contradictions/gaps/duplication.

3. **Read and reconcile**
   - Read the touched artifact + the change's other artifacts.
   - Apply the edit; then check EVERY other artifact against it in any direction (a later-artifact edit may require revising an earlier one). Note inconsistencies.
   - Revise only files that already exist (`existingOutputPaths`). Don't create new artifacts or invent files under glob artifacts — point the user to `/opsx:propose`/propose skill to create them.
   - If already coherent, say so and make no edits.

4. **Confirm and apply, one artifact at a time**
   - Show each proposed revision + why; write only after the user confirms.
   - If rejected, leave that artifact unchanged.
   - For a substantial rewrite, first fetch rules/template:
     ```bash
     openspec instructions <artifact-id> --change "<name>" --json
     ```

5. **Point to next step (guidance only — NEVER act on it)**
   - Missing artifacts → suggest propose skill to create them.
   - Already implemented → code may diverge from plan; suggest apply skill.
   - Done + implemented → suggest archive skill.

**Output**: which artifacts were revised (and which proposed revisions were rejected), anything deferred, and the recommended next step.

**Guardrails**

- Planning artifacts only — NEVER edit implementation code. If the revised plan implies code changes, stop and point to the apply skill.
- Use artifact ids/paths from `openspec status`; never branch on hardcoded names.
- Edit only concrete `existingOutputPaths`; never write to a glob `resolvedOutputPath`.
- Do not advance the build frontier (no new artifacts/files) — that's propose's job.
- Confirm every edit with the user before writing.
- If the request changes the change's _intent_ rather than refining it, recommend starting fresh with a new propose.

---
name: openspec-sync-specs
description: Sync delta specs from an OpenSpec change into the main specs without archiving. Use when the user wants to update main specs with a change's delta spec (requirements added/modified/removed/renamed) while keeping the change active.
allowed-tools: Bash(openspec:*)
license: MIT
compatibility: Requires openspec CLI (this repo pins @fission-ai/openspec; bare `openspec` must be on PATH — see ../README.md).
metadata:
  author: backend-scaffold
  version: '1.0'
---

Sync delta specs from a change to main specs.

This is an **agent-driven** operation — you read delta specs and directly edit main specs to apply changes, enabling intelligent merging (e.g. add one scenario without copying the whole requirement).

> Companion slash command: `/opsx:sync` (see ../commands/opsx-sync.md). Single source of truth: `AGENTS.md` §4. This skill is the Cursor auto-trigger projection.

This repo uses the **local `openspec/` root** (no stores). Omit `--store`.

**Input**: Optionally a change name. If omitted/inferable-ambiguous, run `openspec list --json` and use **AskUserQuestion** to pick. Show only changes that have delta specs under `specs/`. **Do NOT auto-select — let the user choose.**

**Steps**

1. **Resolve context**

   ```bash
   openspec status --change "<name>" --json
   ```

   Use `artifactPaths.specs.existingOutputPaths` as the delta spec file list. If none, inform and stop.

2. **For each delta spec, apply changes to main specs**

   a. Read the delta spec. Sections: `## ADDED Requirements`, `## MODIFIED Requirements`, `## REMOVED Requirements`, `## RENAMED Requirements` (FROM:/TO:).
   b. Read the main spec at `openspec/specs/<capability>/spec.md` (may not exist yet).
   c. Apply intelligently:
   - **ADDED**: add if absent; if present, update to match (treat as implicit MODIFIED).
   - **MODIFIED**: find requirement, apply changes (add/modify scenarios, change description); preserve content not in the delta.
   - **REMOVED**: delete the whole requirement block.
   - **RENAMED**: rename FROM → TO.
     d. If capability has no main spec yet: create `openspec/specs/<capability>/spec.md` with a Purpose (brief/TBD) and the ADDED requirements.

3. **Show summary** — which capabilities updated, what changed (added/modified/removed/renamed).

**Key principle: intelligent merging** — the delta is _intent_, not a wholesale replacement. Partial updates are fine (add one scenario under MODIFIED without copying existing ones). The operation should be idempotent.

**Guardrails**

- Read both delta and main specs before editing.
- Preserve existing content not mentioned in the delta.
- Show what you're changing as you go; ask if unclear.
- Don't touch implementation code — specs only.

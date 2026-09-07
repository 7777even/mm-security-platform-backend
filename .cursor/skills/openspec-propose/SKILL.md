---
name: openspec-propose
description: Propose a new OpenSpec change and generate all planning artifacts (proposal, design, specs, tasks) in one step. Use when the user describes a feature, fix, or behavioral change they want built and wants a ready-to-confirm change rather than ad-hoc edits.
allowed-tools: Bash(openspec:*)
license: MIT
compatibility: Requires openspec CLI (this repo pins @fission-ai/openspec; bare `openspec` must be on PATH — see ../README.md).
metadata:
  author: backend-scaffold
  version: '1.0'
---

Propose a new change — create the change and generate all artifacts in one step.

> Companion slash command: `/opsx:propose` (see ../commands/opsx-propose.md). The single source of truth for the OpenSpec workflow in this repo remains `AGENTS.md` §1 (L0–L4) and §4 (工程记录闭环). This skill is the Cursor auto-trigger projection of that workflow.

This repo uses the **local `openspec/` root** (no stores). Omit `--store` on every command.

**Scope gate (read AGENTS.md §1 first):** Only L3 (business-capability change) and L4 (high-risk) work uses OpenSpec. L0/L1/L2 never create a change. If the request is a doc/style/lint-only tweak under 3 files, do NOT open a change — just do it.

**Input**: A change name in kebab-case (e.g. `add-user-auth`) OR a description of what to build.

**Steps**

1. **If no clear input, ask what to build**

   Use **AskUserQuestion** (open-ended, no presets):

   > "What change do you want to work on? Describe what you want to build or fix."

   Derive a kebab-case name from the description. Do NOT proceed without understanding intent.

2. **Create the change directory**

   ```bash
   openspec new change "<name>"
   ```

   This scaffolds `openspec/changes/<name>/` with `.openspec.yaml`.

3. **Get the artifact build order**

   ```bash
   openspec status --change "<name>" --json
   ```

   Parse JSON for: `applyRequires` (artifacts needed before implementation, e.g. `["tasks"]`), `artifacts` (id/status/dependencies), `changeRoot`, `artifactPaths`, `actionContext`.

4. **Create artifacts in dependency order until apply-ready**

   Use **TodoWrite** to track progress. For each `ready` artifact:

   a. Fetch instructions:

   ```bash
   openspec instructions <artifact-id> --change "<name>" --json
   ```

   The JSON gives `context` (constraints for you — never copy into the file), `rules`, `template` (structure), `instruction`, `resolvedOutputPath`, `dependencies`.
   b. Read completed dependency files for context.
   c. Write the artifact to `resolvedOutputPath` using `template` as structure.
   d. Re-run `openspec status --change "<name>" --json`; stop when every `applyRequires` artifact is `done`.

   If an artifact needs clarification, use **AskUserQuestion**, then continue.

5. **Show final status**

   ```bash
   openspec status --change "<name>"
   ```

**Output**

Summarize: change name + location, artifacts created (proposal/design/specs/tasks), and "All artifacts created — ready for human confirmation before implementation." Then prompt: "Review the proposal; say go ahead (or run `/opsx:apply`) to implement."

**Artifact guidelines**

- Follow each artifact's `instruction` from `openspec instructions`.
- `context`/`rules` are constraints for YOU, not file content — never paste `<context>`/`<rules>` blocks into artifacts.
- Read dependency artifacts before creating new ones.

**Guardrails**

- Create ALL `applyRequires` artifacts (per schema).
- Always read dependency artifacts first.
- Prefer reasonable decisions to keep momentum; ask only when critically unclear.
- If a change with that name exists, ask whether to continue or start fresh.
- Verify each artifact file exists after writing before proceeding.
- **Do not begin implementation** from this skill — proposing is planning only. Implementation requires the user's explicit confirmation (L3/L4 gate, AGENTS.md §1.1).

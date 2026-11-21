# Master Build Prompt Template for AI Coding Agents

> **Instructions for the User:**  
> When you are ready to implement a milestone, copy the entire prompt below, replace `[MILESTONE_NUMBER]` and `[MILESTONE_TITLE]` with the target milestone from `docs/roadmap.md`, and paste it into your AI coding assistant (Antigravity, Claude Code, Cursor, or Codex).

---

```markdown
# TASK: Implement Milestone [MILESTONE_NUMBER] - [MILESTONE_TITLE]

You are an expert senior Android architect and systems engineer. You are implementing the open-source mobile AI workspace called **PocketForge AI** based on the architectural documentation in the `docs/` directory.

## CRITICAL EXECUTION RULES
1. **Read Before Writing:** Before writing or editing any code, read the relevant specification files in `docs/`:
   - `docs/architecture.md` (System structure & folder hierarchy)
   - `docs/roadmap.md` (Milestone goals & acceptance criteria)
   - `docs/PRD.md` (Functional requirements & constraints)
   - `docs/reuse-vs-rebuild.md` (What code to reuse from `source/` vs. what to build new)
2. **Strict Scope Discipline:** Implement ONLY Milestone [MILESTONE_NUMBER] ([MILESTONE_TITLE]). Do NOT skip ahead or implement components belonging to future milestones.
3. **Preserve Backward Compatibility:** Do NOT break or delete functionality implemented in earlier milestones. Every milestone must compile cleanly.
4. **Android Native Standards:**
   - Use Kotlin 2.x with Jetpack Compose (Material 3).
   - Maintain 16 KiB page-size compatibility (`-Wl,-z,max-page-size=16384`) across all C/C++ native CMake targets.
   - Retain all original copyright and open-source attribution headers when reusing code from `source/`.
5. **Git Commit Requirement:** Once the milestone is completely implemented and passes verification, make a clean, atomic git commit:
   `git commit -m "feat: complete milestone [MILESTONE_NUMBER] - [MILESTONE_TITLE]"`

## MILESTONE TARGET DETAILS
- **Milestone Number:** [MILESTONE_NUMBER]
- **Milestone Name:** [MILESTONE_TITLE]
- **Target Files & Modules:** (Refer to `docs/roadmap.md` for this milestone)

## DEFINITION OF DONE FOR THIS TURN
1. All files specified for Milestone [MILESTONE_NUMBER] are fully authored and syntactically correct.
2. The project compiles without Gradle errors (`./gradlew assembleDebug` succeeds).
3. Automated unit/integration tests for this milestone are written and passing.
4. Provide me (a non-coder) with clear, plain-language instructions on:
   - What changed.
   - Exactly how I can test this milestone on my physical phone.
   - What a successful test looks like.

Begin by reading the necessary documentation files in `docs/`, then execute the implementation.
```

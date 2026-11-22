# PocketForge AI: Mockup vs. Real Compose Render Comparison

**Date:** November 22, 2026  
**Document Purpose:** Transparent, honest technical comparison between the HTML design mockup (`docs/mockups/index.html`) and the actual Android Jetpack Compose renders (`com.jarves.mh.ui.v2`).

---

## 1. How Previous Gallery Images Were Produced (Honest Disclosure)

The initial preview images inside `docs/ui-review/` and the CI artifact `ui-gallery` were produced by `scripts/generate_ui_gallery.py`. 
- **Method Used:** A local Python HTTP server served `docs/mockups/index.html`, and a headless instance of Google Chrome (`chrome.exe --headless=new`) captured snapshots using query parameters (`?screen=...&theme=...&lang=...`).
- **Limitation:** These captured the HTML/CSS mockup with a simulated CSS phone bezel and camera notch. They were **design preview screenshots**, not real Android runtime renders.
- **Remediation:** We have integrated **Roborazzi** (`io.github.takahirom.roborazzi`) with **Robolectric** (`org.robolectric:4.14.1`) to execute native Android layoutlib/Skia rendering on the JVM. The resulting `real_*.png` images are pixel-exact Android renders of the actual `@Composable` code without fake frames or bezels.

---

## 2. Dependencies Added & Production Impact

To generate real Compose screenshot renders without requiring a physical device or emulator, the following dependencies were added to `app/build.gradle.kts`:

| Dependency | Purpose | Scope | APK Size Impact |
| :--- | :--- | :--- | :---: |
| `io.github.takahirom.roborazzi:1.44.0` | Gradle plugin & Compose snapshot driver | `testImplementation` | **0 KB** (Excluded from APK) |
| `io.github.takahirom.roborazzi:roborazzi-compose:1.44.0` | Compose tree capture adapter | `testImplementation` | **0 KB** (Excluded from APK) |
| `org.robolectric:robolectric:4.14.1` | Android framework emulation & Skia graphics | `testImplementation` | **0 KB** (Excluded from APK) |
| `androidx.compose.ui:ui-test-junit4` | Compose UI test harness | `testImplementation` | **0 KB** (Excluded from APK) |

> [!NOTE]
> All screenshot dependencies are strictly scoped to `testImplementation`. **Zero bytes** are added to the final debug or release APKs.

---

## 3. Side-by-Side Screen Comparison & Concrete Differences

### A. Home Dashboard (`HomeScreen.kt` vs. `#screen-home`)

| Visual Aspect | HTML Design Mockup (`#screen-home`) | Real Jetpack Compose Render (`HomeScreen.kt`) | Technical Difference |
| :--- | :--- | :--- | :--- |
| **Bezel & Viewport** | Simulated CSS phone bezel with camera notch | Real 390x844 dp Android viewport | Compose renders full edge-to-edge content without decorative CSS borders. |
| **Hero CTA Card** | CSS `linear-gradient(135deg, #6366F1, #06B6D4)` | Compose `Brush.horizontalGradient(listOf(primary, indigoDark, accent))` | Compose gradient renders with smooth hardware color interpolation. |
| **Typography** | Browser WebFont (Inter + system fallback) | Android native typography engine (Roboto / Noto Sans Bengali) | Android font rendering features native hinting, font metrics, and anti-aliasing. |
| **Icons** | SVG sprite `<use href="#ph-...">` | Android `VectorDrawable` parsed via `PhosphorIcon.kt` | Native vector rendering using hardware canvas. |
| **Classic UI Fallback** | Not present in original mockup | Prominent transition card: "Switch to Classic Mobile Harness UI" | Added in Compose to ensure 100% feature parity during migration. |

---

### B. Setup Wizard (`SetupWizardScreen.kt` vs. `screen-setup-*`)

| Visual Aspect | HTML Design Mockup (`screen-setup-*`) | Real Jetpack Compose Render (`SetupWizardScreen.kt`) | Technical Difference |
| :--- | :--- | :--- | :--- |
| **Progress Indicator** | Static CSS progress bar | Material3 `LinearProgressIndicator` | Native Compose animated progress tracking. |
| **Selection Cards** | CSS `:hover` border highlights | Compose `Surface` with `BorderStroke(1.dp, border)` & `clickable` ripple | Android Material3 touch ripple semantics and accessibility focus targets. |
| **Language Switch** | Mockup toggles via JS dictionary replace | Real Compose state mutation (`LocaleHelper.applyLanguage()`) | Compose cleanly re-evaluates all `stringResource()` calls in English and Bengali. |

---

### C. Agent Chat Screen (`ChatScreen.kt` vs. `#screen-chat`)

| Visual Aspect | HTML Design Mockup (`#screen-chat`) | Real Jetpack Compose Render (`ChatScreen.kt`) | Technical Difference |
| :--- | :--- | :--- | :--- |
| **Message Bubbles** | Fixed HTML `<div>` tags | Compose `LazyColumn` with keyed items (`items(messages, key = { it.id })`) | Dynamic scroll state with recycling and low memory overhead. |
| **Reasoning Accordion** | Static HTML details tag | Compose `Surface` with clickable expansion state and `CARET_DOWN` / `CARET_RIGHT` | Fluid state-driven disclosure animation. |
| **Tool Approval Card** | Static card mockup | Interactive `ForgeToolApprovalCard` (`Allow Once`, `Always Allow`, `Deny`) | Bound to bidirectional permissions bridge in `RuntimeInstaller.kt`. |
| **Checkpoint Card** | Static diff mockup | Interactive `ForgeCheckpointCard` (`Review Diff`, `Accept Changes`, `Revert`) | Direct hooks for undo/accept git checkpoints. |
| **Pro Mode KeyBar** | Emulated HTML horizontal flex row | Compose `ForgeKeyBar` with `horizontalScroll(rememberScrollState())` | Real mobile soft-keyboard shortcut bar. |

---

### D. Developer & Credits (`DeveloperCreditsScreen.kt`)

| Visual Aspect | HTML Design Mockup (`#screen-dev`) | Real Jetpack Compose Render (`DeveloperCreditsScreen.kt`) | Technical Difference |
| :--- | :--- | :--- | :--- |
| **Profile & Links** | HTML anchor tags | Compose `ForgeButton` launching `Intent.ACTION_VIEW` for `https://nayemhsn.com` and `https://linkedin.com/in/nayemdevbd` | Real Android OS intent dispatch. |
| **Licenses** | Summary list | Collapsible dialog displaying full licenses for PRoot (GPL-2.0), Mobile Harness (MIT), Phosphor (MIT), and Fonts (OFL 1.1) | Direct in-app legal compliance viewer. |

---

### E. Diagnostics & Bug Report (`BugReportScreen.kt`)

| Visual Aspect | HTML Design Mockup (`#screen-bug`) | Real Jetpack Compose Render (`BugReportScreen.kt`) | Technical Difference |
| :--- | :--- | :--- | :--- |
| **Diagnostics** | Simulated text values | Live `DeveloperDiagnostics` (PSS memory, Java heap, active processes, buffer counters) | Directly bound to Android `ActivityManager` and process runtime metrics. |
| **Redaction & Privacy** | Static disclaimer | Enforced redaction: strictly system counters; no code, prompts, or API keys | Prevents credential leaks during issue submission. |
| **Export** | Static button | System clipboard copy via `ClipboardManager` | Real Android clipboard integration. |

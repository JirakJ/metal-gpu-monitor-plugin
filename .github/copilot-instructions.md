# Copilot Instructions — Metal GPU Monitor

## Project
JetBrains IDE plugin — real-time GPU monitoring for macOS using native IOKit/Metal APIs, displayed via status bar widget and tool window.

## Technical Stack
- **Language:** Kotlin (JVM 17+)
- **Build:** Gradle (Kotlin DSL) with `org.jetbrains.intellij.platform` plugin 2.x
- **Platform:** IntelliJ Platform SDK 2024.2+ (sinceBuild: 242)
- **Native:** JNA for macOS IOKit GPU data access
- **Testing:** JUnit 5 + MockK
- **Obfuscation:** ProGuard

## Build & Test
```bash
./gradlew build
./gradlew test
./gradlew test --tests "*.GPUSnapshotTest"
./gradlew runIde
./gradlew verifyPlugin
./gradlew buildPlugin
```

## Architecture
- **Native layer:** `GPUProvider` interface → `IOKitGPUProvider` (macOS) / `NoopGPUProvider` (fallback)
  - `GPUProviderFactory` selects implementation based on OS detection
- **Model:** `GPUSnapshot`, `GPUDeviceInfo`, `ProcessGPUUsage`, `ThermalState`, `AlertRule`, `NativeBridgeResult`
- **Services:**
  - `GPUMonitoringService` — polling loop, data collection
  - `GPUHistoryService` — historical data retention
  - `GPUAlertService` — threshold-based alerts
  - `GPUMonitorStartupActivity` — initialization on project open
- **UI:**
  - `GPUStatusBarWidget` / `GPUStatusBarWidgetFactory` — status bar indicator
  - `GPUToolWindowFactory` → `GPUToolWindowPanel` (charts + info + process table)
  - `GPUChartPanel`, `GPUInfoPanel`, `ProcessTablePanel` — tool window components
  - `GPUMonitorConfigurable` / `GPUSettingsPanel` — settings UI
- **Licensing:** `TierFeatureGate` central guard, `LicenseChecker` for JetBrains Marketplace validation
- **Actions:** `ToggleMonitoringAction`, `ExportGPUDataAction`
- **Utilities:** `ByteFormatter`, `ColorUtils`, `PlatformDetector`

## Conventions
- Package: `dev.htmeta.mgm.*`
- Plugin ID: `dev.htmeta.metal-gpu-monitor`
- Product descriptor code: `PMETALGPUMON`
- GPU operations on background threads (`Task.Backgroundable`, coroutines)
- Texts in `messages/` resource bundle (when localized)
- Versioning: `2026.1.X`

## Production-Grade Requirements
- All I/O and native calls on background threads (`Task.Backgroundable`, coroutines)
- Read/Write actions correctly (`ReadAction.run {}`, `WriteAction.run {}`)
- `Disposable` pattern for listeners and services
- Exception handling: no unhandled exceptions, graceful degradation on non-macOS
- Logging: `Logger.getInstance(MyClass::class.java)` in every class
- EDT must not be blocked — no native/file operations on EDT
- Smart mode check (`DumbService.isDumb()`) where needed

## Freemium Architecture
- `TierFeatureGate` central guard: checks license status before enabling paid features
- JetBrains Marketplace Licensing API via `<product-descriptor>` in `plugin.xml`
- Paid UI elements: disabled or hidden in free tier (never crash/error)
- Trial: 30 days full access, then downgrade to free tier
- Upgrade prompt: max 1× daily, non-intrusive balloon

## Freemium Boundaries
- **Free:** Status bar widget, tool window with 5-min history, basic GPU metrics, thermal state
- **Paid:** 24-hour history, configurable alerts, data export (JSON/CSV), full process list, custom layouts

## CI/CD
- GitHub Actions: `./gradlew check`, `verifyPlugin`, `runPluginVerifier`
- Detekt for static analysis of Kotlin code
- Tag-based release → `publishPlugin` to Marketplace
- Build runs on `macos-latest` (native dependencies)
- Kotlin coding conventions (JetBrains official style)

## Icons
- `src/main/resources/icons/gpu-monitor.svg` (light)
- `src/main/resources/icons/gpu-monitor-dark.svg` (dark)
- `src/main/resources/icons/export.svg` (export action)
- Action icons: 16×16 SVG (light + dark variants)

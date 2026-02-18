# Metal GPU Monitor — JetBrains Marketplace Listing

---

## 📝 Plugin Information

| Field            | Value                                    |
|------------------|------------------------------------------|
| **Name**         | Metal GPU Monitor                        |
| **Plugin ID**    | dev.htmeta.metal-gpu-monitor             |
| **Version**      | 2026.1.0                                 |
| **Category**     | Performance Monitoring                   |
| **Tags**         | GPU, macOS, Metal, Performance, Monitoring, Apple Silicon, Thermal |

---

## 🎯 Short Description

> Real-time GPU monitoring for macOS — track utilization, memory, thermals, and per-process usage directly in your JetBrains IDE. Free core + Pro at $3.90/mo.

_(155 characters)_

---

## 📖 Full Description

### 🖥️ Monitor Your GPU Without Leaving Your IDE

**The Problem:** macOS developers working with GPU-intensive tasks — Metal shaders, ML training, video processing, 3D rendering — have no visibility into GPU performance from their IDE. They constantly switch to Activity Monitor or external tools, breaking their workflow.

**The Solution:** Metal GPU Monitor brings real-time GPU metrics directly into your JetBrains IDE. A status bar widget shows utilization at a glance, while a dedicated tool window provides live charts, memory tracking, thermal state, and per-process GPU usage — all powered by native macOS IOKit APIs.

---

### ✨ Key Features

#### 🆓 Free — Instant GPU Visibility

- 📊 **Status Bar Widget** — At-a-glance GPU utilization percentage with color-coded indicator
- 📈 **Live Charts** — Real-time utilization and memory usage graphs in a dedicated tool window
- 🌡️ **Thermal State** — Monitor GPU thermal status (Nominal, Fair, Serious, Critical)
- 🔍 **Process Tracking** — See top GPU-consuming processes at a glance
- ⏱️ **5-Minute History** — Short-term charts for recent GPU activity
- 💻 **Device Info** — GPU model, vendor, core count, and architecture details
- ⚙️ **Configurable Polling** — Set your preferred refresh interval

#### 💎 Pro — $3.90/month

- 📅 **24-Hour History** — Extended data retention with scrollable historical charts
- 🔔 **Configurable Alerts** — Notifications when GPU utilization or memory exceeds custom thresholds
- 📤 **Data Export** — Export monitoring data to JSON or CSV for external analysis and reporting
- 📋 **Full Process List** — Complete per-process GPU usage with IDE process highlighting
- 🎨 **Custom Layouts** — Rearrange and configure dashboard panels to fit your workflow

---

### 💰 Pricing Model

| Tier     | Price       | Highlights                                              |
|----------|-------------|---------------------------------------------------------|
| **Free** | $0          | Status bar widget, live charts, thermal state, 5-min history, basic process view |
| **Pro**  | $3.90/month | 24-hour history, configurable alerts, data export, full process list, custom layouts |

---

### 🎯 Perfect For

- **Metal/GPU developers** needing real-time performance feedback during shader development
- **ML engineers** monitoring GPU utilization during model training
- **iOS/macOS developers** tracking GPU impact of their applications
- **Performance engineers** profiling GPU-intensive workloads without leaving the IDE
- **DevOps engineers** monitoring system health during GPU-accelerated CI builds

---

### ⚙️ Requirements

| Requirement        | Version                              |
|--------------------|--------------------------------------|
| **Operating System** | macOS 14+ (Sonoma or later)        |
| **Hardware**       | Apple Silicon (M1/M2/M3/M4) or Intel Mac |
| **JetBrains IDE**  | 2024.2+ (build 242+)                |
| **Java**           | 17+                                  |

---

### 🚀 Quick Start

1. **Install** — Search "Metal GPU Monitor" in `Settings → Plugins → Marketplace`
2. **Monitor** — GPU utilization appears immediately in the status bar
3. **Explore** — Open the **GPU Monitor** tool window (bottom panel) for detailed charts
4. **Configure** — Adjust settings in `Settings → Tools → GPU Monitor`
5. **Export** — Use `Tools → GPU Monitor → Export GPU Data...` for analysis (Pro)

---

## 📸 Screenshots & Media

| #  | Screenshot Description                                                                 |
|----|----------------------------------------------------------------------------------------|
| 1  | **Status bar widget** — GPU utilization percentage with color-coded indicator in the IDE status bar |
| 2  | **Tool window overview** — Dedicated panel showing live utilization and memory charts side by side |
| 3  | **Per-process table** — List of GPU-consuming processes with usage percentages and IDE highlighting |
| 4  | **Thermal state display** — Real-time thermal reporting with color-coded severity levels |
| 5  | **24-hour history chart** — Extended historical view with scrollable timeline (Pro) |
| 6  | **Alert configuration** — Settings panel for GPU and memory threshold alerts (Pro) |
| 7  | **Data export dialog** — JSON/CSV export options with date range selection (Pro) |
| 8  | **Settings panel** — Full configuration page with polling interval, display format, and alert options |

---

## 🏷️ Marketplace Categories

| Type          | Category                |
|---------------|-------------------------|
| **Primary**   | Performance Monitoring  |
| **Secondary** | macOS Tools             |

---

## 🔍 Keywords (SEO)

`GPU monitor`, `Metal GPU`, `macOS GPU`, `Apple Silicon`, `GPU utilization`, `GPU memory`, `thermal monitoring`, `performance monitor`, `GPU profiling`, `IOKit`, `M1`, `M2`, `M3`, `M4`, `process GPU usage`, `status bar widget`, `GPU charts`

---

## 📋 Compatibility

### Compatible JetBrains IDEs

| IDE                        | Compatible |
|----------------------------|:----------:|
| IntelliJ IDEA Community    | ✅          |
| IntelliJ IDEA Ultimate     | ✅          |
| PyCharm                    | ✅          |
| WebStorm                   | ✅          |
| GoLand                     | ✅          |
| PhpStorm                   | ✅          |
| Rider                      | ✅          |
| CLion                      | ✅          |
| RubyMine                   | ✅          |
| Android Studio             | ✅          |

**Minimum IDE Version:** 2024.2 (build 242)

---

## 📞 Vendor Information

| Field        | Value                              |
|--------------|------------------------------------|
| **Vendor**   | HTMETA.dev (Jakub Jirák)          |
| **Email**    | support@jakubjirak.com             |
| **Website**  | https://htmeta.dev                 |
| **Support**  | support@jakubjirak.com             |

---

## ✅ Pre-Launch Checklist

- [ ] Plugin icon meets JetBrains Marketplace guidelines (SVG, 40×40 & 80×80)
- [ ] Short description is under 255 characters
- [ ] Full description renders correctly in Marketplace preview
- [ ] Screenshots are high-resolution (1280×800 or higher), PNG format
- [ ] Plugin ID matches `gradle.properties` and `plugin.xml`
- [ ] `plugin.xml` vendor email and URL are correct
- [ ] Pricing tiers configured in JetBrains Marketplace seller dashboard
- [ ] Product code `PMETALGPUMON` is registered
- [ ] License agreement / EULA uploaded
- [ ] Compatibility range tested on minimum supported IDE version (2024.2)
- [ ] Plugin verified with Plugin Verifier (`runPluginVerifier` Gradle task)
- [ ] CHANGELOG.md is up to date for the release version
- [ ] Support email is monitored and responsive
- [ ] Privacy policy URL is set (no data is collected; IOKit calls are local-only)
- [ ] Plugin signed with JetBrains Marketplace certificate
- [ ] macOS-only requirement is clearly stated in description

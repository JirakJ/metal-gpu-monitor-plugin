# Metal GPU Monitor — JetBrains IDE Plugin

[![License](https://img.shields.io/badge/license-Apache--2.0-blue)](LICENSE) ![Version](https://img.shields.io/badge/version-2026.8.0-green)

> **Free & open source.** Since 2026.8.0 the plugin is completely free — every feature is unlocked for everyone, no license needed — and the source code is available under the Apache License 2.0.

[![Build](https://github.com/JirakJ/metal-gpu-monitor-plugin/actions/workflows/verify.yml/badge.svg)](https://github.com/JirakJ/metal-gpu-monitor-plugin/actions/workflows/verify.yml)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/30266.svg)](https://plugins.jetbrains.com/plugin/30266)
[![Rating](https://img.shields.io/jetbrains/plugin/r/rating/30266.svg)](https://plugins.jetbrains.com/plugin/30266)

> Real-time GPU monitoring for macOS developers directly in their JetBrains IDE. Track GPU utilization, memory usage, thermal state, and per-process GPU consumption — all without leaving your editor.

---

## ✨ Features

### Feature Overview

| Feature | Included |
|---------|:--------:|
| Status bar GPU widget | ✅ |
| Real-time GPU utilization | ✅ |
| Memory usage tracking | ✅ |
| Thermal state monitoring | ✅ |
| Tool window with live charts | ✅ |
| 5-minute history | ✅ |
| Basic device info | ✅ |
| 24-hour extended history | ✅ |
| Configurable alerts | ✅ |
| Data export (JSON/CSV) | ✅ |
| Full per-process GPU list | ✅ |
| Custom dashboard layouts | ✅ |

### 📊 Monitoring

- **Status Bar Widget** — At-a-glance GPU utilization percentage with color-coded indicator
- **Tool Window** — Dedicated bottom panel with live charts for GPU utilization and memory usage
- **Thermal Monitoring** — Real-time thermal state reporting (Nominal, Fair, Serious, Critical)
- **Basic Process Tracking** — See top GPU-consuming processes
- **5-Minute History** — Short-term charts for recent GPU activity
- **Device Info** — GPU model, vendor, and core count display

### 🚀 Advanced

- **24-Hour History** — Extended historical data retention with scrollable charts
- **Configurable Alerts** — Get notified when GPU utilization or memory exceeds custom thresholds
- **Data Export** — Export monitoring data to JSON or CSV for external analysis
- **Full Process List** — Complete per-process GPU usage with IDE process highlighting
- **Custom Layouts** — Rearrange and configure dashboard panels to your preference

---

## 📸 Screenshots

| Screenshot | Description |
|------------|-------------|
| ![Status Bar Widget](docs/screenshots/statusbar.png) | GPU utilization in the status bar with color-coded indicator |
| ![Tool Window](docs/screenshots/toolwindow.png) | Live charts for utilization and memory in the dedicated tool window |
| ![Process Table](docs/screenshots/processes.png) | Per-process GPU usage with IDE process highlighting |
| ![Settings](docs/screenshots/settings.png) | Configuration panel for alerts, thresholds, and display options |

> *Screenshots are placeholder references. Actual screenshots will be added before marketplace publication.*

---

## 📦 Installation

### From JetBrains Marketplace

1. Open **Settings** → **Plugins** → **Marketplace**
2. Search for **"Metal GPU Monitor"**
3. Click **Install** and restart the IDE

### From ZIP

1. Download the latest release from [Releases](https://github.com/JirakJ/metal-gpu-monitor-plugin/releases)
2. Open **Settings** → **Plugins** → **⚙️** → **Install Plugin from Disk**
3. Select the downloaded ZIP file and restart the IDE

---

## ⚙️ Requirements

| Requirement | Version |
|-------------|---------|
| **Operating System** | macOS 14+ (Sonoma or later) |
| **Hardware** | Apple Silicon (M1/M2/M3/M4) or Intel Mac with integrated/discrete GPU |
| **JetBrains IDE** | IntelliJ IDEA 2025.1+ (or any compatible JetBrains IDE, build 251+) |
| **Java** | 17+ |

---

## 🔧 Configuration

Settings are available at **Settings → Tools → GPU Monitor**.

| Setting | Default | Description |
|---------|---------|-------------|
| Enable monitoring | `true` | Start/stop GPU monitoring |
| Polling interval | `100ms` | How often GPU metrics are refreshed (50ms - 1s) |
| Status bar display | `Percentage` | Format of the status bar widget (percentage, graph, or both) |
| Alert: GPU utilization | `90%` | Notification threshold for GPU usage (Pro) |
| Alert: Memory usage | `80%` | Notification threshold for GPU memory (Pro) |
| Alert cooldown | `5 min` | Minimum interval between repeated alerts (Pro) |
| History retention | `5 min` / `24 hr` | Data retention period (Free / Pro) |

### Toggle Monitoring

Use **Tools → GPU Monitor → Toggle GPU Monitoring** or the action search (`⌘⇧A` → "Toggle GPU Monitoring") to pause/resume monitoring.

### Export Data

Use **Tools → GPU Monitor → Export GPU Data...** to save monitoring history to JSON or CSV (Pro).

---

## ❓ FAQ

**Q: Why is this macOS only?**
A: Metal GPU Monitor uses native macOS IOKit APIs and the Metal framework to access GPU performance counters. These APIs are specific to Apple's operating system and hardware. There is no cross-platform equivalent that provides the same level of detail.

**Q: How is GPU data collected?**
A: The plugin uses JNA (Java Native Access) to call macOS IOKit functions that read GPU performance statistics directly from the kernel. This is the same data source that Activity Monitor and other system tools use — no additional drivers or permissions are required.

**Q: Will it slow down my IDE?**
A: No. GPU polling runs on a background thread with a configurable interval (default 100ms, adjustable from 50ms to 1s). The overhead is negligible — comparable to having Activity Monitor open.

**Q: Does it support external GPUs (eGPUs)?**
A: Yes, any GPU visible to macOS IOKit will be detected, including eGPUs connected via Thunderbolt.

**Q: What JetBrains IDEs are supported?**
A: All JetBrains IDEs based on the IntelliJ Platform 2024.2+: IntelliJ IDEA, PyCharm, WebStorm, GoLand, CLion, PhpStorm, Rider, RubyMine, and Android Studio.

---

## 🏗️ Building from Source

```bash
git clone https://github.com/JirakJ/metal-gpu-monitor-plugin.git
cd metal-gpu-monitor-plugin
./gradlew build
```

To run the plugin in a sandboxed IDE instance:

```bash
./gradlew runIde
```

---

## 📞 Support

- **Email:** [support@jakubjirak.com](mailto:support@jakubjirak.com)
- **Issue Tracker:** [GitHub Issues](https://github.com/JirakJ/metal-gpu-monitor-plugin/issues)
- **Website:** [https://htmeta.dev](https://htmeta.dev)

---

## 👤 Credits

Built by **[HTMETA.dev](https://htmeta.dev)** (Jakub Jirák)

## Contributing

Issues and pull requests are welcome at https://github.com/JirakJ/metal-gpu-monitor-plugin.

## License

Copyright 2026 Jakub Jirák. Licensed under the [Apache License, Version 2.0](LICENSE).

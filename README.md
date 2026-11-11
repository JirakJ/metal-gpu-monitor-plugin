# Metal GPU Monitor — JetBrains IDE Plugin

[![License](https://img.shields.io/badge/license-Apache--2.0-blue)](LICENSE) ![Version](https://img.shields.io/badge/version-2026.9.0-green)

> **Free & open source.** Since 2026.8.0 the plugin is completely free — every feature is unlocked for everyone, no license needed — and the source code is available under the Apache License 2.0.

[![Build](https://github.com/JirakJ/metal-gpu-monitor-plugin/actions/workflows/verify.yml/badge.svg)](https://github.com/JirakJ/metal-gpu-monitor-plugin/actions/workflows/verify.yml)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/30266.svg)](https://plugins.jetbrains.com/plugin/30266)
[![Rating](https://img.shields.io/jetbrains/plugin/r/rating/30266.svg)](https://plugins.jetbrains.com/plugin/30266)

> Real-time GPU monitoring for macOS developers directly in their JetBrains IDE. Track GPU utilization, memory usage, thermal state, and per-process GPU consumption — all without leaving your editor.

---

## ✨ Features

- **Status bar widget** — GPU utilization at a glance (icon + %, % only, or bar; can be turned off)
- **Tool window** — live chart of GPU utilization and memory, device info (model, vendor, cores, memory),
  and a list of GPU-related processes with the IDE and WindowServer first
- **Thermal state** — Nominal / Fair / Serious / Critical as reported by the system
- **Alerts** — notifications when GPU utilization or memory exceeds a threshold, or the thermal state becomes
  Serious/Critical, with a configurable cooldown
- **Export** — the last 5 minutes of samples plus the current snapshot as JSON or CSV

macOS does not expose GPU utilization per process, so the process list shows which processes are likely using
the GPU (no per-process percentages).

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
| **JetBrains IDE** | Any JetBrains IDE 2025.1+ (build 251+) |

---

## 🔧 Configuration

Settings are available at **Settings → Tools → GPU Monitor**.

| Setting | Default | Description |
|---------|---------|-------------|
| Refresh interval | `100ms` | How often GPU metrics are sampled (50ms – 1s; also in the tool window toolbar) |
| Status bar format | `Icon + %` | Icon + %, % only, bar, or off |
| Export format | `JSON` | Format used by **Export GPU Data...** and the tool window's Export button |
| Enable GPU alerts | `true` | Turns threshold and thermal alerts on or off |
| GPU threshold | `80%` | Alert when GPU utilization exceeds this |
| Memory threshold | `90%` | Alert when GPU memory usage exceeds this |
| Alert cooldown | `60 s` | Minimum interval between repeated alerts of the same kind |

### Toggle Monitoring

Use **Tools → GPU Monitor → Toggle GPU Monitoring** or the action search (`⌘⇧A` → "Toggle GPU Monitoring") to pause/resume monitoring.

### Export Data

Use **Tools → GPU Monitor → Export GPU Data...** (or the tool window's **Export** button) to save the last 5 minutes of samples to JSON or CSV.

---

## ❓ FAQ

**Q: Why is this macOS only?**
A: Metal GPU Monitor reads GPU performance statistics through the native macOS IOKit API. These APIs are specific to Apple's operating system and hardware. There is no cross-platform equivalent that provides the same level of detail.

**Q: How is GPU data collected?**
A: The plugin uses JNA (Java Native Access) to call macOS IOKit functions that read GPU performance statistics directly from the kernel. This is the same data source that Activity Monitor and other system tools use — no additional drivers or permissions are required.

**Q: Will it slow down my IDE?**
A: No. GPU polling runs on a background thread with a configurable interval (default 100ms, adjustable from 50ms to 1s). The overhead is negligible — comparable to having Activity Monitor open.

**Q: What JetBrains IDEs are supported?**
A: All JetBrains IDEs based on the IntelliJ Platform 2025.1+: IntelliJ IDEA, PyCharm, WebStorm, GoLand, CLion, PhpStorm, Rider, RubyMine, and Android Studio.

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

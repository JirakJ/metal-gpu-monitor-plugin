# Metal GPU Monitor

**Real-time GPU monitoring for macOS developers — right in your JetBrains IDE.**

---

## The Problem

macOS developers working with GPU-intensive tasks — Metal shaders, ML model training, video processing, 3D rendering — have no visibility into GPU performance from their IDE. They constantly switch to Activity Monitor or run terminal commands, breaking their flow. Worse, thermal throttling and memory pressure go unnoticed until builds slow down or processes crash.

---

## The Solution

Metal GPU Monitor brings native macOS GPU metrics directly into your JetBrains IDE. A status bar widget keeps utilization visible at all times, while a dedicated tool window provides live charts, memory tracking, thermal state, and per-process GPU usage — all powered by IOKit APIs with negligible overhead.

---

## Key Features

### Status Bar Widget
See GPU utilization at a glance without opening any panel. Color-coded indicator goes from green (idle) through yellow (moderate) to red (high load).

### Live Charts
Real-time utilization and memory usage graphs in a dedicated tool window. Watch GPU behavior as you compile, train models, or render previews.

### Thermal Monitoring
Know when your Mac is throttling. Real-time thermal state reporting shows Nominal, Fair, Serious, or Critical status so you can react before performance degrades.

### Per-Process GPU Usage
See which processes are consuming GPU resources. IDE processes are highlighted so you know exactly how much GPU your build or preview is using.

### Configurable Alerts (Pro)
Set custom thresholds for GPU utilization and memory. Get notified via IDE balloon when limits are exceeded — catch runaway processes before they impact your work.

### Data Export (Pro)
Export monitoring history to JSON or CSV. Perfect for performance reports, profiling analysis, or tracking GPU usage trends across development sessions.

### 24-Hour History (Pro)
Extended data retention lets you review GPU behavior over an entire workday. Scroll through historical charts to identify patterns and anomalies.

---

## How It Works

1. **Install the plugin** — Available from the JetBrains Marketplace for all JetBrains IDEs on macOS.
2. **See it immediately** — GPU utilization appears in your status bar as soon as the IDE starts.
3. **Open the tool window** — Click the GPU Monitor panel (bottom bar) for detailed charts and process info.
4. **Configure alerts** — Set your thresholds in `Settings → Tools → GPU Monitor` (Pro).
5. **Export data** — Use `Tools → GPU Monitor → Export GPU Data...` for analysis (Pro).

---

## How GPU Data Is Collected

Metal GPU Monitor uses **JNA (Java Native Access)** to call macOS IOKit framework functions directly. This is the same kernel-level data source used by Activity Monitor and `powermetrics`. No additional drivers, kernel extensions, or special permissions are required. All data collection happens locally — nothing is sent to external servers.

---

## Pricing

| Feature | Free | Pro ($3.90/month) |
|---------|:----:|:------------------:|
| Status bar widget | ✅ | ✅ |
| Live utilization & memory charts | ✅ | ✅ |
| Thermal state monitoring | ✅ | ✅ |
| Basic process tracking | ✅ | ✅ |
| 5-minute history | ✅ | ✅ |
| Device info | ✅ | ✅ |
| 24-hour history | ❌ | ✅ |
| Configurable alerts | ❌ | ✅ |
| Data export (JSON/CSV) | ❌ | ✅ |
| Full process list | ❌ | ✅ |
| Custom layouts | ❌ | ✅ |

**30-day free trial** — Full Pro access, no credit card required. After 30 days, Pro features disable and you continue with the free tier.

---

## Testimonials

> *"Finally, I can see GPU utilization while iterating on Metal shaders without switching apps. The thermal alerts saved me from a throttling issue I didn't know I had."*
> — **macOS Developer** *(placeholder)*

> *"The per-process view is incredibly useful during ML training. I can see exactly how much GPU my training script is using vs. other processes."*
> — **ML Engineer** *(placeholder)*

---

## FAQ

**Q: Why is this macOS only?**
A: Metal GPU Monitor uses native macOS IOKit APIs that are specific to Apple's operating system. There is no cross-platform equivalent providing the same level of GPU detail.

**Q: Does it work on Intel Macs?**
A: Yes, it works on both Apple Silicon (M1/M2/M3/M4) and Intel Macs with integrated or discrete GPUs.

**Q: Will it slow down my IDE?**
A: No. GPU polling runs on a background thread with a configurable interval (default 2 seconds). The overhead is negligible.

**Q: Does it support external GPUs (eGPUs)?**
A: Yes, any GPU visible to macOS IOKit is detected, including eGPUs connected via Thunderbolt.

**Q: What data is collected?**
A: All data stays local. The plugin reads GPU metrics from the macOS kernel via IOKit. No data is sent to any external server.

**Q: Can I use it in all JetBrains IDEs?**
A: Yes — IntelliJ IDEA, PyCharm, WebStorm, GoLand, CLion, PhpStorm, Rider, RubyMine, and Android Studio (2024.2+).

---

## Requirements

- **macOS 14+** (Sonoma or later)
- **Apple Silicon** (M1/M2/M3/M4) or **Intel Mac** with integrated/discrete GPU
- **JetBrains IDE** 2024.2+ (any IDE based on IntelliJ Platform)

---

## Get Started

Install Metal GPU Monitor from the JetBrains Marketplace and see your GPU performance in real time.

[Install from JetBrains Marketplace](#) | [View Documentation](#) | [Report Issues](https://github.com/JirakJ/metal-gpu-monitor-plugin/issues)

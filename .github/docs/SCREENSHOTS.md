# Screenshot Planning — Metal GPU Monitor

## Screenshots

### 1. Status Bar Widget
- **Title:** "GPU at a Glance"
- **What to capture:** The IDE status bar showing the GPU Monitor widget with a utilization percentage and color-coded indicator. Show it in context with other standard status bar elements (encoding, line separator, git branch).
- **Resolution:** 1280×200 crop of the status bar area

### 2. Tool Window — Full Overview
- **Title:** "Real-Time GPU Dashboard"
- **What to capture:** The GPU Monitor tool window open in the bottom panel. Show the utilization chart, memory chart, device info section, and process table all visible. Use a project with some GPU activity for realistic data.
- **Resolution:** 1280×800

### 3. GPU Utilization Chart
- **Title:** "Live Utilization Chart"
- **What to capture:** Close-up of the utilization chart showing a realistic pattern — idle periods, spikes during compilation or rendering, and the 5-minute history window.
- **Resolution:** 1280×600

### 4. Per-Process GPU Table
- **Title:** "See Which Processes Use Your GPU"
- **What to capture:** The process table showing multiple GPU-consuming processes. Highlight the IDE process row. Show columns: process name, PID, GPU usage %, memory usage.
- **Resolution:** 1280×600

### 5. Thermal State Display
- **Title:** "Thermal Monitoring"
- **What to capture:** The device info panel showing thermal state in different states. Ideally capture or mock the "Nominal" (green), "Fair" (yellow), and "Serious" (orange) states.
- **Resolution:** 1280×400

### 6. 24-Hour History (Pro)
- **Title:** "Extended History for All-Day Insights"
- **What to capture:** The scrollable 24-hour history chart showing a full workday of GPU activity with visible patterns (morning build spikes, midday ML training, afternoon idle).
- **Resolution:** 1280×600

### 7. Alert Configuration (Pro)
- **Title:** "Custom GPU Alerts"
- **What to capture:** The settings panel showing alert configuration — GPU utilization threshold slider, memory threshold slider, cooldown duration, and notification preferences.
- **Resolution:** 1280×800

### 8. Data Export Dialog (Pro)
- **Title:** "Export for External Analysis"
- **What to capture:** The export dialog showing format selection (JSON/CSV), date range picker, and file path chooser.
- **Resolution:** 800×600

---

## Video Demo Outline (50 seconds)

| Time | Scene | Description |
|------|-------|-------------|
| 0–5s | Intro | Plugin name "Metal GPU Monitor" with tagline over dark IDE background |
| 5–12s | Install | Show installing from Marketplace, IDE restart |
| 12–20s | Status bar | Point out the GPU widget appearing in the status bar, show real-time updates |
| 20–30s | Tool window | Open the GPU Monitor tool window, show live charts updating in real time |
| 30–37s | Processes | Scroll through the process table, highlight IDE process |
| 37–42s | Thermal | Show thermal state indicator changing (or stable at Nominal) |
| 42–47s | Pro features | Quick flash of alerts config, export dialog, 24-hour chart |
| 47–50s | CTA | "Install from JetBrains Marketplace" with URL and plugin icon |

---

## Screenshot Specifications

| Spec | Value |
|------|-------|
| **Format** | PNG |
| **Minimum resolution** | 1280×800 |
| **Theme** | Capture in both Light and Dark themes (Darcula) |
| **IDE** | IntelliJ IDEA (most recognizable) |
| **Annotations** | Use red arrows or highlight boxes sparingly to draw attention |
| **Branding** | Include plugin icon in corner of hero screenshot |
| **File naming** | `01-statusbar.png`, `02-toolwindow.png`, `03-chart.png`, etc. |

---

## Thumbnail for Marketplace

- **Size:** 600×400
- **Content:** GPU Monitor tool window with utilization chart prominently visible
- **Background:** Dark theme (Darcula) for visual contrast
- **Text overlay:** "Metal GPU Monitor" in bottom-left corner

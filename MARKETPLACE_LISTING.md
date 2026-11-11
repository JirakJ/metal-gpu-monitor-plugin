# JetBrains Marketplace Listing — Metal GPU Monitor

| Field | Value |
|---|---|
| **Name** | Metal GPU Monitor |
| **Plugin ID** | dev.htmeta.mgm |
| **Marketplace** | https://plugins.jetbrains.com/plugin/30266 |
| **Category** | Tools Integration |
| **License** | Apache-2.0, free |

## Short description

> Real-time GPU utilization, memory and thermal state for macOS in your JetBrains IDE — status bar widget,
> live chart, alerts and JSON/CSV export. Free and open source.

## Full description

The plugin's description is maintained in `src/main/resources/META-INF/plugin.xml` and mirrors the
[README](README.md) feature list. Keep the three in sync and list only what the plugin actually does:
history is kept for 5 minutes, there are no custom layouts, and macOS provides no per-process GPU percentages.

## Compatibility

- macOS only (IOKit); IntelliJ Platform 2025.1 (build 251) and later
- Verified with the JetBrains Plugin Verifier in CI

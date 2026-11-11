# Changelog

## [Unreleased]

## [2026.9.0] - 2026-11-11

### Fixed
- Settings offered refresh intervals (1–10 s) that did not include the 100 ms default, so the page always looked modified and pressing OK silently changed the interval to 1 s; it now offers the same 50 ms – 1 s choices as the tool window.
- The tool window's **Export** button did nothing; it now runs Export GPU Data.
- The export format (JSON/CSV) could not be changed; it is now in Settings.
- `VRAM,totalMB` was read as bytes instead of megabytes.
- Byte sizes, percentages and exports no longer depend on the system locale (decimal commas); non-finite values export as JSON `null`.
- Process list: short name patterns (e.g. `arc`, `edge`) no longer match inside unrelated names such as `mdsearch`; the per-process GPU % column shows "—" because macOS does not report it.
- Thermal alerts now respect "Enable GPU alerts".
- Per-sample INFO logging moved to DEBUG (it flooded idea.log).

### Removed
- Unused settings left over from the paid tiers.
- Descriptions of features that do not exist (24-hour history, custom layouts, per-process GPU usage).

## [2026.8.0] - 2026-11-10

### Changed
- The plugin is now completely free — all features unlocked, no license needed.
- Source code is now open source under the Apache License 2.0: https://github.com/JirakJ/metal-gpu-monitor-plugin


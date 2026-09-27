# Monorepo Architecture

This repository contains both implementations of WiFi Audio Streaming.

```text
WiFiAudioStreaming/
├── android/             Android application
├── desktop/             Desktop application
├── WFAS_PROTOCOL.md     Shared protocol specification
└── .github/workflows/   Monorepo build workflows
```

## Rules

1. WFAS wire-format changes are documented at the repository root.
2. A wire-incompatible change must be implemented on both Android and Desktop before release.
3. Platform-only implementation details stay inside their platform directory.
4. Windows is the primary Desktop validation target for the current development phase.
5. Android and Desktop may have independent UI/version numbers while sharing the same WFAS protocol version.

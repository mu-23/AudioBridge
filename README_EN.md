# WiFi Audio Streaming

Unified Android + Desktop low-latency LAN audio streaming project.

## Layout

- `android/` — Android sender/receiver, including the Shizuku system-audio bridge.
- `desktop/` — Kotlin/Compose Desktop sender/receiver with native Windows audio capture.
- `WFAS_PROTOCOL.md` — shared wire protocol used by both applications.

Both applications are maintained together so protocol, reconnect, liveness and audio behavior can evolve in sync.

The first Desktop integration target is Windows 10/11 x64, while the existing Linux/macOS architecture remains in the tree.

## Build

Android:

```bash
cd android
./gradlew :app:assembleDebug
```

Desktop:

```bash
cd desktop
./gradlew createReleaseDistributable
```

This repository continues under EUPL v1.2 and retains upstream attribution and third-party notices.

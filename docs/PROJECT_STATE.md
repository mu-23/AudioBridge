# Project State

Last governance refresh: 2026-09-29

## Repository

- Repository: `mu23XR/AudioBridge`
- Default branch: `main`
- Monorepo: Android + Desktop
- Shared protocol: WFAS v2
- Protocol source of truth: `/WFAS_PROTOCOL.md`

## Android identity

The next permanent application identity is:

`io.github.mu23xr.audiobridge`

The historical lab identity `com.cuscus.wifiaudiostreaming.lab` is retired for future releases.

Because the signing identity is also being standardized back to the permanent key, users of the historical Stable #153 / test.11 line must uninstall that historical package once before installing the new permanent line. Future releases should then upgrade in place.

## Signing

Permanent Android signer SHA-256:

`4D:DF:26:7D:25:A7:3F:0A:28:44:6C:9E:77:29:32:04:E3:2F:67:77:C6:34:17:FE:B7:BD:92:50:40:FE:FB:63`

Stable #153 / test.11 used a different historical signer and are not the future signing baseline.

See `docs/SIGNING.md`.

## Current product direction

Android:
- Shizuku no-root system-audio bridge
- Android-to-Android low-latency WFAS
- Home/lock should keep active streaming alive
- clearing the app task/Recents is treated as explicit OFF
- stale Shizuku AudioPolicy/UserService must be removed
- sender and receiver volume control
- local playback retained when supported
- Chinese/English UI

Desktop:
- Windows 10/11 x64 is the primary validation target
- PC -> Android and Android -> PC WFAS
- local playback retained
- discovery/reconnect/tray/volume
- unified GitHub Releases with Android

## Immediate known validation target

Before declaring the next stable release:
- verify sender Recents swipe immediately releases the Shizuku audio route
- verify local speaker/media-volume control returns after stop
- verify receiver background behavior
- verify permanent package + permanent signer install path
- verify Android/Desktop update links point to `mu23XR/AudioBridge`

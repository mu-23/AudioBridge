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

## Current task-removal cleanup fix

The Shizuku AudioPolicy cleanup now prefers Android's synchronous
`unregisterAudioPolicy()` before the UserService is removed. The previous
async-only cleanup could race with task/process teardown and leave an OEM audio
route/volume context behind even after network sending had stopped.

## Immediate known validation target

Before declaring the next stable release:
- verify sender Recents swipe immediately releases the Shizuku audio route
- verify local speaker/media-volume control returns after stop
- verify receiver background behavior
- verify permanent package + permanent signer install path
- verify Android/Desktop update links point to `mu23XR/AudioBridge`


## Task-removal ownership rule

Runtime foreground services must keep `android:stopWithTask="false"` so Android delivers
`Service.onTaskRemoved()`. The app then performs one explicit OFF transition before
stopping services. Setting `stopWithTask="true"` bypasses that callback and can leave
the Shizuku UserService alive.

The Shizuku bridge also receives an app-process Binder token. If the normal app process
is killed unexpectedly, Binder death forces the privileged UserService to release
AudioPolicy and exit.

## Silence and latency policy

- Shizuku silence is not a disconnect condition.
- During capture silence, the sender emits a header-only liveness packet once per second.
- Receiver liveness is independent of PCM availability.
- New Android installs default to 20 ms WFAS target latency.
- Receiver startup preroll is 10 ms; excessive AudioTrack backlog is corrected aggressively.

## Next validation build: test.15

The latest development cleanup and notification commands are retained. Runtime
fixes in test.15 address inherited SEND mute (#10), actual zero PCM silence (#4),
nonblocking receiver writes/stalled playback and obsolete transport/mode-command
cleanup (#9). These are implementations awaiting real-device acceptance, not
closed bugs. Existing Recents/owner-death cleanup remains intact.

Test prerelease tags now build signed Android and Windows assets through the
reusable test workflow; Android, Desktop and governance jobs gate publication.
See docs/releases/1.3.1-test.15.md and docs/handoffs/2026-10-01-test15-implementation.md.


## Active issue tracker

GitHub Issues are the authoritative task list for unresolved work.

Current stabilization tracker:
- #6 Android stabilization before next stable release

Blocking bugs:
- #3 Recents removal can leave Shizuku bridge/audio route alive
- #4 Shizuku SEND can disconnect after prolonged source silence
- #5 WFAS playback latency can become unexpectedly large

Do not treat a code change or green CI as proof that these are fixed. Follow each Issue's real-device acceptance criteria before closing it.


## Repository governance tracker

Open Issue #7 tracks remaining repository-governance work, including main-branch protection, stale branch cleanup, release hardening and removal of temporary test workflows. Do not describe repository governance as complete until that tracker is closed.

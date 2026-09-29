# Testing Policy

## Required CI

Every pull request that changes relevant files should run:

- Android unit/build validation
- Windows Desktop build validation
- governance checks

## Android manual regression checklist

For changes touching runtime/services/audio:

1. Start SEND using Shizuku.
2. Confirm local playback behavior is as intended.
3. Lock screen and confirm streaming continues.
4. Return with Home/backgrounding and confirm streaming continues.
5. Swipe AudioBridge from Recents.
6. Confirm sender/receiver stops.
7. Confirm Shizuku UserService/AudioPolicy no longer owns the route.
8. Confirm phone speaker output returns.
9. Confirm media volume buttons control the local output again.
10. Reopen and reconnect; ensure no stale state remains.
11. Test RECEIVE background/reconnect.
12. Test SEND <-> RECEIVE role switching.

## Protocol regression

When changing WFAS:
- Android -> Android
- Desktop -> Android
- Android -> Desktop
- mismatch behavior
- reconnect/liveness
- BYE/task teardown
- packet loss/reordering where applicable

Wire-incompatible changes require a protocol-version bump.

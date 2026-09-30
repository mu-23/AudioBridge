# test.15 implementation handoff

## Authorized scope

User requested latest features (test.14 development line) + today's runtime fixes + a new release. Base: 2caa437 on a separate fix/android-test15-stability branch. Preserve #8 cleanup and #9 command semantics; no deleted author UI, donations, website links or copyright headers restored. Permanent package and signer retained; release is a prerelease, no main merge/stable promotion.

## Work items

- #4 claimed: exact zero PCM liveness + nonblocking playback and stalled-track recovery.
- #10 created: SEND inherits local media mute on remote_submix.
- #9: prevent obsolete mode commands and receiver finalizers from disrupting replacements.
- #11 created: release YAML and Desktop validation blockers.
- #3: existing cleanup retained, regression pending; #5 latency regression pending.

## Implementation checkpoint

Exact digital-zero classification preserves quiet nonzero PCM and validates bounds. Sender advances sample positions during zero capture while sending header-only liveness once a second. Receiver filters zero PCM from older senders, never blocks UDP on writes, accounts actual accepted frames and recovers stalled tracks after 500ms when fresh PCM arrives. Transport generations guard cleanup/callbacks and old CLIENT_BYE, and OFF invalidates generations. Runtime role commands own/cancel pending startup jobs and recheck role/generation after settings load.

REMOTE_SUBMIX capture records original local mute/volume, waits for only the remote_submix route before unmuting, leaves speaker indices untouched, and restores original mute after route release only if local volume did not change. LOOP_BACK_RENDER does not change local volume. Unsupported reflection/route setup fails with explicit capture error rather than adjusting an unverified local route.

Desktop RTP test helpers receive unique names; protocol executable checks join Gradle check. Formal release YAML stale duplicate tail removed. Reusable test workflow builds on prerelease tags and publishes Android/Windows/checksums/source SHA only after build, Desktop and governance jobs succeed. Stable publication stays separate.

## Verification checkpoint

Pending CI and same-signer real-device tests. Do not close runtime issues based on compilation. Detailed diagnostic source: 2026-10-01-device-debug.md; broader static review: 2026-10-01-version-review.md. Temporary probes/scripts are ignored under out/review. Existing diagnostic handoffs included unchanged except scoped updates.

## CI blocker and device authorization checkpoint

Android regression tests and governance passed; Windows protocol checks revealed #13: reordered Snapcast volume echoes could restore an earlier command after confirming the newest one. Added a bounded 2.5-second history filter for superseded local volume/mute values, preserving unrelated remote values and accepting old values again after expiry. Tests now force out-of-order confirmation and check external changes/expiry; callback state uses an AtomicReference. Values matching a superseded local command are ambiguous during this short window because notifications carry no request ID.

Fixed reusable/standalone Windows concurrency groups to prevent mutual cancellation. User explicitly authorized overwrite install on the connected tablet and phone and real-device validation once the signed build is ready. Runtime acceptance remains pending; no runtime issue closed.

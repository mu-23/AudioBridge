# Android Signing Policy

## Permanent identity

All future signed Android test and stable releases use one permanent signing certificate.

SHA-256:

`4D:DF:26:7D:25:A7:3F:0A:28:44:6C:9E:77:29:32:04:E3:2F:67:77:C6:34:17:FE:B7:BD:92:50:40:FE:FB:63`

This fingerprint is public metadata. The keystore bytes and passwords are private and must never be committed.

## GitHub Actions secrets

The release workflow expects:

- `SIGNING_KEY_BASE64`
- `SIGNING_STORE_PASSWORD`
- `SIGNING_KEY_ALIAS`
- `SIGNING_KEY_PASSWORD`

The private keystore must also be kept by the owner in an offline/encrypted backup.

## Historical signer

Stable #153 and test.11 were signed by a different historical certificate:

`4C:C4:F5:04:BA:95:3B:31:FF:AB:B2:55:5E:30:99:DA:31:D9:42:59:CF:58:25:9B:9E:6B:6F:AE:19:78:27:92`

That signer is not the future release baseline.

## Hard rules

- Never auto-generate a release key when a secret is missing.
- Missing signing secrets must fail the release.
- CI verifies the permanent certificate before building.
- CI verifies the produced APK certificate after building.
- Never upload a raw JKS/keystore to Releases or commit history.
- A signing change is a migration event and must be documented explicitly.

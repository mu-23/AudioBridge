# Release Policy

## Stable releases

Stable tags use:

`vMAJOR.MINOR.PATCH`

Example: `v2.0.0`.

Stable releases are generated only by `.github/workflows/release.yml`.

The workflow:
1. validates the tag
2. checks out the exact tag
3. restores the permanent Android signing key from GitHub Secrets
4. verifies the expected certificate fingerprint
5. runs Android unit tests
6. builds an Android release APK
7. verifies the APK signer
8. builds the Windows Desktop package
9. publishes both assets to one GitHub Release

## Versioning

Application version names follow SemVer.

Android `versionCode` must monotonically increase within the permanent package/signing line.

Do not reuse historical Stable #153/test.11 package/signing assumptions for the new permanent line.

## Test builds

Test builds are prerelease/development artifacts and must be clearly labeled. They must never silently become the latest stable release.

Where a signed test APK is intended to upgrade into a future stable build, it must use the permanent signing identity.

## Rollback

Do not delete previous stable Releases merely because a new release is bad. Publish a corrected higher version instead, or clearly mark the affected release as withdrawn.

## Release checklist

- Android CI green
- Desktop CI green
- governance CI green
- signer fingerprint verified
- package id verified
- version metadata verified
- WFAS protocol version compatible with both clients
- release notes describe breaking/migration behavior

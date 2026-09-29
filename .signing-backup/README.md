# Stable 153 signing recovery

This directory stores only an **encrypted** backup of the Android signing keystore used by AudioBridge Stable #153.

Stable signer certificate SHA-256:

`4cc4f504ba953b31ffabb2555e3099da31d94259cf58259b9e6b6fae19782792`

The decryption password is **not** stored in the repository. It must exist only as the GitHub Actions secret `STABLE153_BACKUP_PASSWORD` and in the owner's offline backup.

Do not replace this backup with a newly generated keystore. A new key would break in-place Android upgrades.

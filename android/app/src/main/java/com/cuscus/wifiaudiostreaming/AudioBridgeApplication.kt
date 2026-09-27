package com.cuscus.wifiaudiostreaming

import android.app.Application
import android.content.Intent
import android.os.Build

/**
 * Clears stale persisted receiver intent when the APK version changes.
 *
 * A normal process restart keeps the Stable-153 reconnect behavior. An app
 * upgrade is different: Android may restart a START_STICKY ClientService before
 * the new UI is shown, which can restore the previous receiver target and look
 * like an unsolicited auto-connect. We intentionally start a new version with
 * no carried receiver target.
 */
class AudioBridgeApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        val currentVersion = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageManager.getPackageInfo(packageName, 0).longVersionCode
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, 0).versionCode.toLong()
        }
        val previousVersion = prefs.getLong(KEY_VERSION_CODE, 0L)

        if (previousVersion != 0L && previousVersion != currentVersion) {
            ClientSessionController.userDisconnect(this)
            stopService(Intent(this, ClientService::class.java))
            stopService(Intent(this, AutoConnectService::class.java))
        }

        prefs.edit().putLong(KEY_VERSION_CODE, currentVersion).apply()
    }

    private companion object {
        const val PREFS = "audiobridge_app_runtime"
        const val KEY_VERSION_CODE = "last_version_code"
    }
}

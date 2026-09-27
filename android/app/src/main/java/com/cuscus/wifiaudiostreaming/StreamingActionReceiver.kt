/*
 * Copyright (c) 2026 Marco Morosi
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package com.cuscus.wifiaudiostreaming

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import com.cuscus.wifiaudiostreaming.shizuku.ShizukuAudioBridgeManager

class StreamingActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_STOP_STREAMING -> stopEverything(context)
            ACTION_SERVER_VOLUME_UP -> shiftServerVolume(NotificationCenter.VOLUME_STEP)
            ACTION_SERVER_VOLUME_DOWN -> shiftServerVolume(-NotificationCenter.VOLUME_STEP)
            ACTION_CLIENT_VOLUME_UP -> shiftClientVolume(NotificationCenter.VOLUME_STEP)
            ACTION_CLIENT_VOLUME_DOWN -> shiftClientVolume(-NotificationCenter.VOLUME_STEP)
            ACTION_SHOW_SERVER_VOLUME -> VolumeOverlayController.show(
                context,
                VolumeOverlayController.MODE_SERVER
            )
            ACTION_SHOW_CLIENT_VOLUME -> VolumeOverlayController.show(
                context,
                VolumeOverlayController.MODE_CLIENT
            )
        }
    }

    private fun shiftServerVolume(delta: Float) {
        NetworkManager.serverVolume.value =
            NotificationCenter.nudgeVolume(NetworkManager.serverVolume.value, delta)
    }

    private fun shiftClientVolume(delta: Float) {
        val next = (NetworkManager.clientVolume.value + delta).coerceIn(0f, 1f)
        NetworkManager.setClientVolume(next)
    }

    companion object {
        const val ACTION_STOP_STREAMING = "com.cuscus.wifiaudiostreaming.ACTION_STOP_STREAMING"
        const val ACTION_SERVER_VOLUME_UP =
            "com.cuscus.wifiaudiostreaming.ACTION_SERVER_VOLUME_UP"
        const val ACTION_SERVER_VOLUME_DOWN =
            "com.cuscus.wifiaudiostreaming.ACTION_SERVER_VOLUME_DOWN"
        const val ACTION_CLIENT_VOLUME_UP =
            "com.cuscus.wifiaudiostreaming.ACTION_CLIENT_VOLUME_UP"
        const val ACTION_CLIENT_VOLUME_DOWN =
            "com.cuscus.wifiaudiostreaming.ACTION_CLIENT_VOLUME_DOWN"
        const val ACTION_SHOW_SERVER_VOLUME =
            "com.cuscus.wifiaudiostreaming.ACTION_SHOW_SERVER_VOLUME"
        const val ACTION_SHOW_CLIENT_VOLUME =
            "com.cuscus.wifiaudiostreaming.ACTION_SHOW_CLIENT_VOLUME"

        private const val TASK_PREFS = "wfas_task_runtime"
        private const val KEY_TASK_REMOVED_AT = "task_removed_at"
        private const val TASK_REMOVAL_DEBOUNCE_MS = 1_500L
        private const val TASK_REMOVAL_RESTART_GUARD_MS = 10_000L
        @Volatile
        private var lastTaskRemovalStopElapsed = 0L

        fun handleTaskRemoved(context: Context) {
            val nowElapsed = SystemClock.elapsedRealtime()
            synchronized(this) {
                if (nowElapsed - lastTaskRemovalStopElapsed < TASK_REMOVAL_DEBOUNCE_MS) {
                    return
                }
                lastTaskRemovalStopElapsed = nowElapsed
            }

            val app = context.applicationContext
            RoleSelectionGate.clear()
            app.getSharedPreferences(TASK_PREFS, Context.MODE_PRIVATE)
                .edit()
                .putLong(KEY_TASK_REMOVED_AT, System.currentTimeMillis())
                .commit()
            stopEverything(app)
        }

        fun clearTaskRemovedStop(context: Context) {
            context.applicationContext
                .getSharedPreferences(TASK_PREFS, Context.MODE_PRIVATE)
                .edit()
                .remove(KEY_TASK_REMOVED_AT)
                .apply()
        }

        fun wasRecentlyTaskRemoved(context: Context): Boolean {
            val ts = context.applicationContext
                .getSharedPreferences(TASK_PREFS, Context.MODE_PRIVATE)
                .getLong(KEY_TASK_REMOVED_AT, 0L)
            if (ts <= 0L) return false
            val age = System.currentTimeMillis() - ts
            return age in 0..TASK_REMOVAL_RESTART_GUARD_MS
        }

        /**
         * One authoritative user-requested shutdown path.
         *
         * Swiping the app away from Recents counts as an explicit disconnect,
         * unlike silence, network loss or normal process recreation.
         */
        fun stopEverything(context: Context) {
            val app = context.applicationContext
            ClientSessionController.userDisconnect(app)
            ShizukuAudioBridgeManager.stop(app)
            NetworkManager.stopStreaming(app)
            app.stopService(Intent(app, AudioCaptureService::class.java))
            app.stopService(Intent(app, ClientService::class.java))
            app.stopService(Intent(app, AutoConnectService::class.java))
            app.stopService(Intent(app, SnapcastClientService::class.java))
            app.stopService(Intent(app, RtpClientService::class.java))
            VolumeOverlayController.dismiss()
            NotificationCenter.cancelAll(app)
        }
    }
}
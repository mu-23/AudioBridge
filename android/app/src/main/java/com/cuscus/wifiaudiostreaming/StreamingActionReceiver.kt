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
import com.cuscus.wifiaudiostreaming.shizuku.ShizukuAudioBridgeManager

class StreamingActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_STOP_STREAMING -> stopEverything(context)
            ACTION_SERVER_VOLUME_UP -> shiftServerVolume(NotificationCenter.VOLUME_STEP)
            ACTION_SERVER_VOLUME_DOWN -> shiftServerVolume(-NotificationCenter.VOLUME_STEP)
            ACTION_CLIENT_VOLUME_UP -> shiftClientVolume(NotificationCenter.VOLUME_STEP)
            ACTION_CLIENT_VOLUME_DOWN -> shiftClientVolume(-NotificationCenter.VOLUME_STEP)
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

        /**
         * One authoritative user-requested shutdown path.
         *
         * Swiping the app away from Recents counts as an explicit disconnect,
         * unlike silence, network loss or normal process recreation.
         */
        fun stopEverything(context: Context) {
            val app = context.applicationContext
            ClientSessionController.userDisconnect(app)
            if (ShizukuAudioBridgeManager.isActive()) {
                ShizukuAudioBridgeManager.stop(app)
            }
            NetworkManager.stopStreaming(app)
            app.stopService(Intent(app, AudioCaptureService::class.java))
            app.stopService(Intent(app, ClientService::class.java))
            app.stopService(Intent(app, AutoConnectService::class.java))
            app.stopService(Intent(app, SnapcastClientService::class.java))
            app.stopService(Intent(app, RtpClientService::class.java))
            NotificationCenter.cancelAll(app)
        }
    }
}
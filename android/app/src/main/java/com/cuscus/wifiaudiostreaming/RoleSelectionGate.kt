package com.cuscus.wifiaudiostreaming

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Explicit role chosen by the user for the current app process.
 *
 * A fresh process starts in Sender mode, matching the UI default.
 *
 * Sender mode by itself never starts capture or transmission; only the user's
 * explicit Start action does that. Auto-connect/reconnect is allowed only after
 * the user switches the UI to Receiver mode.
 *
 * This does not replace the Stable-153 persistent session logic; it only prevents
 * receiver services from restoring while the app is still in its default Sender mode.
 */
object RoleSelectionGate {
    enum class Role { SENDER, RECEIVER }

    private val _role = MutableStateFlow(Role.SENDER)
    val role: StateFlow<Role> = _role.asStateFlow()

    fun selectSender() {
        _role.value = Role.SENDER
    }

    fun selectReceiver() {
        _role.value = Role.RECEIVER
    }

    fun clear() {
        _role.value = Role.SENDER
    }

    fun isSenderSelected(): Boolean = _role.value == Role.SENDER
    fun isReceiverSelected(): Boolean = _role.value == Role.RECEIVER
}

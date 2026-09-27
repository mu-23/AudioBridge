package com.cuscus.wifiaudiostreaming

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Explicit role chosen by the user for the current app process.
 *
 * A fresh process starts with no role. Background auto-connect/reconnect is
 * allowed only after the user has explicitly chosen Send or Receive.
 * This does not replace the Stable-153 persistent session logic; it only gates
 * when that logic is allowed to start.
 */
object RoleSelectionGate {
    enum class Role { SENDER, RECEIVER }

    private val _role = MutableStateFlow<Role?>(null)
    val role: StateFlow<Role?> = _role.asStateFlow()

    fun selectSender() {
        _role.value = Role.SENDER
    }

    fun selectReceiver() {
        _role.value = Role.RECEIVER
    }

    fun clear() {
        _role.value = null
    }

    fun isSenderSelected(): Boolean = _role.value == Role.SENDER
    fun isReceiverSelected(): Boolean = _role.value == Role.RECEIVER
}

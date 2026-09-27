package com.cuscus.wifiaudiostreaming

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Role chosen explicitly by the user for the current app process/session.
 *
 * Auto-connect must never decide the role on behalf of the user. The role is
 * intentionally not persisted across a cold process start: after a fresh app
 * launch the user chooses Send or Receive first.
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

    fun isReceiverSelected(): Boolean = _role.value == Role.RECEIVER
    fun isSenderSelected(): Boolean = _role.value == Role.SENDER
}

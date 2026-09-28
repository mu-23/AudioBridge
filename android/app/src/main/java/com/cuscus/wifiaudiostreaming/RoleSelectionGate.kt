package com.cuscus.wifiaudiostreaming

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Explicit runtime mode selected by the user.
 *
 * OFF means neither sender nor receiver is active.
 * SEND and RECEIVE survive normal process recreation so Android can restore
 * foreground services without requiring MainActivity to be opened again.
 */
object RoleSelectionGate {
    enum class Role { OFF, SENDER, RECEIVER }

    private const val PREFS = "audiobridge_runtime_mode"
    private const val KEY_ROLE = "role"

    private val _role = MutableStateFlow(Role.OFF)
    val role: StateFlow<Role> = _role.asStateFlow()

    @Volatile
    private var appContext: Context? = null
    @Volatile
    private var initialized = false

    fun initialize(context: Context) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            val app = context.applicationContext
            appContext = app
            val stored = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_ROLE, Role.OFF.name)
            _role.value = runCatching { Role.valueOf(stored ?: Role.OFF.name) }
                .getOrDefault(Role.OFF)
            initialized = true
        }
    }

    private fun set(role: Role, context: Context? = null) {
        context?.applicationContext?.let {
            appContext = it
            initialized = true
        }
        _role.value = role
        appContext?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            ?.edit()
            ?.putString(KEY_ROLE, role.name)
            ?.apply()
    }

    fun selectSender(context: Context? = null) = set(Role.SENDER, context)

    fun selectReceiver(context: Context? = null) = set(Role.RECEIVER, context)

    fun selectOff(context: Context? = null) = set(Role.OFF, context)

    /** Legacy call sites use clear() for "no active role". */
    fun clear() = selectOff()

    fun isSenderSelected(): Boolean = _role.value == Role.SENDER

    fun isReceiverSelected(): Boolean = _role.value == Role.RECEIVER

    fun isOffSelected(): Boolean = _role.value == Role.OFF
}

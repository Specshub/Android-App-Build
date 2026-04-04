package com.iptv.player.data.local // ✅ الهوية الجديدة الموحدة

import android.content.Context
import android.content.SharedPreferences
import com.iptv.player.data.model.LoginCredentials // ✅ استيراد الموديل من المسار الجديد

class PrefsManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "iptv_player_prefs"
        private const val KEY_HOST = "pref_host"
        private const val KEY_USERNAME = "pref_username"
        private const val KEY_PASSWORD = "pref_password"
        private const val KEY_REMEMBER_LOGIN = "pref_remember_login"
        private const val KEY_DISCLAIMER_ACCEPTED = "pref_disclaimer_accepted"
        private const val KEY_ASPECT_RATIO = "pref_aspect_ratio"

        // Aspect ratio constants
        const val ASPECT_16_9 = "16:9"
        const val ASPECT_4_3 = "4:3"
        const val ASPECT_STRETCH = "stretch"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ── Login ──────────────────────────────────────────────────────────────

    fun saveCredentials(credentials: LoginCredentials) {
        prefs.edit().apply {
            putString(KEY_HOST, credentials.host)
            putString(KEY_USERNAME, credentials.username)
            putString(KEY_PASSWORD, credentials.password)
            putBoolean(KEY_REMEMBER_LOGIN, true)
            apply()
        }
    }

    fun getCredentials(): LoginCredentials? {
        if (!isRememberLogin()) return null
        val host = prefs.getString(KEY_HOST, null) ?: return null
        val username = prefs.getString(KEY_USERNAME, null) ?: return null
        val password = prefs.getString(KEY_PASSWORD, null) ?: return null
        return LoginCredentials(host, username, password)
    }

    fun clearCredentials() {
        prefs.edit().apply {
            remove(KEY_HOST)
            remove(KEY_USERNAME)
            remove(KEY_PASSWORD)
            remove(KEY_REMEMBER_LOGIN)
            apply()
        }
    }

    fun isRememberLogin(): Boolean = prefs.getBoolean(KEY_REMEMBER_LOGIN, false)

    // ── Disclaimer ─────────────────────────────────────────────────────────

    fun isDisclaimerAccepted(): Boolean = prefs.getBoolean(KEY_DISCLAIMER_ACCEPTED, false)

    fun setDisclaimerAccepted(accepted: Boolean) {
        prefs.edit().putBoolean(KEY_DISCLAIMER_ACCEPTED, accepted).apply()
    }

    // ── Player Preferences ─────────────────────────────────────────────────

    fun getAspectRatio(): String = prefs.getString(KEY_ASPECT_RATIO, ASPECT_16_9) ?: ASPECT_16_9

    fun saveAspectRatio(ratio: String) {
        prefs.edit().putString(KEY_ASPECT_RATIO, ratio).apply()
    }
}

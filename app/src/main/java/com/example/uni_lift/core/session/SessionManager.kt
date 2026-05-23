package com.example.uni_lift.core.session

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "UniLiftSession"
        private const val KEY_TOKEN = "user_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
    }

    fun saveSession(token: String, userId: String, role: String, name: String, email: String) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USER_ROLE, role)
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_EMAIL, email)
            .apply()
    }

    fun saveAuthToken(token: String) { prefs.edit().putString(KEY_TOKEN, token).apply() }
    fun saveRefreshToken(token: String) { prefs.edit().putString(KEY_REFRESH_TOKEN, token).apply() }
    fun fetchRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)
    fun fetchAuthToken(): String? = prefs.getString(KEY_TOKEN, null)
    fun fetchUserId(): String? = prefs.getString(KEY_USER_ID, null)
    fun fetchUserRole(): String = prefs.getString(KEY_USER_ROLE, "RIDER") ?: "RIDER"
    fun fetchUserName(): String = prefs.getString(KEY_USER_NAME, "") ?: ""
    fun fetchUserEmail(): String = prefs.getString(KEY_USER_EMAIL, "") ?: ""
    fun isLoggedIn(): Boolean = !fetchAuthToken().isNullOrBlank()

    fun clearSession() {
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_ROLE)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_REFRESH_TOKEN)
            .apply()
    }

    // Legacy alias kept for existing callers
    fun clearAuthToken() = clearSession()
}

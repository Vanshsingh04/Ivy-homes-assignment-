package data

import kotlinx.browser.window

private fun jsDateNow(): Double = js("Date.now()")

/**
 * Token storage using localStorage via Wasm JS interop.
 * Persists access_token and refresh_token across page refreshes.
 */
object TokenStorage {
    private const val ACCESS_TOKEN_KEY = "ivy_access_token"
    private const val REFRESH_TOKEN_KEY = "ivy_refresh_token"
    private const val EXPIRES_AT_KEY = "ivy_expires_at"
    private const val USER_EMAIL_KEY = "ivy_user_email"

    fun saveTokens(accessToken: String, refreshToken: String, expiresInSeconds: Int) {
        val expiresAt = (jsDateNow() / 1000.0 + expiresInSeconds).toLong()
        window.localStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
        window.localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
        window.localStorage.setItem(EXPIRES_AT_KEY, expiresAt.toString())
    }

    fun getAccessToken(): String? = window.localStorage.getItem(ACCESS_TOKEN_KEY)

    fun getRefreshToken(): String? = window.localStorage.getItem(REFRESH_TOKEN_KEY)

    fun isTokenExpired(): Boolean {
        val expiresAt = window.localStorage.getItem(EXPIRES_AT_KEY)?.toLongOrNull() ?: return true
        val now = (jsDateNow() / 1000.0).toLong()
        return now >= (expiresAt - 30) // 30 second buffer
    }

    fun saveUserEmail(email: String) {
        window.localStorage.setItem(USER_EMAIL_KEY, email)
    }

    fun getUserEmail(): String? = window.localStorage.getItem(USER_EMAIL_KEY)

    fun clearAll() {
        window.localStorage.removeItem(ACCESS_TOKEN_KEY)
        window.localStorage.removeItem(REFRESH_TOKEN_KEY)
        window.localStorage.removeItem(EXPIRES_AT_KEY)
        window.localStorage.removeItem(USER_EMAIL_KEY)
    }

    fun hasValidSession(): Boolean {
        return getAccessToken() != null && getRefreshToken() != null
    }
}

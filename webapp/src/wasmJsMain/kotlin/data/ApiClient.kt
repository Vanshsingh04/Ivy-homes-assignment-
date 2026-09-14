package data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * API client configured for the actual Ivy Homes API behavior:
 * - Uses X-API-Key header (NOT query param)
 * - Uses offset/limit pagination (NOT page)
 * - Handles token refresh (access_token expires in 15 min)
 */
object ApiClient {
    const val BASE_URL = "https://solve.ivy.homes"
    const val API_KEY = "IVY26-8E6108248D19"

    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
    }

    /** Login and store tokens. Returns the login response. */
    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = httpClient.post("$BASE_URL/auth/login") {
                header("X-API-Key", API_KEY)
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, password))
            }
            
            if (!response.status.isSuccess()) {
                return Result.failure(Exception("Invalid email or password"))
            }

            val loginResponse: LoginResponse = response.body()
            TokenStorage.saveTokens(loginResponse.access_token, loginResponse.refresh_token, loginResponse.expires_in)
            TokenStorage.saveUserEmail(email)
            Result.success(loginResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Refresh the access token using the stored refresh token. */
    suspend fun refreshToken(): Boolean {
        val refreshToken = TokenStorage.getRefreshToken() ?: return false
        return try {
            val response: LoginResponse = httpClient.post("$BASE_URL/auth/refresh") {
                header("X-API-Key", API_KEY)
                contentType(ContentType.Application.Json)
                setBody(RefreshRequest(refreshToken))
            }.body()

            TokenStorage.saveTokens(response.access_token, response.refresh_token, response.expires_in)
            true
        } catch (e: Exception) {
            false
        }
    }

    /** Ensure we have a valid token. Refresh if needed. */
    @PublishedApi
    internal suspend fun ensureValidToken(): Boolean {
        if (TokenStorage.isTokenExpired()) {
            return refreshToken()
        }
        return TokenStorage.getAccessToken() != null
    }

    /** Make an authenticated GET request. Auto-refreshes token on 401. */
    suspend inline fun <reified T> get(path: String, params: Map<String, String> = emptyMap()): Result<T> {
        if (!ensureValidToken()) return Result.failure(Exception("Not authenticated"))

        return try {
            var response = httpClient.get("$BASE_URL$path") {
                header("X-API-Key", API_KEY)
                header("Authorization", "Bearer ${TokenStorage.getAccessToken()}")
                params.forEach { (k, v) -> parameter(k, v) }
            }

            // Retry on 401
            if (response.status == HttpStatusCode.Unauthorized) {
                if (refreshToken()) {
                    response = httpClient.get("$BASE_URL$path") {
                        header("X-API-Key", API_KEY)
                        header("Authorization", "Bearer ${TokenStorage.getAccessToken()}")
                        params.forEach { (k, v) -> parameter(k, v) }
                    }
                } else {
                    return Result.failure(Exception("Authentication expired. Please login again."))
                }
            }

            if (!response.status.isSuccess()) {
                return Result.failure(Exception("API error: ${response.status} - ${response.bodyAsText()}"))
            }

            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Make an authenticated POST request. */
    suspend inline fun <reified T, reified B> post(path: String, body: B): Result<T> {
        if (!ensureValidToken()) return Result.failure(Exception("Not authenticated"))

        return try {
            var response = httpClient.post("$BASE_URL$path") {
                header("X-API-Key", API_KEY)
                header("Authorization", "Bearer ${TokenStorage.getAccessToken()}")
                contentType(ContentType.Application.Json)
                setBody(body)
            }

            if (response.status == HttpStatusCode.Unauthorized) {
                if (refreshToken()) {
                    response = httpClient.post("$BASE_URL$path") {
                        header("X-API-Key", API_KEY)
                        header("Authorization", "Bearer ${TokenStorage.getAccessToken()}")
                        contentType(ContentType.Application.Json)
                        setBody(body)
                    }
                }
            }

            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Make an authenticated DELETE request. */
    suspend fun delete(path: String): Result<SaveResponse> {
        if (!ensureValidToken()) return Result.failure(Exception("Not authenticated"))

        return try {
            val response = httpClient.delete("$BASE_URL$path") {
                header("X-API-Key", API_KEY)
                header("Authorization", "Bearer ${TokenStorage.getAccessToken()}")
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        TokenStorage.clearAll()
    }
}

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import java.io.File
import java.time.Instant

const val API_KEY = "IVY26-8E6108248D19"
const val BASE_URL = "https://solve.ivy.homes"
const val EMAIL = "demo1@ivy.homes"
const val PASSWORD = "fcd9ed5fe4"
const val LIMIT = 50

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class LoginResponse(
    val access_token: String,
    val refresh_token: String,
    val token_type: String,
    val expires_in: Int,
    val refresh_url: String? = null,
    val user: JsonObject? = null
)

@Serializable
data class RefreshRequest(val refresh_token: String)

@Serializable
data class PaginatedResponse(
    val limit: Int,
    val offset: Int,
    val count: Int,
    val total: Int,
    val has_more: Boolean,
    val results: JsonArray
)

class Session(private val client: HttpClient) {
    private var accessToken: String = ""
    private var refreshToken: String = ""
    private var expiresAt: Long = 0

    suspend fun login() {
        val response: LoginResponse = client.post("$BASE_URL/auth/login") {
            header("X-API-Key", API_KEY)
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(EMAIL, PASSWORD))
        }.body()

        accessToken = response.access_token
        refreshToken = response.refresh_token
        expiresAt = Instant.now().epochSecond + response.expires_in - 30
        println("  Logged in, token expires in ${response.expires_in}s")
    }

    suspend fun refresh() {
        try {
            val response: LoginResponse = client.post("$BASE_URL/auth/refresh") {
                header("X-API-Key", API_KEY)
                contentType(ContentType.Application.Json)
                setBody(RefreshRequest(refreshToken))
            }.body()

            accessToken = response.access_token
            refreshToken = response.refresh_token
            expiresAt = Instant.now().epochSecond + response.expires_in - 30
            println("  Token refreshed")
        } catch (e: Exception) {
            println("  Refresh failed: ${e.message}, re-logging in...")
            login()
        }
    }

    suspend fun ensureValid() {
        if (Instant.now().epochSecond > expiresAt) {
            refresh()
        }
    }

    fun authHeaders(): HeadersBuilder.() -> Unit = {
        append("X-API-Key", API_KEY)
        append("Authorization", "Bearer $accessToken")
    }
}

suspend fun fetchAll(client: HttpClient, session: Session, endpoint: String, label: String): JsonArray {
    val allRecords = mutableListOf<JsonElement>()
    var offset = 0
    var total: Int? = null

    println("\nFetching $label from $endpoint...")
    while (true) {
        session.ensureValid()
        val data: PaginatedResponse = client.get("$BASE_URL$endpoint") {
            headers(session.authHeaders())
            parameter("offset", offset)
            parameter("limit", LIMIT)
        }.body()

        if (total == null) {
            total = data.total
            println("  Total: $total")
        }

        allRecords.addAll(data.results)
        println("  Fetched offset=$offset, got ${data.count} records (${allRecords.size}/$total)")

        if (!data.has_more || data.count == 0) break
        offset += data.count
        delay(50)
    }

    println("  Done: ${allRecords.size} total records fetched")
    return JsonArray(allRecords)
}

fun main() = runBlocking {
    val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    val session = Session(client)
    session.login()

    // Fetch /v1/me
    session.ensureValid()
    val me: JsonObject = client.get("$BASE_URL/v1/me") {
        headers(session.authHeaders())
    }.body()
    println("\nAPI Context: $me")

    val listings = fetchAll(client, session, "/v1/listings", "Listings")
    val rentals = fetchAll(client, session, "/v1/rentals", "Rentals")
    val projects = fetchAll(client, session, "/v1/projects", "Projects")

    val output = buildJsonObject {
        put("meta", buildJsonObject {
            put("api_key", API_KEY)
            put("city", me["city"]!!)
            put("city_id", me["city_id"]!!)
            put("fetched_at", Instant.now().toString())
        })
        put("listings", listings)
        put("rentals", rentals)
        put("projects", projects)
    }

    val outputStr = json.encodeToString(JsonElement.serializer(), output)
    File("data.json").writeText(outputStr)

    println("\n=== Summary ===")
    println("Listings: ${listings.size}")
    println("Rentals:  ${rentals.size}")
    println("Projects: ${projects.size}")
    println("Saved to data.json (${outputStr.length / 1024 / 1024} MB)")

    client.close()
}

package data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Repository layer handling data fetching, caching, and client-side filtering.
 *
 * Client-side filtering is needed because:
 * - min_price, max_price, furnishing filters are IGNORED by the server
 * - is_live=false listings are returned (docs say they aren't)
 * - Negative prices exist (corrupt data)
 */
object Repository {
    // Cached data
    private val _listings = MutableStateFlow<List<Listing>>(emptyList())
    val listings: StateFlow<List<Listing>> = _listings

    private val _rentals = MutableStateFlow<List<Rental>>(emptyList())
    val rentals: StateFlow<List<Rental>> = _rentals

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects

    private val _savedIds = MutableStateFlow<Set<String>>(emptySet())
    val savedIds: StateFlow<Set<String>> = _savedIds

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // ---- Listings ----

    suspend fun fetchListings(
        locality: String? = null,
        bhk: Int? = null,
        propertyType: String? = null,
        offset: Int = 0,
        limit: Int = 50
    ): PaginatedResponse<Listing> {
        val params = mutableMapOf<String, String>()
        params["offset"] = offset.toString()
        params["limit"] = limit.toString()
        locality?.let { params["locality"] = it }
        bhk?.let { params["bhk"] = it.toString() }
        propertyType?.let { params["property_type"] = it }

        val result = ApiClient.get<PaginatedResponse<Listing>>("/v1/listings", params)
        return result.getOrElse {
            _error.value = it.message
            PaginatedResponse()
        }
    }

    suspend fun fetchListing(id: String): Listing? {
        val result = ApiClient.get<Listing>("/v1/listings/$id")
        return result.getOrElse {
            _error.value = it.message
            null
        }
    }

    /** Apply client-side filters that the server ignores. */
    fun filterListings(
        listings: List<Listing>,
        minPrice: Long? = null,
        maxPrice: Long? = null,
        furnishing: String? = null,
        liveOnly: Boolean = true
    ): List<Listing> {
        return listings.filter { listing ->
            // Filter out non-live if requested
            if (liveOnly && !listing.is_live) return@filter false
            // Filter out negative/corrupt prices
            if (listing.price < 0) return@filter false
            // Client-side price filter (server ignores these)
            minPrice?.let { if (listing.price < it) return@filter false }
            maxPrice?.let { if (listing.price > it) return@filter false }
            // Client-side furnishing filter (server ignores this)
            furnishing?.let {
                if (it.isNotBlank() && listing.furnishing != it) return@filter false
            }
            true
        }
    }

    // ---- Rentals ----

    suspend fun fetchRentals(
        locality: String? = null,
        bhk: Int? = null,
        offset: Int = 0,
        limit: Int = 50
    ): PaginatedResponse<Rental> {
        val params = mutableMapOf<String, String>()
        params["offset"] = offset.toString()
        params["limit"] = limit.toString()
        locality?.let { params["locality"] = it }
        bhk?.let { params["bhk"] = it.toString() }

        val result = ApiClient.get<PaginatedResponse<Rental>>("/v1/rentals", params)
        return result.getOrElse {
            _error.value = it.message
            PaginatedResponse()
        }
    }

    suspend fun fetchRental(id: String): Rental? {
        val result = ApiClient.get<Rental>("/v1/rentals/$id")
        return result.getOrElse {
            _error.value = it.message
            null
        }
    }

    // ---- Projects ----

    suspend fun fetchProjects(
        locality: String? = null,
        offset: Int = 0,
        limit: Int = 50
    ): PaginatedResponse<Project> {
        val params = mutableMapOf<String, String>()
        params["offset"] = offset.toString()
        params["limit"] = limit.toString()
        locality?.let { params["locality"] = it }

        val result = ApiClient.get<PaginatedResponse<Project>>("/v1/projects", params)
        return result.getOrElse {
            _error.value = it.message
            PaginatedResponse()
        }
    }

    suspend fun fetchProject(id: String): Project? {
        val result = ApiClient.get<Project>("/v1/projects/$id")
        return result.getOrElse {
            _error.value = it.message
            null
        }
    }

    // ---- Saved / Favourites ----

    suspend fun fetchSaved(): List<Listing> {
        val result = ApiClient.get<SavedListResponse>("/v1/saved")
        return result.getOrElse {
            _error.value = it.message
            SavedListResponse()
        }.results.also { listings ->
            _savedIds.value = listings.map { it.listing_id }.toSet()
        }
    }

    suspend fun saveListing(listingId: String): Boolean {
        val result = ApiClient.post<SaveResponse, SaveRequest>("/v1/saved", SaveRequest(listingId))
        return result.getOrNull()?.ok == true.also {
            if (it) _savedIds.value = _savedIds.value + listingId
        }
    }

    suspend fun unsaveListing(listingId: String): Boolean {
        val result = ApiClient.delete("/v1/saved/$listingId")
        return result.getOrNull()?.ok == true.also {
            if (it) _savedIds.value = _savedIds.value - listingId
        }
    }

    fun isSaved(listingId: String): Boolean = listingId in _savedIds.value

    // ---- User Info ----

    suspend fun fetchMe(): MeResponse? {
        val result = ApiClient.get<MeResponse>("/v1/me")
        return result.getOrNull()
    }

    fun clearError() {
        _error.value = null
    }
}

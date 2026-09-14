package data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class LoginResponse(
    val access_token: String,
    val refresh_token: String,
    val token_type: String,
    val expires_in: Int,
    val refresh_url: String? = null,
    val user: UserInfo? = null
)

@Serializable
data class RefreshRequest(val refresh_token: String)

@Serializable
data class UserInfo(
    val email: String,
    val name: String? = null
)

@Serializable
data class MeResponse(
    val user: UserInfo,
    val city_id: Int,
    val city: String,
    val assigned_locality: String? = null,
    val reference_date: String? = null,
    val note: String? = null
)

@Serializable
data class PaginatedResponse<T>(
    val limit: Int = 0,
    val offset: Int = 0,
    val count: Int = 0,
    val total: Int = 0,
    val has_more: Boolean = false,
    val results: List<T> = emptyList()
)

@Serializable
data class Listing(
    val listing_id: String = "",
    val listing_url: String? = null,
    val website: String? = null,
    val city_id: Int = 0,
    val apartment_name: String? = null,
    val locality: String? = null,
    val property_type: String? = null,
    val bedroom: Int = 0,
    val bathroom: Int = 0,
    val balcony: Int = 0,
    val floor: Int = 0,
    val total_floors: Int = 0,
    val furnishing: String? = null,
    val facing_direction: String? = null,
    val covered_parking: Int = 0,
    val price: Long = 0,
    val carpet_area: Int = 0,
    val super_built_up_area: Int = 0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val posted_by: String? = null,
    val posted_by_name: String? = null,
    val posted_by_contact: String? = null,
    val project_id: String? = null,
    val is_verified: Boolean = false,
    val description: String? = null,
    val posted_at: String? = null,
    val is_live: Boolean = true
)

@Serializable
data class Rental(
    val listing_id: String = "",
    val listing_url: String? = null,
    val website: String? = null,
    val city_id: Int = 0,
    val title: String? = null,
    val apartment_name: String? = null,
    val locality: String? = null,
    val property_type: String? = null,
    val bedroom: Int = 0,
    val bathroom: Int = 0,
    val floor: Int = 0,
    val total_floors: Int = 0,
    val furnishing: String? = null,
    val facing_direction: String? = null,
    val price: Long = 0,
    val deposit: Long = 0,
    val maintenance: Long = 0,
    val carpet_area: Int = 0,
    val super_builtup_area: Int = 0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val posted_by: String? = null,
    val posted_by_name: String? = null,
    val posted_by_contact: String? = null,
    val description: String? = null,
    val posted_at: String? = null,
    val is_live: Boolean = true
)

@Serializable
data class Project(
    val project_id: String = "",
    val project_url: String? = null,
    val city_id: Int = 0,
    val apartment_name: String? = null,
    val developer_name: String? = null,
    val locality: String? = null,
    val project_status: String? = null,
    val total_units: Int = 0,
    val total_towers: Int = 0,
    val total_floors: Int = 0,
    val launch_date: String? = null,
    val possession_date: String? = null,
    val rera_number: String? = null,
    val min_area_sqft: Int = 0,
    val max_area_sqft: Int = 0,
    val amenities: List<String> = emptyList(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val total_listings: Int = 0,
    val price_min: Double = 0.0,
    val price_max: Double = 0.0
)

@Serializable
data class SaveRequest(val listing_id: String)

@Serializable
data class SaveResponse(
    val ok: Boolean = false,
    val listing_id: String? = null,
    val saved_count: Int = 0
)

@Serializable
data class SavedListResponse(
    val count: Int = 0,
    val results: List<Listing> = emptyList()
)

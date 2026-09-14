package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.Listing
import data.PaginatedResponse
import data.Repository
import kotlinx.coroutines.launch
import theme.IvyColors
import ui.components.*

@Composable
fun ListingsScreen(
    onListingClick: (String) -> Unit
) {
    var listings by remember { mutableStateOf<List<Listing>>(emptyList()) }
    var filteredListings by remember { mutableStateOf<List<Listing>>(emptyList()) }
    var total by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var hasMore by remember { mutableStateOf(false) }
    var offset by remember { mutableStateOf(0) }
    var filters by remember { mutableStateOf(FilterState()) }
    var savedIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    val localities = remember {
        listOf(
            "whitefield", "koramangala", "hsr layout", "indiranagar", "jayanagar",
            "jp nagar", "electronic city", "marathahalli", "bellandur", "sarjapur road",
            "hebbal", "yelahanka", "bannerghatta road", "rajajinagar", "malleshwaram",
            "btm layout", "basavanagudi", "mg road", "brigade road", "domlur"
        )
    }

    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Fetch data
    fun loadListings(reset: Boolean = false) {
        scope.launch {
            isLoading = true
            val newOffset = if (reset) 0 else offset
            val response = Repository.fetchListings(
                locality = filters.locality.ifBlank { null },
                bhk = filters.bhk,
                propertyType = filters.propertyType.ifBlank { null },
                offset = newOffset
            )

            if (reset) {
                listings = response.results
                offset = response.results.size
            } else {
                listings = listings + response.results
                offset += response.results.size
            }

            total = response.total
            hasMore = response.has_more

            // Apply client-side filters (server ignores min_price, max_price, furnishing)
            filteredListings = Repository.filterListings(
                listings,
                minPrice = filters.minPrice,
                maxPrice = filters.maxPrice,
                furnishing = filters.furnishing.ifBlank { null }
            )

            isLoading = false
        }
    }

    // Initial load + saved IDs
    LaunchedEffect(Unit) {
        loadListings(reset = true)
        val saved = Repository.fetchSaved()
        savedIds = saved.map { it.listing_id }.toSet()
    }

    // Reload when server-side filters change
    LaunchedEffect(filters.locality, filters.bhk, filters.propertyType) {
        loadListings(reset = true)
    }

    // Re-filter when client-side filters change
    LaunchedEffect(filters.minPrice, filters.maxPrice, filters.furnishing) {
        filteredListings = Repository.filterListings(
            listings,
            minPrice = filters.minPrice,
            maxPrice = filters.maxPrice,
            furnishing = filters.furnishing.ifBlank { null }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(IvyColors.Background)) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(IvyColors.Background)
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Column {
                Text(
                    "Discover Properties",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-0.5).sp,
                    color = IvyColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${filteredListings.size} out of $total exclusive properties in Bangalore",
                    fontSize = 14.sp,
                    color = IvyColors.TextSecondary
                )
            }
        }

        // Filters
        FilterBar(
            filters = filters,
            localities = localities,
            onFiltersChanged = { filters = it }
        )

        Divider(color = IvyColors.Border, thickness = 0.5.dp)

        // Listings list
        if (isLoading && listings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = IvyColors.Primary)
            }
        } else if (filteredListings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏠", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No listings match your filters", color = IvyColors.TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(onClick = { filters = FilterState() }) {
                        Text("Clear filters", color = IvyColors.Primary)
                    }
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredListings, key = { it.listing_id }) { listing ->
                    ListingCard(
                        listing = listing,
                        isSaved = listing.listing_id in savedIds,
                        onSaveToggle = {
                            scope.launch {
                                if (listing.listing_id in savedIds) {
                                    Repository.unsaveListing(listing.listing_id)
                                    savedIds = savedIds - listing.listing_id
                                } else {
                                    Repository.saveListing(listing.listing_id)
                                    savedIds = savedIds + listing.listing_id
                                }
                            }
                        },
                        onClick = { onListingClick(listing.listing_id) }
                    )
                }

                // Load more
                if (hasMore && !isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            Button(
                                onClick = { loadListings() },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = IvyColors.SurfaceVariant,
                                    contentColor = IvyColors.Primary
                                )
                            ) {
                                Text("Load More")
                            }
                        }
                    }
                }

                if (isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = IvyColors.Primary, modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
    }
}

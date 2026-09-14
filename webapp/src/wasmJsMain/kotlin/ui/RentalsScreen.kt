package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.Rental
import data.Repository
import kotlinx.coroutines.launch
import theme.IvyColors
import ui.components.*

@Composable
fun RentalsScreen(onRentalClick: (String) -> Unit) {
    var rentals by remember { mutableStateOf<List<Rental>>(emptyList()) }
    var total by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var hasMore by remember { mutableStateOf(false) }
    var offset by remember { mutableStateOf(0) }
    var filters by remember { mutableStateOf(FilterState()) }

    val localities = remember {
        listOf(
            "whitefield", "koramangala", "hsr layout", "indiranagar", "jayanagar",
            "jp nagar", "electronic city", "marathahalli", "bellandur", "sarjapur road",
            "hebbal", "yelahanka", "bannerghatta road", "rajajinagar", "malleshwaram"
        )
    }
    val scope = rememberCoroutineScope()

    fun loadRentals(reset: Boolean = false) {
        scope.launch {
            isLoading = true
            val newOffset = if (reset) 0 else offset
            val response = Repository.fetchRentals(
                locality = filters.locality.ifBlank { null },
                bhk = filters.bhk,
                offset = newOffset
            )
            if (reset) {
                rentals = response.results
                offset = response.results.size
            } else {
                rentals = rentals + response.results
                offset += response.results.size
            }
            total = response.total
            hasMore = response.has_more
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadRentals(reset = true) }
    LaunchedEffect(filters.locality, filters.bhk) { loadRentals(reset = true) }

    // Client-side filter for furnishing
    val filtered = remember(rentals, filters.furnishing) {
        if (filters.furnishing.isBlank()) rentals
        else rentals.filter { it.furnishing == filters.furnishing }
    }

    Column(modifier = Modifier.fillMaxSize().background(IvyColors.Background)) {
        Box(
            modifier = Modifier.fillMaxWidth().background(IvyColors.Surface).padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Column {
                Text("Rentals", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = IvyColors.TextPrimary)
                Text("${filtered.size} of $total rentals • Bangalore", fontSize = 13.sp, color = IvyColors.TextSecondary)
            }
        }

        FilterBar(
            filters = filters,
            localities = localities,
            showPriceFilters = false,
            onFiltersChanged = { filters = it }
        )

        Divider(color = IvyColors.Border, thickness = 0.5.dp)

        if (isLoading && rentals.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = IvyColors.Primary)
            }
        } else if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No rentals found", color = IvyColors.TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.listing_id }) { rental ->
                    RentalCard(rental = rental, onClick = { onRentalClick(rental.listing_id) })
                }
                if (hasMore && !isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            Button(
                                onClick = { loadRentals() },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = IvyColors.SurfaceVariant, contentColor = IvyColors.Primary)
                            ) { Text("Load More") }
                        }
                    }
                }
            }
        }
    }
}

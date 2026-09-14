package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import ui.components.PriceFormatter

/**
 * Client-side computed insights dashboard.
 * /v1/analytics/summary returns 404 (docs lie), so we compute everything from listings data.
 */
@Composable
fun InsightsScreen() {
    var isLoading by remember { mutableStateOf(true) }
    var totalListings by remember { mutableStateOf(0) }
    var liveListings by remember { mutableStateOf(0) }
    var medianPrice by remember { mutableStateOf(0L) }
    var medianPricePerSqft by remember { mutableStateOf(0L) }
    var byLocality by remember { mutableStateOf<List<LocalityStats>>(emptyList()) }
    var byBhk by remember { mutableStateOf<List<BhkStats>>(emptyList()) }
    var corruptCount by remember { mutableStateOf(0) }
    var totalRentals by remember { mutableStateOf(0) }
    var totalProjects by remember { mutableStateOf(0) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true

            // Fetch a large batch for analysis
            val allListings = mutableListOf<Listing>()
            var offset = 0
            var hasMore = true
            while (hasMore && offset < 500) { // First 500 for quick insights
                val response = Repository.fetchListings(offset = offset, limit = 50)
                allListings.addAll(response.results)
                hasMore = response.has_more
                offset += response.results.size
                if (response.results.isEmpty()) break
            }

            // Get totals
            val listingResponse = Repository.fetchListings(limit = 1)
            totalListings = listingResponse.total
            val rentalResponse = Repository.fetchRentals(limit = 1)
            totalRentals = rentalResponse.total
            val projectResponse = Repository.fetchProjects(limit = 1)
            totalProjects = projectResponse.total

            // Compute stats from fetched data
            val validListings = allListings.filter { it.price > 0 && it.is_live }
            liveListings = allListings.count { it.is_live }
            corruptCount = allListings.count { it.price < 0 }

            // Median price
            val sortedPrices = validListings.map { it.price }.sorted()
            medianPrice = if (sortedPrices.isNotEmpty()) sortedPrices[sortedPrices.size / 2] else 0

            // Median price per sqft
            val pricesPerSqft = validListings.filter { it.carpet_area > 100 }
                .map { it.price / it.carpet_area }
                .sorted()
            medianPricePerSqft = if (pricesPerSqft.isNotEmpty()) pricesPerSqft[pricesPerSqft.size / 2] else 0

            // By locality
            byLocality = validListings
                .groupBy { it.locality ?: "unknown" }
                .map { (locality, listings) ->
                    val prices = listings.map { it.price }.sorted()
                    LocalityStats(
                        locality = locality,
                        count = listings.size,
                        medianPrice = prices[prices.size / 2]
                    )
                }
                .sortedByDescending { it.count }
                .take(15)

            // By BHK
            byBhk = validListings
                .groupBy { it.bedroom }
                .map { (bhk, listings) ->
                    val prices = listings.map { it.price }.sorted()
                    BhkStats(
                        bedroom = bhk,
                        count = listings.size,
                        medianPrice = prices[prices.size / 2]
                    )
                }
                .sortedBy { it.bedroom }

            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(IvyColors.Background)) {
        Box(
            modifier = Modifier.fillMaxWidth().background(IvyColors.Surface).padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Column {
                Text("Market Insights", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = IvyColors.TextPrimary)
                Text("Bangalore Real Estate Dashboard", fontSize = 13.sp, color = IvyColors.TextSecondary)
            }
        }

        Divider(color = IvyColors.Border, thickness = 0.5.dp)

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = IvyColors.Primary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Computing insights from live data…", fontSize = 13.sp, color = IvyColors.TextSecondary)
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Summary cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard("Total Listings", totalListings.toString(), "Live: $liveListings", Modifier.weight(1f))
                    MetricCard("Rentals", totalRentals.toString(), "", Modifier.weight(1f))
                    MetricCard("Projects", totalProjects.toString(), "", Modifier.weight(1f))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard("Median Price", PriceFormatter.formatRupees(medianPrice), "Sale listings", Modifier.weight(1f))
                    MetricCard("₹/sq.ft", PriceFormatter.formatRupees(medianPricePerSqft), "Median", Modifier.weight(1f))
                    MetricCard("Corrupt", corruptCount.toString(), "Negative prices", Modifier.weight(1f))
                }

                // By Locality
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = IvyColors.Surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("By Locality", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = IvyColors.TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Top localities by listing count (sample)", fontSize = 12.sp, color = IvyColors.TextMuted)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Header
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("Locality", modifier = Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = IvyColors.TextSecondary)
                            Text("Count", modifier = Modifier.width(60.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = IvyColors.TextSecondary)
                            Text("Median", modifier = Modifier.width(100.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = IvyColors.TextSecondary)
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = IvyColors.Border)

                        byLocality.forEach { stat ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Text(
                                    stat.locality.replaceFirstChar { it.uppercase() },
                                    modifier = Modifier.weight(1f),
                                    fontSize = 13.sp,
                                    color = IvyColors.TextPrimary
                                )
                                Text(
                                    stat.count.toString(),
                                    modifier = Modifier.width(60.dp),
                                    fontSize = 13.sp,
                                    color = IvyColors.TextSecondary
                                )
                                Text(
                                    PriceFormatter.formatRupees(stat.medianPrice),
                                    modifier = Modifier.width(100.dp),
                                    fontSize = 13.sp,
                                    color = IvyColors.Primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // By BHK
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = IvyColors.Surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("By BHK", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = IvyColors.TextPrimary)
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("BHK", modifier = Modifier.weight(1f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = IvyColors.TextSecondary)
                            Text("Count", modifier = Modifier.width(60.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = IvyColors.TextSecondary)
                            Text("Median", modifier = Modifier.width(100.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = IvyColors.TextSecondary)
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = IvyColors.Border)

                        byBhk.forEach { stat ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "${stat.bedroom} BHK",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = IvyColors.TextPrimary
                                    )
                                }
                                Text(
                                    stat.count.toString(),
                                    modifier = Modifier.width(60.dp),
                                    fontSize = 13.sp,
                                    color = IvyColors.TextSecondary
                                )
                                Text(
                                    PriceFormatter.formatRupees(stat.medianPrice),
                                    modifier = Modifier.width(100.dp),
                                    fontSize = 13.sp,
                                    color = IvyColors.Primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Visual bar
                            val maxCount = byBhk.maxOf { it.count }
                            if (maxCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(stat.count.toFloat() / maxCount)
                                        .height(4.dp)
                                        .background(IvyColors.Primary.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                                )
                            }
                        }
                    }
                }

                // Note about data
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = IvyColors.SurfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "ℹ️ Note: Analytics are computed client-side from sampled data. " +
                                "The /v1/analytics/summary endpoint documented in the API reference does not exist (returns 404).",
                            fontSize = 12.sp,
                            color = IvyColors.TextMuted,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun MetricCard(title: String, value: String, subtitle: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = IvyColors.Surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 11.sp, color = IvyColors.TextMuted, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = IvyColors.Primary)
            if (subtitle.isNotBlank()) {
                Text(subtitle, fontSize = 11.sp, color = IvyColors.TextSecondary)
            }
        }
    }
}

data class LocalityStats(val locality: String, val count: Int, val medianPrice: Long)
data class BhkStats(val bedroom: Int, val count: Int, val medianPrice: Long)

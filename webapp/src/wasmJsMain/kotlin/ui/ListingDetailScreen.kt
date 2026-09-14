package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.Listing
import data.Repository
import kotlinx.coroutines.launch
import theme.IvyColors
import ui.components.*

@Composable
fun ListingDetailScreen(
    listingId: String,
    onBack: () -> Unit
) {
    var listing by remember { mutableStateOf<Listing?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaved by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(listingId) {
        isLoading = true
        listing = Repository.fetchListing(listingId)
        isSaved = Repository.isSaved(listingId)
        isLoading = false
    }

    Column(modifier = Modifier.fillMaxSize().background(IvyColors.Background)) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(IvyColors.Surface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(IvyColors.SurfaceVariant)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Text("←", fontSize = 18.sp, color = IvyColors.TextPrimary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Property Details",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = IvyColors.TextPrimary,
                modifier = Modifier.weight(1f)
            )
            // Save button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isSaved) IvyColors.Heart.copy(alpha = 0.15f) else IvyColors.SurfaceVariant)
                    .clickable {
                        scope.launch {
                            if (isSaved) {
                                Repository.unsaveListing(listingId)
                            } else {
                                Repository.saveListing(listingId)
                            }
                            isSaved = !isSaved
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Save",
                    tint = if (isSaved) IvyColors.Heart else IvyColors.HeartOutline,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = IvyColors.Primary)
            }
        } else if (listing == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Listing not found", color = IvyColors.TextSecondary)
            }
        } else {
            val l = listing!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Price header
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = IvyColors.Surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = PriceFormatter.formatRupees(l.price),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = IvyColors.Primary
                        )
                        if (l.carpet_area > 0 && l.price > 0) {
                            val pricePerSqft = l.price / l.carpet_area
                            Text(
                                text = "₹${pricePerSqft}/sq.ft",
                                fontSize = 14.sp,
                                color = IvyColors.TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = l.apartment_name ?: "Unknown Property",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = IvyColors.TextPrimary
                        )
                        Text(
                            text = (l.locality ?: "").replaceFirstChar { it.uppercase() } + ", Bangalore",
                            fontSize = 14.sp,
                            color = IvyColors.TextSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            l.property_type?.let { PropertyTag(it) }
                            l.furnishing?.let { FurnishingTag(it) }
                            if (l.is_verified) StatusTag("✓ Verified", IvyColors.TagVerified, IvyColors.Success)
                            if (!l.is_live) StatusTag("Inactive", IvyColors.TagNotLive, IvyColors.Error)
                        }
                    }
                }

                // Property specs
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = IvyColors.Surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Property Details", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = IvyColors.TextPrimary)
                        Spacer(modifier = Modifier.height(12.dp))

                        DetailRow("Bedrooms", "${l.bedroom} BHK")
                        DetailRow("Bathrooms", "${l.bathroom}")
                        DetailRow("Balconies", "${l.balcony}")
                        DetailRow("Floor", "${l.floor} of ${l.total_floors}")
                        DetailRow("Carpet Area", PriceFormatter.formatArea(l.carpet_area))
                        DetailRow("Super Built-up", PriceFormatter.formatArea(l.super_built_up_area))
                        DetailRow("Facing", l.facing_direction ?: "N/A")
                        DetailRow("Parking", "${l.covered_parking} covered")
                        DetailRow("Furnished", l.furnishing ?: "N/A")
                    }
                }

                // Description
                l.description?.let { desc ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = IvyColors.Surface)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Description", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = IvyColors.TextPrimary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(desc, fontSize = 14.sp, color = IvyColors.TextSecondary, lineHeight = 22.sp)
                        }
                    }
                }

                // Contact info
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = IvyColors.Surface)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Contact", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = IvyColors.TextPrimary)
                        Spacer(modifier = Modifier.height(12.dp))
                        DetailRow("Posted By", "${l.posted_by_name ?: "N/A"} (${l.posted_by ?: ""})")
                        DetailRow("Contact", l.posted_by_contact ?: "N/A")
                        DetailRow("Posted At", l.posted_at?.take(10) ?: "N/A")
                        DetailRow("Source", l.website ?: "N/A")
                        l.project_id?.let { DetailRow("Project ID", it) }
                        DetailRow("Listing ID", l.listing_id)
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = IvyColors.TextMuted)
        Text(value, fontSize = 13.sp, color = IvyColors.TextPrimary, fontWeight = FontWeight.Medium)
    }
}

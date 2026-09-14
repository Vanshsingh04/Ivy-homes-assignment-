package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.Rental
import data.Repository
import theme.IvyColors
import ui.components.*

@Composable
fun RentalDetailScreen(
    rentalId: String,
    onBack: () -> Unit
) {
    var rental by remember { mutableStateOf<Rental?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(rentalId) {
        isLoading = true
        rental = Repository.fetchRental(rentalId)
        isLoading = false
    }

    Column(modifier = Modifier.fillMaxSize().background(IvyColors.Background)) {
        Row(
            modifier = Modifier.fillMaxWidth().background(IvyColors.Surface).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(IvyColors.SurfaceVariant).clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) { Text("←", fontSize = 18.sp, color = IvyColors.TextPrimary) }
            Spacer(modifier = Modifier.width(12.dp))
            Text("Rental Details", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = IvyColors.TextPrimary)
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = IvyColors.Primary)
            }
        } else if (rental == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Rental not found", color = IvyColors.TextSecondary)
            }
        } else {
            val r = rental!!
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Price
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = IvyColors.Surface)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(PriceFormatter.formatRent(r.price), fontSize = 32.sp, fontWeight = FontWeight.Bold, color = IvyColors.Primary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            if (r.deposit > 100) Text("Deposit: ${PriceFormatter.formatRupees(r.deposit)}", fontSize = 14.sp, color = IvyColors.TextSecondary)
                            if (r.maintenance > 0) Text("Maintenance: ₹${r.maintenance}/mo", fontSize = 14.sp, color = IvyColors.TextSecondary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(r.title ?: r.apartment_name ?: "Unknown", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = IvyColors.TextPrimary)
                        Text((r.locality ?: "").replaceFirstChar { it.uppercase() } + ", Bangalore", fontSize = 14.sp, color = IvyColors.TextSecondary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            r.property_type?.let { PropertyTag(it) }
                            r.furnishing?.let { FurnishingTag(it) }
                        }
                    }
                }

                // Details
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = IvyColors.Surface)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Rental Details", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = IvyColors.TextPrimary)
                        Spacer(modifier = Modifier.height(12.dp))
                        DetailRow("Bedrooms", "${r.bedroom} BHK")
                        DetailRow("Bathrooms", "${r.bathroom}")
                        DetailRow("Floor", "${r.floor} of ${r.total_floors}")
                        DetailRow("Carpet Area", PriceFormatter.formatArea(r.carpet_area))
                        DetailRow("Super Built-up", PriceFormatter.formatArea(r.super_builtup_area))
                        DetailRow("Facing", r.facing_direction ?: "N/A")
                        DetailRow("Furnished", r.furnishing ?: "N/A")
                    }
                }

                r.description?.let { desc ->
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = IvyColors.Surface)) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Description", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = IvyColors.TextPrimary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(desc, fontSize = 14.sp, color = IvyColors.TextSecondary, lineHeight = 22.sp)
                        }
                    }
                }

                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = IvyColors.Surface)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Contact", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = IvyColors.TextPrimary)
                        Spacer(modifier = Modifier.height(12.dp))
                        DetailRow("Posted By", "${r.posted_by_name ?: "N/A"} (${r.posted_by ?: ""})")
                        DetailRow("Contact", r.posted_by_contact ?: "N/A")
                        DetailRow("Posted At", r.posted_at?.take(10) ?: "N/A")
                        DetailRow("Source", r.website ?: "N/A")
                        DetailRow("Listing ID", r.listing_id)
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

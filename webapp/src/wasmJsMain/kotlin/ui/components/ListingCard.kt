package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.Listing
import data.Rental
import data.Project
import theme.IvyColors

@Composable
fun ListingCard(
    listing: Listing,
    isSaved: Boolean = false,
    onSaveToggle: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = IvyColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Top row: price + save
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = PriceFormatter.formatRupees(listing.price),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-0.5).sp,
                    color = IvyColors.TextPrimary
                )
                // Save button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isSaved) IvyColors.Heart.copy(alpha = 0.15f) else IvyColors.SurfaceVariant)
                        .clickable(onClick = onSaveToggle),
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

            Spacer(modifier = Modifier.height(8.dp))

            // Apartment name
            Text(
                text = listing.apartment_name ?: "Unknown",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = IvyColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Locality
            Text(
                text = (listing.locality ?: "").replaceFirstChar { it.uppercase() },
                fontSize = 13.sp,
                color = IvyColors.TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Property details row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailChip("${listing.bedroom} BHK")
                DetailChip("${listing.bathroom} Bath")
                DetailChip(PriceFormatter.formatArea(listing.carpet_area))
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tags row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listing.property_type?.let { PropertyTag(it) }
                listing.furnishing?.let { FurnishingTag(it) }
                if (listing.is_verified) {
                    StatusTag("✓ Verified", IvyColors.TagVerified, IvyColors.Success)
                }
                if (!listing.is_live) {
                    StatusTag("Inactive", IvyColors.TagNotLive, IvyColors.Error)
                }
            }

            // Floor info
            if (listing.floor > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Floor ${listing.floor} of ${listing.total_floors} • ${listing.facing_direction ?: ""} facing",
                    fontSize = 12.sp,
                    color = IvyColors.TextMuted
                )
            }
        }
    }
}

@Composable
fun RentalCard(
    rental: Rental,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = IvyColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = PriceFormatter.formatRent(rental.price),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-0.5).sp,
                    color = IvyColors.TextPrimary
                )
                val depositText = if (rental.deposit > 100) {
                    "Deposit: ${PriceFormatter.formatRupees(rental.deposit)}"
                } else {
                    "Deposit: N/A"
                }
                Text(
                    text = depositText,
                    fontSize = 12.sp,
                    color = IvyColors.TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = rental.title ?: rental.apartment_name ?: "Unknown",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = IvyColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = (rental.locality ?: "").replaceFirstChar { it.uppercase() },
                fontSize = 13.sp,
                color = IvyColors.TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailChip("${rental.bedroom} BHK")
                DetailChip("${rental.bathroom} Bath")
                DetailChip(PriceFormatter.formatArea(rental.carpet_area))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rental.furnishing?.let { FurnishingTag(it) }
                if (rental.maintenance > 0) {
                    DetailChip("Maint: ₹${rental.maintenance}")
                }
            }
        }
    }
}

@Composable
fun ProjectCard(
    project: Project,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = IvyColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Price range
            val minP = minOf(project.price_min, project.price_max)
            val maxP = maxOf(project.price_min, project.price_max)
            Text(
                text = "${PriceFormatter.formatCrores(minP)} - ${PriceFormatter.formatCrores(maxP)}",
                fontSize = 26.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = (-0.5).sp,
                color = IvyColors.TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = project.apartment_name ?: "Unknown",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = IvyColors.TextPrimary
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = (project.locality ?: "").replaceFirstChar { it.uppercase() },
                    fontSize = 13.sp,
                    color = IvyColors.TextSecondary
                )
                project.developer_name?.let {
                    Text(" • by $it", fontSize = 13.sp, color = IvyColors.TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailChip("${project.total_units} Units")
                DetailChip("${project.total_towers} Towers")
                DetailChip("${project.min_area_sqft}-${project.max_area_sqft} sq.ft")
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                project.project_status?.let { StatusTag(it.replaceFirstChar { c -> c.uppercase() },
                    when(it) {
                        "ready to move" -> IvyColors.TagLive
                        "under construction" -> Color(0xFFFFC107).copy(alpha = 0.15f)
                        else -> IvyColors.TagBg
                    },
                    when(it) {
                        "ready to move" -> IvyColors.Success
                        "under construction" -> IvyColors.Warning
                        else -> IvyColors.TextSecondary
                    }
                )}
                DetailChip("${project.total_listings} Listings")
            }

            project.rera_number?.let {
                Spacer(modifier = Modifier.height(6.dp))
                Text("RERA: $it", fontSize = 11.sp, color = IvyColors.TextMuted)
            }
        }
    }
}

@Composable
fun DetailChip(text: String) {
    Box(
        modifier = Modifier
            .background(IvyColors.SurfaceVariant, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text = text, fontSize = 12.sp, color = IvyColors.TextSecondary)
    }
}

@Composable
fun PropertyTag(type: String) {
    val label = type.replaceFirstChar { it.uppercase() }
    Box(
        modifier = Modifier
            .background(IvyColors.SurfaceVariant, RoundedCornerShape(6.dp))
            .border(0.5.dp, IvyColors.Border, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = IvyColors.TextPrimary)
    }
}

@Composable
fun FurnishingTag(furnishing: String) {
    val label = furnishing.replaceFirstChar { it.uppercase() }
    Box(
        modifier = Modifier
            .background(IvyColors.SurfaceVariant, RoundedCornerShape(6.dp))
            .border(0.5.dp, IvyColors.Border, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = IvyColors.TextPrimary)
    }
}

@Composable
fun StatusTag(text: String, bg: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text = text, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = textColor)
    }
}

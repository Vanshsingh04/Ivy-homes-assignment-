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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.Project
import data.Repository
import theme.IvyColors
import ui.components.*

import androidx.compose.foundation.layout.ExperimentalLayoutApi

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProjectDetailScreen(
    projectId: String,
    onBack: () -> Unit
) {
    var project by remember { mutableStateOf<Project?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(projectId) {
        isLoading = true
        project = Repository.fetchProject(projectId)
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
            Text("Project Details", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = IvyColors.TextPrimary)
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = IvyColors.Primary)
            }
        } else if (project == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Project not found", color = IvyColors.TextSecondary)
            }
        } else {
            val p = project!!
            val minP = minOf(p.price_min, p.price_max)
            val maxP = maxOf(p.price_min, p.price_max)

            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = IvyColors.Surface)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "${PriceFormatter.formatCrores(minP)} - ${PriceFormatter.formatCrores(maxP)}",
                            fontSize = 28.sp, fontWeight = FontWeight.Bold, color = IvyColors.Primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(p.apartment_name ?: "Unknown", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = IvyColors.TextPrimary)
                        Row {
                            Text((p.locality ?: "").replaceFirstChar { it.uppercase() }, fontSize = 14.sp, color = IvyColors.TextSecondary)
                            p.developer_name?.let { Text(" • by $it", fontSize = 14.sp, color = IvyColors.TextMuted) }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        p.project_status?.let {
                            StatusTag(
                                it.replaceFirstChar { c -> c.uppercase() },
                                when (it) { "ready to move" -> IvyColors.TagLive; "under construction" -> Color(0xFFFFC107).copy(alpha = 0.15f); else -> IvyColors.TagBg },
                                when (it) { "ready to move" -> IvyColors.Success; "under construction" -> IvyColors.Warning; else -> IvyColors.TextSecondary }
                            )
                        }
                    }
                }

                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = IvyColors.Surface)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Project Details", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = IvyColors.TextPrimary)
                        Spacer(modifier = Modifier.height(12.dp))
                        DetailRow("Total Units", "${p.total_units}")
                        DetailRow("Towers", "${p.total_towers}")
                        DetailRow("Floors", "${p.total_floors}")
                        DetailRow("Area Range", "${p.min_area_sqft} - ${p.max_area_sqft} sq.ft")
                        DetailRow("Available Listings", "${p.total_listings}")
                        DetailRow("Launch Date", p.launch_date ?: "N/A")
                        DetailRow("Possession", p.possession_date ?: "N/A")
                        DetailRow("RERA", p.rera_number ?: "N/A")
                        DetailRow("Project ID", p.project_id)
                    }
                }

                if (p.amenities.isNotEmpty()) {
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = IvyColors.Surface)) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Amenities", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = IvyColors.TextPrimary)
                            Spacer(modifier = Modifier.height(12.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                p.amenities.forEach { amenity ->
                                    DetailChip(amenity.replaceFirstChar { it.uppercase() })
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

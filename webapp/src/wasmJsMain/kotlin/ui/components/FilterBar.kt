package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import theme.IvyColors

data class FilterState(
    val locality: String = "",
    val bhk: Int? = null,
    val minPrice: Long? = null,
    val maxPrice: Long? = null,
    val furnishing: String = "",
    val propertyType: String = ""
)

@Composable
fun FilterBar(
    filters: FilterState,
    localities: List<String> = emptyList(),
    showPriceFilters: Boolean = true,
    showFurnishing: Boolean = true,
    onFiltersChanged: (FilterState) -> Unit
) {
    // A horizontal scrolling row of modern pill filters
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(IvyColors.Background)
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Locality Pill
        item {
            PillDropdown(
                label = if (filters.locality.isEmpty()) "Any Locality" else filters.locality.replaceFirstChar { it.uppercase() },
                options = listOf("") + localities,
                displayText = { if (it.isEmpty()) "Any Locality" else it.replaceFirstChar { c -> c.uppercase() } },
                onSelected = { onFiltersChanged(filters.copy(locality = it)) }
            )
        }

        // BHK Pill
        item {
            PillDropdown(
                label = filters.bhk?.let { "$it BHK" } ?: "Any BHK",
                options = listOf("", "1", "2", "3", "4", "5"),
                displayText = { if (it.isEmpty()) "Any BHK" else "$it BHK" },
                onSelected = { onFiltersChanged(filters.copy(bhk = it.toIntOrNull())) }
            )
        }
        
        // Furnishing Pill
        if (showFurnishing) {
            item {
                PillDropdown(
                    label = if (filters.furnishing.isEmpty()) "Any Furnishing" else filters.furnishing.replaceFirstChar { it.uppercase() },
                    options = listOf("", "unfurnished", "semi-furnished", "fully-furnished"),
                    displayText = { if (it.isEmpty()) "Any Furnishing" else it.replaceFirstChar { c -> c.uppercase() } },
                    onSelected = { onFiltersChanged(filters.copy(furnishing = it)) }
                )
            }
        }
        
        // Price Filter is trickier to put in a simple dropdown, so we'll use a slightly different pill that acts as a simple min/max bounds for now or just a dropdown of standard ranges.
        // For the assignment, we can make the Price a Dropdown with preset ranges to be extremely clean.
        if (showPriceFilters) {
            item {
                PillDropdown(
                    label = priceLabel(filters.minPrice, filters.maxPrice),
                    options = listOf(
                        "Any Price",
                        "Under ₹1 Cr",
                        "₹1 Cr - ₹3 Cr",
                        "₹3 Cr - ₹5 Cr",
                        "Above ₹5 Cr"
                    ),
                    displayText = { it },
                    onSelected = { option ->
                        when (option) {
                            "Under ₹1 Cr" -> onFiltersChanged(filters.copy(minPrice = null, maxPrice = 10000000))
                            "₹1 Cr - ₹3 Cr" -> onFiltersChanged(filters.copy(minPrice = 10000000, maxPrice = 30000000))
                            "₹3 Cr - ₹5 Cr" -> onFiltersChanged(filters.copy(minPrice = 30000000, maxPrice = 50000000))
                            "Above ₹5 Cr" -> onFiltersChanged(filters.copy(minPrice = 50000000, maxPrice = null))
                            else -> onFiltersChanged(filters.copy(minPrice = null, maxPrice = null))
                        }
                    }
                )
            }
        }
    }
}

fun priceLabel(min: Long?, max: Long?): String {
    if (min == null && max == null) return "Any Price"
    if (min == null && max == 10000000L) return "Under ₹1 Cr"
    if (min == 10000000L && max == 30000000L) return "₹1 Cr - ₹3 Cr"
    if (min == 30000000L && max == 50000000L) return "₹3 Cr - ₹5 Cr"
    if (min == 50000000L && max == null) return "Above ₹5 Cr"
    return "Custom Price"
}

@Composable
fun PillDropdown(
    label: String,
    options: List<String>,
    displayText: (String) -> String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    val isActive = label != "Any Locality" && label != "Any BHK" && label != "Any Furnishing" && label != "Any Price"
    
    val bgColor = if (isActive) IvyColors.Primary.copy(alpha = 0.1f) else IvyColors.SurfaceVariant
    val borderColor = if (isActive) IvyColors.Primary else IvyColors.Border
    val textColor = if (isActive) IvyColors.Primary else IvyColors.TextPrimary

    Box {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(bgColor)
                .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Dropdown",
                modifier = Modifier.size(16.dp),
                tint = textColor
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(IvyColors.SurfaceElevated)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = displayText(option),
                            fontSize = 13.sp,
                            color = IvyColors.TextPrimary
                        )
                    },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

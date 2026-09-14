package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.Listing
import data.Repository
import kotlinx.coroutines.launch
import theme.IvyColors
import ui.components.ListingCard

@Composable
fun SavedScreen(onListingClick: (String) -> Unit) {
    var savedListings by remember { mutableStateOf<List<Listing>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    fun loadSaved() {
        scope.launch {
            isLoading = true
            savedListings = Repository.fetchSaved()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { loadSaved() }

    Column(modifier = Modifier.fillMaxSize().background(IvyColors.Background)) {
        Box(
            modifier = Modifier.fillMaxWidth().background(IvyColors.Surface).padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Column {
                Text("Saved Properties", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = IvyColors.TextPrimary)
                Text("${savedListings.size} saved", fontSize = 13.sp, color = IvyColors.TextSecondary)
            }
        }

        Divider(color = IvyColors.Border, thickness = 0.5.dp)

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = IvyColors.Primary)
            }
        } else if (savedListings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Empty",
                        modifier = Modifier.size(64.dp),
                        tint = IvyColors.TextMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No saved properties yet", fontSize = 16.sp, color = IvyColors.TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Tap the heart icon on any listing to save it", fontSize = 13.sp, color = IvyColors.TextMuted)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(savedListings, key = { it.listing_id }) { listing ->
                    ListingCard(
                        listing = listing,
                        isSaved = true,
                        onSaveToggle = {
                            scope.launch {
                                Repository.unsaveListing(listing.listing_id)
                                loadSaved()
                            }
                        },
                        onClick = { onListingClick(listing.listing_id) }
                    )
                }
            }
        }
    }
}

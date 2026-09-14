package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import navigation.NavController
import navigation.Screen
import navigation.rememberNavController
import theme.IvyColors
import ui.components.PremiumHeader

@Composable
fun App() {
    val navController = rememberNavController()
    var isLoggedIn by remember { mutableStateOf(false) }

    if (!isLoggedIn) {
        LoginScreen(
            onLoginSuccess = {
                isLoggedIn = true
                navController.navigateAndClear(Screen.Listings)
            }
        )
    } else {
        // Determine current tab for bottom nav
        val currentTab = remember(navController.currentScreen) {
            when (navController.currentScreen) {
                is Screen.Listings, is Screen.ListingDetail -> "listings"
                is Screen.Rentals, is Screen.RentalDetail -> "rentals"
                is Screen.Projects, is Screen.ProjectDetail -> "projects"
                is Screen.Saved -> "saved"
                is Screen.Insights -> "insights"
                else -> "listings"
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(IvyColors.Background)
        ) {
            PremiumHeader(
                scrolled = false,
                currentTab = currentTab,
                onTabSelected = { tab ->
                    val screen = when (tab) {
                        "listings" -> Screen.Listings
                        "rentals" -> Screen.Rentals
                        "projects" -> Screen.Projects
                        "saved" -> Screen.Saved
                        "insights" -> Screen.Insights
                        else -> Screen.Listings
                    }
                    navController.navigateTab(screen)
                },
                onNavigate = { route -> 
                    if (route == "listings") {
                        navController.navigateAndClear(Screen.Listings)
                    } else if (route == "login") {
                        isLoggedIn = false
                    }
                }
            )
            
            // Screen content
            Box(modifier = Modifier.weight(1f)) {
                when (val screen = navController.currentScreen) {
                    is Screen.Login -> {
                        LoginScreen(onLoginSuccess = {
                            isLoggedIn = true
                            navController.navigateAndClear(Screen.Listings)
                        })
                    }
                    is Screen.Listings -> {
                        ListingsScreen(
                            onListingClick = { id ->
                                navController.navigate(Screen.ListingDetail(id))
                            }
                        )
                    }
                    is Screen.ListingDetail -> {
                        ListingDetailScreen(
                            listingId = screen.listingId,
                            onBack = { navController.goBack() }
                        )
                    }
                    is Screen.Rentals -> {
                        RentalsScreen(
                            onRentalClick = { id ->
                                navController.navigate(Screen.RentalDetail(id))
                            }
                        )
                    }
                    is Screen.RentalDetail -> {
                        RentalDetailScreen(
                            rentalId = screen.rentalId,
                            onBack = { navController.goBack() }
                        )
                    }
                    is Screen.Projects -> {
                        ProjectsScreen(
                            onProjectClick = { id ->
                                navController.navigate(Screen.ProjectDetail(id))
                            }
                        )
                    }
                    is Screen.ProjectDetail -> {
                        ProjectDetailScreen(
                            projectId = screen.projectId,
                            onBack = { navController.goBack() }
                        )
                    }
                    is Screen.Saved -> {
                        SavedScreen(
                            onListingClick = { id ->
                                navController.navigate(Screen.ListingDetail(id))
                            }
                        )
                    }
                    is Screen.Insights -> {
                        InsightsScreen()
                    }
                }
            }

        }
    }
}


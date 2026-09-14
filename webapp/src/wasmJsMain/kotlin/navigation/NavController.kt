package navigation

import androidx.compose.runtime.*

/** Simple sealed class for screens/destinations */
sealed class Screen {
    data object Login : Screen()
    data object Listings : Screen()
    data class ListingDetail(val listingId: String) : Screen()
    data object Rentals : Screen()
    data class RentalDetail(val rentalId: String) : Screen()
    data object Projects : Screen()
    data class ProjectDetail(val projectId: String) : Screen()
    data object Saved : Screen()
    data object Insights : Screen()
}

/** Simple stack-based navigation for Compose/Wasm (no external library needed). */
class NavController {
    private val _backStack = mutableStateListOf<Screen>()
    val currentScreen: Screen
        get() = _backStack.lastOrNull() ?: Screen.Login

    val canGoBack: Boolean
        get() = _backStack.size > 1

    fun navigate(screen: Screen) {
        _backStack.add(screen)
    }

    fun navigateAndClear(screen: Screen) {
        _backStack.clear()
        _backStack.add(screen)
    }

    fun goBack(): Boolean {
        if (_backStack.size > 1) {
            _backStack.removeLast()
            return true
        }
        return false
    }

    /** Navigate to a tab destination, clearing back stack to just root + this screen */
    fun navigateTab(screen: Screen) {
        val root = _backStack.firstOrNull() ?: screen
        _backStack.clear()
        if (root != screen) {
            _backStack.add(root)
        }
        _backStack.add(screen)
    }
}

@Composable
fun rememberNavController(): NavController {
    return remember { NavController() }
}

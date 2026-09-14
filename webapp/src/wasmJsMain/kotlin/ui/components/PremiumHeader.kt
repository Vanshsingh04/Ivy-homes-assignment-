package ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.TokenStorage
import theme.IvyColors

@Composable
fun PremiumHeader(
    scrolled: Boolean,
    currentTab: String = "",
    onTabSelected: (String) -> Unit = {},
    onNavigate: (String) -> Unit
) {
    var mobileMenuOpen by remember { mutableStateOf(false) }

    val bgColor by animateColorAsState(
        if (scrolled) IvyColors.Surface.copy(alpha = 0.95f) else IvyColors.Background
    )

    Surface(
        modifier = Modifier.fillMaxWidth().height(72.dp),
        color = bgColor,
        contentColor = IvyColors.TextPrimary,
        shadowElevation = if (scrolled) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Wordmark
            Text(
                text = "IVY HOMES",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = IvyColors.Secondary,
                letterSpacing = 2.sp,
                modifier = Modifier.clickable { onNavigate("listings") }
            )

            // Desktop Nav
            Row(
                modifier = Modifier.weight(1f).padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TopNavTab("Buy", "listings", currentTab, onTabSelected)
                TopNavTab("Rent", "rentals", currentTab, onTabSelected)
                TopNavTab("Projects", "projects", currentTab, onTabSelected)
                TopNavTab("Saved", "saved", currentTab, onTabSelected)
                TopNavTab("Insights", "insights", currentTab, onTabSelected)
            }

            // Actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Theme Toggle
                TextButton(onClick = { IvyColors.setDarkMode(!IvyColors.isDark) }) {
                    Text(
                        text = if (IvyColors.isDark) "Light Mode" else "Dark Mode",
                        color = IvyColors.TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Auth
                if (TokenStorage.hasValidSession()) {
                    val email = TokenStorage.getUserEmail() ?: "Profile"
                    var profileMenuOpen by remember { mutableStateOf(false) }

                    Box {
                        OutlinedButton(
                            onClick = { profileMenuOpen = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = IvyColors.Secondary),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(email, fontSize = 13.sp)
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
                        }

                        DropdownMenu(
                            expanded = profileMenuOpen,
                            onDismissRequest = { profileMenuOpen = false },
                            modifier = Modifier.background(IvyColors.SurfaceElevated)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Logout", color = IvyColors.Error) },
                                onClick = {
                                    profileMenuOpen = false
                                    TokenStorage.clearAll()
                                    onNavigate("login")
                                }
                            )
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { onNavigate("login") },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = IvyColors.Secondary)
                    ) {
                        Text("Sign In")
                    }
                    Button(
                        onClick = { onNavigate("login") },
                        colors = ButtonDefaults.buttonColors(containerColor = IvyColors.Primary, contentColor = IvyColors.OnPrimary)
                    ) {
                        Text("Get Started")
                    }
                }

                // Mobile toggle placeholder
                IconButton(onClick = { mobileMenuOpen = !mobileMenuOpen }, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = IvyColors.Secondary)
                }
            }
        }
    }
}

@Composable
private fun TopNavTab(label: String, tabId: String, currentTab: String, onTabSelected: (String) -> Unit) {
    val isSelected = currentTab == tabId
    Text(
        text = label,
        fontSize = 14.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        color = if (isSelected) IvyColors.Primary else IvyColors.TextSecondary,
        modifier = Modifier
            .clickable { onTabSelected(tabId) }
            .padding(vertical = 8.dp, horizontal = 4.dp)
    )
}

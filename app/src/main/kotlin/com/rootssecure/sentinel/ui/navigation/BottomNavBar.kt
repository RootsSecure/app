package com.rootssecure.sentinel.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.rootssecure.sentinel.ui.theme.Background
import com.rootssecure.sentinel.ui.theme.GlassBorder
import com.rootssecure.sentinel.ui.theme.OnSurfaceVariant
import com.rootssecure.sentinel.ui.theme.SurfaceContainer
import com.rootssecure.sentinel.ui.theme.TealPrimary

private data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val navItems = listOf(
    NavItem(Screen.Dashboard.route,    "Dashboard", Icons.Filled.Dashboard),
    NavItem(Screen.Timeline.route,     "Alerts",    Icons.Filled.NotificationsActive),
    NavItem(Screen.Provisioning.route, "Setup",     Icons.Filled.Settings)
)

@Composable
fun BottomNavBar(navController: NavController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute   = backStackEntry?.destination?.route

    NavigationBar(
        containerColor = SurfaceContainer,
        tonalElevation = androidx.compose.ui.unit.Dp.Unspecified
    ) {
        navItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick  = {
                    if (!selected) {
                        navController.navigate(item.route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text       = item.label,
                        fontSize   = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor       = TealPrimary,
                    selectedTextColor       = TealPrimary,
                    unselectedIconColor     = OnSurfaceVariant,
                    unselectedTextColor     = OnSurfaceVariant,
                    indicatorColor          = Color(0xFF55D8E1).copy(alpha = 0.12f)
                )
            )
        }
    }
}

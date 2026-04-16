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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.rootssecure.sentinel.ui.theme.OnSurfaceVariant
import com.rootssecure.sentinel.ui.theme.SurfaceContainer
import com.rootssecure.sentinel.ui.theme.TealPrimary

private data class NavItem(
    val label: String,
    val icon: ImageVector
)

private val navItems = listOf(
    NavItem("Dashboard", Icons.Filled.Dashboard),
    NavItem("Health",    Icons.Filled.Analytics),
    NavItem("Alerts",    Icons.Filled.NotificationsActive),
    NavItem("Settings",  Icons.Filled.Settings)
)

@Composable
fun BottomNavBar(
    activePageIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = SurfaceContainer,
        tonalElevation = androidx.compose.ui.unit.Dp.Unspecified
    ) {
        navItems.forEachIndexed { index, item ->
            val selected = activePageIndex == index
            NavigationBarItem(
                selected = selected,
                onClick  = {
                    if (!selected) {
                        onTabSelected(index)
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

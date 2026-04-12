package com.rootssecure.sentinel.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rootssecure.sentinel.ui.screen.alertdetail.AlertDetailScreen
import com.rootssecure.sentinel.ui.screen.dashboard.DashboardScreen
import com.rootssecure.sentinel.ui.screen.provisioning.ProvisioningScreen
import com.rootssecure.sentinel.ui.screen.timeline.TimelineScreen

/**
 * Root navigation host. Contains the [Scaffold] with [BottomNavBar] and
 * manages transitions between the four main screens.
 *
 * The bottom bar is hidden for full-screen destinations like [AlertDetailScreen].
 */
@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = Screen.Dashboard.route,
            modifier         = Modifier.padding(innerPadding),
            enterTransition  = { fadeIn(animationSpec = tween(220)) },
            exitTransition   = { fadeOut(animationSpec = tween(180)) }
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen()
            }

            composable(Screen.Timeline.route) {
                TimelineScreen(
                    onAlertClick = { alertId ->
                        navController.navigate(Screen.AlertDetail.createRoute(alertId))
                    }
                )
            }

            composable(
                route = Screen.AlertDetail.route,
                arguments = listOf(navArgument("alertId") { type = NavType.StringType }),
                enterTransition = { slideInHorizontally { it } + fadeIn() },
                exitTransition  = { slideOutHorizontally { it } + fadeOut() }
            ) { backStackEntry ->
                val alertId = backStackEntry.arguments?.getString("alertId") ?: return@composable
                AlertDetailScreen(
                    alertId    = alertId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Provisioning.route) {
                ProvisioningScreen()
            }
        }
    }
}

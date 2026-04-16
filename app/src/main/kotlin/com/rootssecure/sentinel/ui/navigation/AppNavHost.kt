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
import com.rootssecure.sentinel.ui.screen.health.HealthDeepDiveScreen
import com.rootssecure.sentinel.ui.screen.provisioning.ProvisioningScreen
import com.rootssecure.sentinel.ui.screen.settings.SettingsScreen
import com.rootssecure.sentinel.ui.screen.timeline.TimelineScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import com.rootssecure.sentinel.ui.theme.OnBackground

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

/**
 * Root navigation host. Contains the [Scaffold] with [BottomNavBar] and
 * manages transitions between the four main screens.
 *
 * The bottom bar is hidden for full-screen destinations like [AlertDetailScreen].
 */
@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController    = navController,
        startDestination = "main_tabs",
        enterTransition  = { fadeIn(animationSpec = tween(220)) },
        exitTransition   = { fadeOut(animationSpec = tween(180)) }
    ) {
        composable("main_tabs") {
            MainTabsScreen(navController)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MainTabsScreen(navController: androidx.navigation.NavController) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            BottomNavBar(
                activePageIndex = pagerState.currentPage,
                onTabSelected = { index ->
                    coroutineScope.launch { pagerState.animateScrollToPage(index) }
                }
            )
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(innerPadding).fillMaxSize()
        ) { page ->
            when (page) {
                0 -> DashboardScreen()
                1 -> HealthDeepDiveScreen()
                2 -> TimelineScreen(
                    onAlertClick = { alertId ->
                        navController.navigate(Screen.AlertDetail.createRoute(alertId))
                    }
                )
                3 -> SettingsScreen(
                    onNavigateToProvisioning = { navController.navigate(Screen.Provisioning.route) }
                )
            }
        }
    }
}

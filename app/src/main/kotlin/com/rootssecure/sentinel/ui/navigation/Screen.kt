package com.rootssecure.sentinel.ui.navigation

/** Sealed class defining all navigation routes in the app. */
sealed class Screen(val route: String) {
    object Dashboard    : Screen("dashboard")
    object Timeline     : Screen("timeline")
    object Provisioning : Screen("provisioning")

    /** Alert detail — takes a vendorEventId argument. */
    object AlertDetail  : Screen("timeline/{alertId}") {
        fun createRoute(alertId: String) = "timeline/$alertId"
    }
}

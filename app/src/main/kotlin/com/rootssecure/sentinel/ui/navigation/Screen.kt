package com.rootssecure.sentinel.ui.navigation

/** Sealed class defining all navigation routes in the app. */
sealed class Screen(val route: String) {
    object Dashboard    : Screen("dashboard")
    object Health       : Screen("health")
    object AlertInbox   : Screen("alerts")
    object Timeline     : Screen("timeline")
    object Settings     : Screen("settings")
    object Provisioning : Screen("provisioning")

    /** Alert detail — takes a vendorEventId argument. */
    object AlertDetail  : Screen("alerts/{alertId}") {
        fun createRoute(alertId: String) = "alerts/$alertId"
    }
}

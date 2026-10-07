package com.mydelivery.manager.navigation

import androidx.annotation.StringRes
import com.mydelivery.manager.R

sealed class Screen(val route: String, @StringRes val title: Int, val stage: Int) {
    data object Home : Screen("home", R.string.nav_home, 4)
    data object Deliveries : Screen("deliveries", R.string.nav_deliveries, 3)
    data object Customers : Screen("customers", R.string.nav_customers, 5)
    data object Map : Screen("map", R.string.nav_map, 6)
    data object More : Screen("more", R.string.nav_more, 1)
    data object Income : Screen("income", R.string.nav_income, 4)
    data object Cod : Screen("cod", R.string.nav_cod, 4)
    data object Expenses : Screen("expenses", R.string.nav_expenses, 4)
    data object Reports : Screen("reports", R.string.nav_reports, 4)
    data object Settings : Screen("settings", R.string.nav_settings, 10)

    companion object {
        /** Tabs shown in the bottom bar. */
        val bottomTabs = listOf(Home, Deliveries, Customers, Map, More)

        /** Screens reached through the More tab. */
        val moreItems = listOf(Income, Cod, Expenses, Reports, Settings)
    }
}

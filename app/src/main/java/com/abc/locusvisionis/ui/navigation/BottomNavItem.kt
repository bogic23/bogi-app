package com.abc.locusvisionis.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem(
        route = "home",
        title = "Home",
        icon = Icons.Default.Home
    )

    object MoneyManager : BottomNavItem(
        route = "money_manager",
        title = "Finance",
        icon = Icons.Default.AccountBalanceWallet
    )

    object Reflection : BottomNavItem(
        route = "reflection",
        title = "Reflection",
        icon = Icons.Default.SelfImprovement
    )

    object Bible : BottomNavItem(
        route = "bible",
        title = "Bible",
        icon = Icons.Default.MenuBook
    )

    object Profile : BottomNavItem(
        route = "profile",
        title = "Profile",
        icon = Icons.Default.Person
    )
}

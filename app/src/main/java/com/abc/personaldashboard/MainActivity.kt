package com.abc.personaldashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.abc.personaldashboard.ui.navigation.BottomNavItem
import com.abc.personaldashboard.ui.screens.BibleScreen
import com.abc.personaldashboard.ui.screens.HomeScreen
import com.abc.personaldashboard.ui.screens.MoneyManagerScreen
import com.abc.personaldashboard.ui.screens.ProfileScreen
import com.abc.personaldashboard.ui.screens.ReflectionScreen
import com.abc.personaldashboard.ui.theme.DashboardTheme
import com.abc.personaldashboard.ui.theme.PersonalDashboardTheme
import com.abc.personaldashboard.ui.theme.ThemeOption

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var themeOption by rememberSaveable { mutableStateOf(ThemeOption.Cyan) }

            PersonalDashboardTheme(themeOption = themeOption) {
                MainApp(
                    themeOption = themeOption,
                    onThemeChange = { themeOption = it }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    themeOption: ThemeOption,
    onThemeChange: (ThemeOption) -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val appColors = DashboardTheme.colors

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = appColors.surface,
                tonalElevation = 8.dp
            ) {
                val items = listOf(
                    BottomNavItem.Home,
                    BottomNavItem.MoneyManager,
                    BottomNavItem.Reflection,
                    BottomNavItem.Bible,
                    BottomNavItem.Profile
                )

                items.forEach { item ->
                    val selected = currentRoute == item.route
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = appColors.primary,
                            selectedTextColor = appColors.primary,
                            unselectedIconColor = appColors.textSecondary,
                            unselectedTextColor = appColors.textSecondary,
                            indicatorColor = appColors.primary.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(BottomNavItem.Home.route) { HomeScreen() }
            composable(BottomNavItem.MoneyManager.route) { MoneyManagerScreen() }
            composable(BottomNavItem.Reflection.route) { ReflectionScreen() }
            composable(BottomNavItem.Bible.route) { BibleScreen() }
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    selectedTheme = themeOption,
                    onThemeChange = onThemeChange
                )
            }
        }
    }
}

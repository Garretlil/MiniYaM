package com.example.miniyam

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

sealed class Screen(val route: String, val icon: ImageVector, val label: String) {
    data object Home : Screen("home", Icons.Default.Home, "Главная")
    data object Search : Screen("search", Icons.Default.Search, "Поиск")
    data object Library : Screen("library", Icons.Default.LibraryMusic, "Моя музыка")
    data object Profile : Screen("profile", Icons.Default.Person, "Профиль")
}

@Composable
fun BottomNavBar(navController: NavHostController) {
    val items = listOf(
        Screen.Home,
        Screen.Search,
        Screen.Library,
        Screen.Profile
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = currentRoute == screen.route,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}
@Composable
fun BottomBar(navController: NavHostController) {
    var currentRoute by remember { mutableStateOf("home") }
    NavigationBar(containerColor = Color(0xB3F6F6F6), tonalElevation = 0.dp) {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = { navController.navigate("home"); currentRoute = "home" },
            icon = { Icon(Icons.Default.MusicNote, contentDescription = null,modifier = Modifier
                .size(28.dp)) },
            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent),
        )
        NavigationBarItem(
            selected = currentRoute == "search",
            onClick = { navController.navigate("search"); currentRoute = "search" },
            icon = { Icon(Icons.Default.Search, contentDescription = null,modifier = Modifier
                .size(28.dp)) },
            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent),
        )
        NavigationBarItem(
            selected = currentRoute == "library",
            onClick = { navController.navigate("library"); currentRoute = "library" },
            icon = { Icon(Icons.Default.HeartBroken, contentDescription = null,modifier = Modifier
                .size(28.dp)) },
            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent),
        )
        NavigationBarItem(
            selected = currentRoute == "profile",
            onClick = { navController.navigate("profile"); currentRoute = "profile" },
            icon = { Icon(Icons.Default.PersonOutline, contentDescription = null,modifier = Modifier
                .size(28.dp)) },
            colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent),
        )

    }
}
package dev.lrdcxdes.lfilms.ui

import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import dev.lrdcxdes.lfilms.R

@Composable
fun BottomNavigationBar(navController: NavHostController, onHomeClicked: () -> Unit = {}) {
    val resources = LocalContext.current.resources

    val items = listOf(
        BottomNavItem("home", resources.getString(R.string.home), Icons.Filled.Home),
        BottomNavItem("favorites", resources.getString(R.string.favorites), Icons.Filled.Favorite),
        BottomNavItem("history", resources.getString(R.string.history), Icons.Filled.List),
        BottomNavItem("settings", resources.getString(R.string.settings), Icons.Filled.Settings)
    )

    NavigationBar(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true

                        // if clicked on home icon, and already on home screen, load defaultList()
                        if (item.route == "home" && currentRoute == "home") {
                            onHomeClicked()
                        }
                    }
                }
            )
        }
    }
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

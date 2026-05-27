package com.aim.app.presentation.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun AimBottomBar(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination

    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
    ) {
        BottomTabs.forEach { tab ->
            val selected = currentDestination.isOnTab(tab)
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) navController.switchTab(tab)
                },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = null,
                    )
                },
                label = {
                    Text(
                        text = stringResource(tab.labelRes),
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}

private fun NavDestination?.isOnTab(tab: BottomTab): Boolean =
    this?.hierarchy?.any { it.hasRoute(tab.routeClass) } == true

// Reified `navigate<T>` резолвит T статически. Передача `tab.route` через sealed-родителя сделала
// бы T = AimRoute и сериализатор не совпал бы с дескриптором destination, зарегистрированным под
// конкретным data object. Smart-cast в `when` сужает тип до конкретного singleton'а — этого
// достаточно, чтобы reified-параметр стал нужным конкретным типом.
private fun NavHostController.switchTab(tab: BottomTab) {
    val applyTabOptions: NavOptionsBuilder.() -> Unit = {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
    when (val route = tab.route) {
        AimRoute.Today -> navigate(route, applyTabOptions)
        AimRoute.Tasks -> navigate(route, applyTabOptions)
        AimRoute.Goals -> navigate(route, applyTabOptions)
        AimRoute.Habits -> navigate(route, applyTabOptions)
        AimRoute.Dashboard -> navigate(route, applyTabOptions)
        else -> Unit // прочие маршруты не используются как вкладки нижней навигации
    }
}

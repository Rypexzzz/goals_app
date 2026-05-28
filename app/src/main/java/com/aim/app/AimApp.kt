package com.aim.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aim.app.presentation.navigation.AimBottomBar
import com.aim.app.presentation.navigation.AimNavHost
import com.aim.app.presentation.navigation.BottomTabs

@Composable
fun AimApp() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination

    val showBottomBar = remember(currentDestination) {
        currentDestination?.hierarchy?.any { dest ->
            BottomTabs.any { tab -> dest.hasRoute(tab.routeClass) }
        } ?: false
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        // Системные инсеты обрабатывают сами экраны (их TopAppBar) и нижняя навигация —
        // иначе статус-бар учитывался бы дважды и над хэдером появлялась пустая полоса.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            ) {
                AimBottomBar(navController = navController)
            }
        },
    ) { padding ->
        AimNavHost(
            navController = navController,
            modifier = Modifier.padding(padding),
        )
    }
}

package com.aim.app.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.aim.app.presentation.screens.dashboard.DashboardScreen
import com.aim.app.presentation.screens.goals.GoalsScreen
import com.aim.app.presentation.screens.habits.HabitsScreen
import com.aim.app.presentation.screens.settings.SettingsScreen
import com.aim.app.presentation.screens.today.TodayScreen

private const val SCREEN_ANIM_DURATION_MS = 220

@Composable
fun AimNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val openSettings: () -> Unit = { navController.navigate(AimRoute.Settings) }

    NavHost(
        navController = navController,
        startDestination = AimRoute.Today,
        modifier = modifier,
        enterTransition = { fadeIn(animationSpec = tween(SCREEN_ANIM_DURATION_MS)) },
        exitTransition = { fadeOut(animationSpec = tween(SCREEN_ANIM_DURATION_MS)) },
        popEnterTransition = { fadeIn(animationSpec = tween(SCREEN_ANIM_DURATION_MS)) },
        popExitTransition = { fadeOut(animationSpec = tween(SCREEN_ANIM_DURATION_MS)) },
    ) {
        composable<AimRoute.Today> {
            TodayScreen(onSettingsClick = openSettings)
        }
        composable<AimRoute.Goals> {
            GoalsScreen(onSettingsClick = openSettings)
        }
        composable<AimRoute.Habits> {
            HabitsScreen(onSettingsClick = openSettings)
        }
        composable<AimRoute.Dashboard> {
            DashboardScreen(onSettingsClick = openSettings)
        }
        composable<AimRoute.Settings>(
            enterTransition = {
                slideIntoContainer(SlideDirection.Start, tween(SCREEN_ANIM_DURATION_MS))
            },
            popExitTransition = {
                slideOutOfContainer(SlideDirection.End, tween(SCREEN_ANIM_DURATION_MS))
            },
        ) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}

package com.aim.app.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.aim.app.presentation.screens.archive.ArchiveScreen
import com.aim.app.presentation.screens.dashboard.DashboardScreen
import com.aim.app.presentation.screens.goaldetail.GoalDetailScreen
import com.aim.app.presentation.screens.goals.GoalsScreen
import com.aim.app.presentation.screens.habitdetail.HabitDetailScreen
import com.aim.app.presentation.screens.habits.HabitsScreen
import com.aim.app.presentation.screens.notificationsettings.NotificationSettingsScreen
import com.aim.app.presentation.screens.settings.SettingsScreen
import com.aim.app.presentation.screens.taskdetail.TaskDetailScreen
import com.aim.app.presentation.screens.today.TodayScreen
import com.aim.app.presentation.screens.trash.TrashScreen

private const val SCREEN_ANIM_DURATION_MS = 220

private val pushEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideIntoContainer(SlideDirection.Start, tween(SCREEN_ANIM_DURATION_MS))
}
private val pushExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutOfContainer(SlideDirection.Start, tween(SCREEN_ANIM_DURATION_MS))
}
private val popEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideIntoContainer(SlideDirection.End, tween(SCREEN_ANIM_DURATION_MS))
}
private val popExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutOfContainer(SlideDirection.End, tween(SCREEN_ANIM_DURATION_MS))
}

@Composable
fun AimNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val openSettings: () -> Unit = { navController.navigate(AimRoute.Settings) }
    val openGoal: (Long) -> Unit = { goalId -> navController.navigate(AimRoute.GoalDetail(goalId)) }
    val openTask: (Long) -> Unit = { taskId -> navController.navigate(AimRoute.TaskDetail(taskId)) }
    val openHabit: (Long) -> Unit = { habitId -> navController.navigate(AimRoute.HabitDetail(habitId)) }
    val openTrash: () -> Unit = { navController.navigate(AimRoute.Trash) }
    val openArchive: () -> Unit = { navController.navigate(AimRoute.Archive) }
    val openNotificationSettings: () -> Unit = { navController.navigate(AimRoute.NotificationSettings) }

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
            GoalsScreen(
                onSettingsClick = openSettings,
                onGoalClick = openGoal,
                onOpenTrash = openTrash,
                onOpenArchive = openArchive,
            )
        }
        composable<AimRoute.Habits> {
            HabitsScreen(
                onSettingsClick = openSettings,
                onHabitClick = openHabit,
            )
        }
        composable<AimRoute.Dashboard> {
            DashboardScreen(
                onSettingsClick = openSettings,
                onOpenHabit = openHabit,
                onOpenGoal = openGoal,
            )
        }
        composable<AimRoute.Settings>(
            enterTransition = pushEnter,
            exitTransition = pushExit,
            popEnterTransition = popEnter,
            popExitTransition = popExit,
        ) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onOpenArchive = openArchive,
                onOpenTrash = openTrash,
                onOpenNotificationSettings = openNotificationSettings,
            )
        }
        composable<AimRoute.NotificationSettings>(
            enterTransition = pushEnter,
            exitTransition = pushExit,
            popEnterTransition = popEnter,
            popExitTransition = popExit,
        ) {
            NotificationSettingsScreen(onBack = { navController.popBackStack() })
        }
        composable<AimRoute.GoalDetail>(
            enterTransition = pushEnter,
            exitTransition = pushExit,
            popEnterTransition = popEnter,
            popExitTransition = popExit,
        ) {
            GoalDetailScreen(
                onBack = { navController.popBackStack() },
                onOpenTask = openTask,
                onOpenHabit = openHabit,
            )
        }
        composable<AimRoute.TaskDetail>(
            enterTransition = pushEnter,
            exitTransition = pushExit,
            popEnterTransition = popEnter,
            popExitTransition = popExit,
        ) {
            TaskDetailScreen(
                onBack = { navController.popBackStack() },
                onOpenBreadcrumb = { breadcrumbTaskId ->
                    if (breadcrumbTaskId != null) {
                        navController.navigate(AimRoute.TaskDetail(breadcrumbTaskId))
                    } else {
                        navController.popBackStack()
                    }
                },
                onOpenSubtask = openTask,
            )
        }
        composable<AimRoute.HabitDetail>(
            enterTransition = pushEnter,
            exitTransition = pushExit,
            popEnterTransition = popEnter,
            popExitTransition = popExit,
        ) {
            HabitDetailScreen(
                onBack = { navController.popBackStack() },
                onOpenGoal = openGoal,
            )
        }
        composable<AimRoute.Trash>(
            enterTransition = pushEnter,
            exitTransition = pushExit,
            popEnterTransition = popEnter,
            popExitTransition = popExit,
        ) {
            TrashScreen(onBack = { navController.popBackStack() })
        }
        composable<AimRoute.Archive>(
            enterTransition = pushEnter,
            exitTransition = pushExit,
            popEnterTransition = popEnter,
            popExitTransition = popExit,
        ) {
            ArchiveScreen(
                onBack = { navController.popBackStack() },
                onOpenGoal = openGoal,
            )
        }
    }
}

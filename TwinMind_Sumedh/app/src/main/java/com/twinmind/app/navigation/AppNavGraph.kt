package com.twinmind.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.compose.*
import com.twinmind.app.ui.dashboard.DashboardScreen
import com.twinmind.app.ui.recording.RecordingScreen
import com.twinmind.app.ui.summary.SummaryScreen
import com.twinmind.app.ui.transcript.TranscriptScreen

sealed class Screen(val route: String) {
    data object Dashboard  : Screen("dashboard")
    data object Recording  : Screen("recording/{sessionId}") {
        fun createRoute(sessionId: String) = "recording/$sessionId"
    }
    data object Transcript : Screen("transcript/{sessionId}") {
        fun createRoute(sessionId: String) = "transcript/$sessionId"
    }
    data object Summary    : Screen("summary/{sessionId}") {
        fun createRoute(sessionId: String) = "summary/$sessionId"
    }
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onStartRecording = { sessionId ->
                    navController.navigate(Screen.Recording.createRoute(sessionId))
                },
                onSessionClick = { sessionId ->
                    navController.navigate(Screen.Summary.createRoute(sessionId))
                }
            )
        }

        composable(
            route = Screen.Recording.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { back ->
            val sessionId = back.arguments?.getString("sessionId") ?: return@composable
            RecordingScreen(
                sessionId    = sessionId,
                onStop       = { navController.navigate(Screen.Summary.createRoute(sessionId)) {
                    popUpTo(Screen.Dashboard.route)
                }},
                onNavigateUp = { navController.navigateUp() }
            )
        }

        composable(
            route = Screen.Transcript.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { back ->
            val sessionId = back.arguments?.getString("sessionId") ?: return@composable
            TranscriptScreen(
                sessionId    = sessionId,
                onNavigateUp = { navController.navigateUp() }
            )
        }

        composable(
            route = Screen.Summary.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { back ->
            val sessionId = back.arguments?.getString("sessionId") ?: return@composable
            SummaryScreen(
                sessionId         = sessionId,
                onViewTranscript  = { navController.navigate(Screen.Transcript.createRoute(sessionId)) },
                onNavigateUp      = { navController.navigateUp() }
            )
        }
    }
}
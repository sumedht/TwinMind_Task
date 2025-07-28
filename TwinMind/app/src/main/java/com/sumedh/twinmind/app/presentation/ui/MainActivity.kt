package com.sumedh.twinmind.app.presentation.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sumedh.twinmind.app.presentation.ui.home.HomeScreen
import com.sumedh.twinmind.app.presentation.ui.login.LoginScreen
import com.sumedh.twinmind.app.presentation.ui.meeting.MeetingScreen
import com.sumedh.twinmind.app.presentation.ui.meeting_details.MeetingDetailsScreen
import com.sumedh.twinmind.app.util.HandleAudioPermission
import com.sumedh.twinmind.ui.theme.TwinMindTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TwinMindTheme {
                var hasAudioPermission by remember { mutableStateOf(false) }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    // The HandleAudioPermission composable will manage the request.
                    HandleAudioPermission(
                        onPermissionGranted = { hasAudioPermission = true },
                        onPermissionDenied = {
                            // You could show an error message or close the app
                            // if the permission is essential.
                        }
                    )
                    // The rest of the app will only be shown if the permission is granted.
                    if (hasAudioPermission) {
                        AppNavigation()
                    } else {
                        // Optional: Show a screen explaining why the permission is needed.
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text("Audio permission is required to use this app.")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val isLoading by mainViewModel.isLoading.collectAsState()
    val startDestination by mainViewModel.startDestination.collectAsState()

    if (isLoading) {
        // Show a loading indicator while checking auth state
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        NavHost(navController = navController, startDestination = startDestination) {
            composable("login") {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onLoginError = {
                        Toast.makeText(navController.context,"Error in login", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            composable("home") {
                HomeScreen(
                    onCaptureClick = { navController.navigate("meeting") },
                    onMeetingClick = { meetingId ->
                        // Navigate to details screen with the meeting ID
                        navController.navigate("meeting_details/$meetingId")
                    }
                )
            }

            composable("meeting") {
                // This is where MeetingScreen gets created when you navigate to it
                MeetingScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(
                route = "meeting_details/{meetingId}",
                arguments = listOf(navArgument("meetingId") { type = NavType.LongType })
            ) {
                MeetingDetailsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onChatClick = { /* TODO: Navigate to chat */ }
                )
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TwinMindTheme {
        Greeting("Android")
    }
}
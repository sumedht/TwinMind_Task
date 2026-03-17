package com.twinmind.app.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.twinmind.app.data.db.entity.RecordingSessionEntity
import com.twinmind.app.data.db.entity.SessionStatus
import com.twinmind.app.ui.dashboard.*
import com.twinmind.app.ui.theme.TwinMindTheme
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyStateShownWhenNoSessions() {
        val vm = mockk<DashboardViewModel>(relaxed = true)
        every { vm.uiState } returns MutableStateFlow(
            DashboardUiState(sessions = emptyList(), isLoading = false)
        )
        composeRule.setContent {
            TwinMindTheme { DashboardScreen(onStartRecording = {}, onSessionClick = {}, viewModel = vm) }
        }
        composeRule.onNodeWithText("No recordings yet").assertIsDisplayed()
    }

    @Test
    fun sessionCardShownForEachSession() {
        val vm = mockk<DashboardViewModel>(relaxed = true)
        every { vm.uiState } returns MutableStateFlow(
            DashboardUiState(
                sessions = listOf(
                    RecordingSessionEntity("1", "Meeting Alpha", 1000L, status = SessionStatus.STOPPED.name),
                    RecordingSessionEntity("2", "Meeting Beta",  2000L, status = SessionStatus.STOPPED.name)
                ),
                isLoading = false
            )
        )
        composeRule.setContent {
            TwinMindTheme { DashboardScreen(onStartRecording = {}, onSessionClick = {}, viewModel = vm) }
        }
        composeRule.onNodeWithText("Meeting Alpha").assertIsDisplayed()
        composeRule.onNodeWithText("Meeting Beta").assertIsDisplayed()
    }

    @Test
    fun fabClickCallsOnStartRecording() {
        val vm      = mockk<DashboardViewModel>(relaxed = true)
        val clicked = mutableListOf<String>()
        every { vm.uiState } returns MutableStateFlow(DashboardUiState(isLoading = false))
        every { vm.generateNewSessionId() } returns "new-session-id"
        composeRule.setContent {
            TwinMindTheme {
                DashboardScreen(
                    onStartRecording = { clicked.add(it) },
                    onSessionClick   = {},
                    viewModel        = vm
                )
            }
        }
        composeRule.onNodeWithContentDescription("Start Recording").performClick()
        assert(clicked.contains("new-session-id"))
    }

    @Test
    fun sessionCardClickCallsOnSessionClick() {
        val vm      = mockk<DashboardViewModel>(relaxed = true)
        val clicked = mutableListOf<String>()
        every { vm.uiState } returns MutableStateFlow(
            DashboardUiState(
                sessions = listOf(RecordingSessionEntity("id-1", "Click Me", 1000L)),
                isLoading = false
            )
        )
        composeRule.setContent {
            TwinMindTheme {
                DashboardScreen(
                    onStartRecording = {},
                    onSessionClick   = { clicked.add(it) },
                    viewModel        = vm
                )
            }
        }
        composeRule.onNodeWithText("Click Me").performClick()
        assert(clicked.contains("id-1"))
    }
}
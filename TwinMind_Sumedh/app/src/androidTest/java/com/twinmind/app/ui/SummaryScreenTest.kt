package com.twinmind.app.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.twinmind.app.data.db.entity.SummaryEntity
import com.twinmind.app.ui.summary.*
import com.twinmind.app.ui.theme.TwinMindTheme
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SummaryScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun buildVm(state: SummaryScreenState): SummaryViewModel {
        val vm = mockk<SummaryViewModel>(relaxed = true)
        every { vm.screenState } returns MutableStateFlow(state)
        return vm
    }

    @Test
    fun loadingStateShowsSpinner() {
        composeRule.setContent {
            TwinMindTheme {
                SummaryScreen("s1", {}, {}, viewModel = buildVm(SummaryScreenState.Loading))
            }
        }
        composeRule.onNodeWithText("Generating summary...").assertIsDisplayed()
    }

    @Test
    fun transcribingStateShowsCorrectMessage() {
        composeRule.setContent {
            TwinMindTheme {
                SummaryScreen("s1", {}, {}, viewModel = buildVm(SummaryScreenState.Transcribing))
            }
        }
        composeRule.onNodeWithText("Transcribing audio...").assertIsDisplayed()
    }

    @Test
    fun completeStateRendersSections() {
        val summary = SummaryEntity(
            sessionId   = "s1",
            title       = "Q3 Sync",
            summary     = "We discussed targets.",
            actionItems = """["Follow up", "Schedule call"]""",
            keyPoints   = """["Budget needed", "On track"]""",
            isComplete  = true
        )
        composeRule.setContent {
            TwinMindTheme {
                SummaryScreen("s1", {}, {}, viewModel = buildVm(SummaryScreenState.Complete(summary)))
            }
        }
        composeRule.onNodeWithText("Q3 Sync").assertIsDisplayed()
        composeRule.onNodeWithText("We discussed targets.").assertIsDisplayed()
        composeRule.onNodeWithText("Action Items").assertIsDisplayed()
        composeRule.onNodeWithText("Key Points").assertIsDisplayed()
        composeRule.onNodeWithText("Follow up").assertIsDisplayed()
        composeRule.onNodeWithText("Budget needed").assertIsDisplayed()
    }

    @Test
    fun errorStateShowsRetryButton() {
        composeRule.setContent {
            TwinMindTheme {
                SummaryScreen("s1", {}, {},
                    viewModel = buildVm(SummaryScreenState.Error("Connection timeout"))
                )
            }
        }
        composeRule.onNodeWithText("Retry").assertIsDisplayed()
        composeRule.onNodeWithText("Connection timeout").assertIsDisplayed()
    }

    @Test
    fun retryButtonCallsViewModelRetry() {
        val vm = buildVm(SummaryScreenState.Error("Error"))
        composeRule.setContent {
            TwinMindTheme { SummaryScreen("s1", {}, {}, viewModel = vm) }
        }
        composeRule.onNodeWithText("Retry").performClick()
        verify(exactly = 1) { vm.retry() }
    }

    @Test
    fun transcriptButtonNavigatesToTranscript() {
        val calls = mutableListOf<Unit>()
        composeRule.setContent {
            TwinMindTheme {
                SummaryScreen(
                    sessionId        = "s1",
                    onViewTranscript = { calls.add(Unit) },
                    onNavigateUp     = {},
                    viewModel        = buildVm(SummaryScreenState.Complete(
                        SummaryEntity(sessionId = "s1", isComplete = true)
                    ))
                )
            }
        }
        composeRule.onNodeWithText("Transcript").performClick()
        assert(calls.size == 1)
    }
}
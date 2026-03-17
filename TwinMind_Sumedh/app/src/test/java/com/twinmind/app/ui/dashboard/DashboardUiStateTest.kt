package com.twinmind.app.ui.dashboard

import app.cash.turbine.test
import com.twinmind.app.data.db.entity.RecordingSessionEntity
import com.twinmind.app.data.repository.AudioRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var repo     : AudioRepository
    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        repo      = mockk()
        every { repo.observeAllSessions() } returns flowOf(emptyList())
        viewModel = DashboardViewModel(repo)
    }

    @After
    fun teardown() { Dispatchers.resetMain() }

    @Test
    fun `initial state has isLoading true`() = runTest {
        viewModel.uiState.test {
            val initial = awaitItem()
            assertTrue(initial.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `sessions list updates when repository emits`() = runTest {
        val sessions = listOf(
            RecordingSessionEntity("1", "Meeting A", 1000L),
            RecordingSessionEntity("2", "Meeting B", 2000L)
        )
        every { repo.observeAllSessions() } returns flowOf(sessions)
        viewModel = DashboardViewModel(repo)

        viewModel.uiState.test {
            awaitItem() // loading
            val loaded = awaitItem()
            assertEquals(2, loaded.sessions.size)
            assertFalse(loaded.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `generateNewSessionId returns unique IDs`() {
        val id1 = viewModel.generateNewSessionId()
        val id2 = viewModel.generateNewSessionId()
        assertNotEquals(id1, id2)
    }

    @Test
    fun `generateNewSessionId returns valid UUID format`() {
        val id = viewModel.generateNewSessionId()
        assertTrue(id.matches(Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")))
    }
}
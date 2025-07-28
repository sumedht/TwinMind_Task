package com.sumedh.twinmind.app.presentation.ui.login

import org.junit.Assert.*

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.sumedh.twinmind.app.data.model.UpcomingEvent
import com.sumedh.twinmind.app.domain.model.Meeting
import com.sumedh.twinmind.app.domain.usecase.GetAllMeetingsUseCase
import com.sumedh.twinmind.app.domain.usecase.GetUpcomingEventsUseCase
import com.sumedh.twinmind.app.presentation.ui.home.HomeViewModel
import com.sumedh.twinmind.app.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.OffsetDateTime

@ExperimentalCoroutinesApi
class HomeViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: HomeViewModel
    private val mockGetUpcomingEventsUseCase: GetUpcomingEventsUseCase = mock()
    private val mockGetAllMeetingsUseCase: GetAllMeetingsUseCase = mock()
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init fetches events and memories successfully`() = runTest {
        // Arrange
        val events = listOf(UpcomingEvent("e1", "Event 1", OffsetDateTime.now(), OffsetDateTime.now()))
        val meetings = listOf(Meeting(1, "Meeting 1", OffsetDateTime.now()))
        whenever(mockGetUpcomingEventsUseCase.invoke()).thenReturn(Result.Success(events))
        whenever(mockGetAllMeetingsUseCase.invoke()).thenReturn(flowOf(meetings))

        // Act
        viewModel = HomeViewModel(mockGetUpcomingEventsUseCase, mockGetAllMeetingsUseCase)

        // Assert
        val uiState = viewModel.uiState.value
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.calendarEventsByDate).isNotEmpty()
        assertThat(uiState.memoriesByDate).isNotEmpty()
        assertThat(uiState.error).isNull()
    }

    @Test
    fun `fetchUpcomingEvents with error, uiState should contain error message`() = runTest {
        // Arrange
        val errorMessage = "Failed to fetch events"
        whenever(mockGetUpcomingEventsUseCase.invoke()).thenReturn(Result.Error(Exception(errorMessage)))
        whenever(mockGetAllMeetingsUseCase.invoke()).thenReturn(flowOf(emptyList()))

        // Act
        viewModel = HomeViewModel(mockGetUpcomingEventsUseCase, mockGetAllMeetingsUseCase)

        // Assert
        val uiState = viewModel.uiState.value
        assertThat(uiState.isLoading).isFalse()
        assertThat(uiState.error).isEqualTo(errorMessage)
    }
}
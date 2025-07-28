package com.sumedh.twinmind.app.domain.usecase

import com.sumedh.twinmind.app.data.model.UpcomingEvent
import com.sumedh.twinmind.app.domain.repository.CalendarRepository
import javax.inject.Inject
import com.sumedh.twinmind.app.util.Result

class GetUpcomingEventsUseCase @Inject constructor(
    private val calendarRepository: CalendarRepository
) {
    suspend operator fun invoke(): Result<List<UpcomingEvent>> {
        return calendarRepository.getUpcomingEvents()
    }
}
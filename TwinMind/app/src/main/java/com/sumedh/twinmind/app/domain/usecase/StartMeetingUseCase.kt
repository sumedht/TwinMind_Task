package com.sumedh.twinmind.app.domain.usecase

import com.sumedh.twinmind.app.domain.repository.MeetingRepository
import javax.inject.Inject

class StartMeetingUseCase @Inject constructor(
    private val repository: MeetingRepository
) {
    suspend operator fun invoke(title: String): Long {
        return repository.startMeeting(title)
    }
}
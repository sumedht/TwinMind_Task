package com.sumedh.twinmind.app.domain.usecase

import com.sumedh.twinmind.app.domain.repository.MeetingRepository
import javax.inject.Inject

class AddTranscriptUseCase @Inject constructor(
    private val repository: MeetingRepository
) {
    suspend operator fun invoke(meetingId: Long, text: String, timestamp: Long) {
        repository.addTranscript(meetingId, text, timestamp)
    }
}
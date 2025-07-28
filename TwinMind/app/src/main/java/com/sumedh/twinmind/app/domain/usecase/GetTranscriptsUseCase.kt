package com.sumedh.twinmind.app.domain.usecase

import com.sumedh.twinmind.app.domain.model.Transcript
import com.sumedh.twinmind.app.domain.repository.MeetingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTranscriptsUseCase @Inject constructor(
    private val repository: MeetingRepository
) {
    operator fun invoke(meetingId: Long): Flow<List<Transcript>> {
        return repository.getTranscripts(meetingId)
    }
}
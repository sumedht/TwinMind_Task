package com.sumedh.twinmind.app.domain.usecase

import com.sumedh.twinmind.app.domain.model.Meeting
import com.sumedh.twinmind.app.domain.repository.MeetingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllMeetingsUseCase @Inject constructor(
    private val repository: MeetingRepository
) {
    operator fun invoke(): Flow<List<Meeting>> {
        return repository.getAllMeetings()
    }
}
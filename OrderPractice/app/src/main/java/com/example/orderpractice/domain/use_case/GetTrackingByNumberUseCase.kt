package com.example.orderpractice.domain.use_case

import com.example.orderpractice.common.Resource
import com.example.orderpractice.data.dto.toEvent
import com.example.orderpractice.domain.model.Event
import com.example.orderpractice.domain.repository.TrackingRepository
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class GetTrackingByNumberUseCase @Inject constructor(
    private val repository: TrackingRepository
) {
    fun invoke(): Flow<Resource<List<Event>>> = flow {
        try {
            emit(Resource.Loading())
            val events = repository.getTracking().map { it.toEvent() }
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage?:"An unexpected error occured"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't earch server. Check your internet connectivity"))
        }
    }
}
package com.example.orderpractice.data.dto

import com.example.orderpractice.domain.model.Event

data class EventDto(
    val courierCode: String,
    val datetime: String,
    val eventId: String,
    val eventTrackingNumber: String,
    val location: String,
    val occurrenceDatetime: String,
    val sourceCode: String,
    val status: String,
    val trackingNumber: String
)

fun EventDto.toEvent(): Event {
    return Event(
        courierCode =  courierCode,
        datetime = datetime,
        eventId = eventId,
        eventTrackingNumber = eventTrackingNumber,
        occurrenceDatetime = occurrenceDatetime,
        sourceCode =  sourceCode,
        status = status,
        trackingNumber = trackingNumber
    )
}
package com.example.orderpractice.domain.model

data class Event(
    val courierCode: String,
    val datetime: String,
    val eventId: String,
    val eventTrackingNumber: String,
    val occurrenceDatetime: String,
    val sourceCode: String,
    val status: String,
    val trackingNumber: String
)
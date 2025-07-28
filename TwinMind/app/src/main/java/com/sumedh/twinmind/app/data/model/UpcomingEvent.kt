package com.sumedh.twinmind.app.data.model

import java.time.OffsetDateTime

data class UpcomingEvent(
    val id: String,
    val title: String,
    val startTime: OffsetDateTime,
    val endTime: OffsetDateTime
)
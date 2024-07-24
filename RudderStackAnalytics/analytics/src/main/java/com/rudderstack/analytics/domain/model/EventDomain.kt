package com.rudderstack.analytics.domain.model

data class EventDomain(
    val name: String,
    val properties: Map<String, Any>
)
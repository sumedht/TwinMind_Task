package com.rudderstack.analytics.data

import com.rudderstack.analytics.data.model.Event
import java.util.concurrent.LinkedBlockingQueue

class EventQueue {
    private val queue = LinkedBlockingQueue<Event>()

    fun addEvent(event: Event) {
        queue.put(event)
    }

    fun getEvent(): Event?{
        return queue.poll()
    }
}
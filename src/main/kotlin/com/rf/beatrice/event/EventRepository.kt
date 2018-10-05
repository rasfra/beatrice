package com.rf.beatrice.event

import org.joda.time.DateTime

interface EventRepository {
    fun get(id: Int): EventBooking
    fun list(): Collection<EventBooking>
    fun purge()
    fun book(user: String, date: DateTime, title: String)
    fun cancel(id: Int)
    fun registerChangeListener(callback: (EventChange) -> Unit)
}
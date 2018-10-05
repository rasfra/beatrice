package com.rf.beatrice.event

import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import java.util.*

class InMemoryEventRepository : EventRepository {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val events = ArrayList<EventBooking>()
    private val callbacks = ArrayList<(EventChange) -> Unit>()


    override fun get(id: Int): EventBooking {
        return events.first { it.id == id }
    }

    override fun list(): Collection<EventBooking> {
        return events.sortedBy { it.date }
    }

    override fun purge() {
        logger.info("Purging outdated events")
        events.removeAll { it.date.isBeforeNow }
    }

    override fun book(user: String, date: DateTime, title: String) {
        logger.info("Booking $user, $date, $title")
        val id = Random().nextInt()
        events.add(EventBooking(id, user, date, title))
        notify(id, EventChange.Type.ADD)
    }

    override fun cancel(id: Int) {
        logger.info("Cancel event $id")
        events.removeAll { it.id == id }
        notify(id, EventChange.Type.REMOVE)
    }

    private fun notify(id: Int, type: EventChange.Type) {
        logger.info("Notifying changes on event $id")
        callbacks.forEach { it.invoke(EventChange(id, type)) }
    }

    override fun registerChangeListener(callback: (EventChange) -> Unit) {
        logger.info("Callback registered")
        callbacks.add { callback }
    }

    fun clear() {
        events.clear()
        callbacks.clear()
    }
}
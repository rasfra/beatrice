package com.rf.beatrice.event

import com.rf.beatrice.Events
import com.rf.beatrice.H2Event
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import java.util.*

class H2EventRepository(private val db: Database) : EventRepository {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val callbacks = ArrayList<(EventChange) -> Unit>()

    override fun get(id: Int): EventBooking {
        return transaction(db) {
            H2Event[id].toEventBooking()
        }
    }

    override fun list(): Collection<EventBooking> {
        return transaction(db) {
            H2Event.all().map { it.toEventBooking() }
        }
    }

    override fun purge() {
        logger.info("Purging events")
        transaction(db) {
            Events.deleteWhere { Events.date less DateTime.now() }
        }
    }

    override fun book(user: String, eventDate: DateTime, eventTitle: String) {
        logger.info("Booking event by $user at $eventDate with title $eventTitle")
        val id = transaction(db) {
            Events.insert {
                it[organizer] = user
                it[date] = eventDate
                it[title] = eventTitle
            } get Events.id
        }
        notify(id!!.value, EventChange.Type.ADD)
    }

    override fun cancel(id: Int) {
        logger.info("Cancel event $id")
        transaction(db) {
            Events.deleteWhere { Events.id eq id }
        }
        notify(id, EventChange.Type.REMOVE)
    }

    private fun notify(id: Int, type: EventChange.Type) {
        for (c in callbacks) {
            val event = EventChange(id, type)
            c.invoke(event)
        }
        //callbacks.forEach { it.invoke(EventChange(id, type)) }
    }

    override fun registerChangeListener(callback: (EventChange) -> Unit) {
        logger.info("Callback registered")
        callbacks.add(callback)
    }

    private fun H2Event.toEventBooking() = EventBooking(id.value, organizer, date, title)
}
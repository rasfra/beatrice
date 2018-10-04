package com.rf.quotebot.event

import org.jetbrains.exposed.sql.Database
import org.joda.time.DateTime

class H2EventRepository(private val database: Database) : EventRepository {
    override fun get(id: Int): EventBooking {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun list(): Collection<EventBooking> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun purge() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun book(user: String, date: DateTime, title: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun cancel(id: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun registerChangeListener(callback: (EventChange) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
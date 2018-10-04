package com.rf.quotebot.event

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

data class EventBooking(val id: Int, val user: String, val date: DateTime, val title: String) {
    private val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")
    private fun formatDate(): String = formatter.print(date)
    override fun toString(): String {
        return "${formatDate()} ($user) $title"
    }
}
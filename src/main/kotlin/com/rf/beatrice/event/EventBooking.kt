package com.rf.beatrice.event

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

data class EventBooking(val id: Int, val user: String, val date: DateTime, val title: String) {
    private val formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")
    private val timeFormatter = DateTimeFormat.forPattern("HH:mm")
    fun formatDate(): String = formatter.print(date)
    fun formatTime(): String = timeFormatter.print(date)
    fun description() = "$title ($user)"
    override fun toString(): String {
        return "${formatDate()} ($user) $title"
    }
}
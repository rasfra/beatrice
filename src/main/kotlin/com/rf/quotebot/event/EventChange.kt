package com.rf.quotebot.event

class EventChange(val id: Int, val type: Type) {
    enum class Type { ADD, REMOVE }
}

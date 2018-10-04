package com.rf.quotebot.conversation

import java.text.SimpleDateFormat
import java.util.*

class Conversation(
        val date: Date,
        val messages: List<ConversationMessage>,
        val title: String?,
        val source: Source,
        val uploadedBy: String?
) {
    fun text(): String {
        var str = ""
        for (m in messages) {
            if (m.from != null) {
                str += "${m.from}: ${m.text}\n"
            } else {
                str += "${m.text}\n"
            }
        }
        str += "($source)"
        return str
    }

    fun prettyDate(): String = SimpleDateFormat("yyyy-MM-dd").format(date)
    override fun toString(): String {
        return "Conversation '$title': size: ${messages.size}"
    }
}

enum class Source { TELEGRAM, IRC }
class ConversationMessage(val from: String? = null, val text: String)
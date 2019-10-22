package com.rf.beatrice.conversation

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class Conversation(
        val id: Int,
        val date: DateTime,
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
        str += "($source $id)"
        return str
    }

    fun prettyDate(): String = DateTimeFormat.forPattern("yyyy-MM-dd").print(date)
    override fun toString(): String {
        return "Conversation '$title': size: ${messages.size}"
    }
}

enum class Source { TELEGRAM, IRC }
class ConversationMessage(val from: String? = null, val text: String)
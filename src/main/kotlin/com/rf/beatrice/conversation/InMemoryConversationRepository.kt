package com.rf.beatrice.conversation

import org.apache.commons.lang3.StringUtils
import org.joda.time.DateTime
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class InMemoryConversationRepository : ConversationRepository {
    private val random = Random()
    private val conversations = ArrayList<Conversation>()
    private val ids = AtomicInteger(0)

    override fun all(): Collection<Conversation> {
        return conversations
    }

    override fun store(cMessages: List<ConversationMessage>, cTitle: String?,
                       cSource: Source, cUploadedBy: String?): Conversation {
        val c = Conversation(ids.incrementAndGet(), DateTime.now(), cMessages, cTitle, cSource, cUploadedBy)
        conversations.add(c)
        return c
    }

    override fun get(id: Int) = conversations.first { it.id == id }

    override fun search(s: String): Conversation? =
            conversations.filter { StringUtils.containsIgnoreCase(it.text(), s) or StringUtils.containsIgnoreCase(it.title, s) }
                    .random()


    override fun random(): Conversation? = conversations.random()

    override fun delete(id: Int): Boolean {
        return conversations.removeIf { it.id == id }
    }

    private fun List<Conversation>.random(): Conversation? =
            when {
                size > 1 -> get(random.nextInt(size - 1))
                size == 1 -> first()
                else -> null
            }

    fun clear() = conversations.clear()
}
package com.rf.beatrice.conversation

import org.apache.commons.lang3.StringUtils
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class InMemoryConversationRepository : ConversationRepository {
    private val random = Random()
    private val conversations = ArrayList<Conversation>()
    private val ids = AtomicInteger(0)

    override fun all(): Collection<Conversation> {
        return conversations
    }

    override fun store(c: Conversation) {
        conversations.add(c)
    }

    override fun get(id: Int): Conversation {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun search(s: String): Conversation? =
            conversations.filter { StringUtils.containsIgnoreCase(it.text(), s) }
                    .random()


    override fun random(): Conversation? = conversations.random()

    private fun List<Conversation>.random(): Conversation? =
            when {
                size > 1 -> get(random.nextInt(size - 1))
                size == 1 -> first()
                else -> null
            }

}
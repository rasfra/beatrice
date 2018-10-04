package com.rf.quotebot.conversation

import com.rf.quotebot.Conversations
import com.rf.quotebot.H2Conversation
import com.rf.quotebot.Messages
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

class H2ConversationRepository(private val database: Database) : ConversationRepository {
    override fun store(c: Conversation) {
        var id: EntityID<Int>? = null
        transaction(database) {
            id = Conversations.insert {
                it[title] = c.title
                it[date] = DateTime(c.date)
                it[src] = c.source.name
                it[uploadedBy] = c.uploadedBy
            } get Conversations.id

            c.messages.forEach { m ->
                Messages.insert {
                    it[conversation] = id!!
                    it[from] = m.from
                    it[text] = m.text
                }
            }
        }
    }

    override fun all(): Collection<Conversation> {
        return transaction(database) {
            H2Conversation.wrapRows(Conversations.selectAll()).map { toConversation(it) }
        }
    }

    override fun get(id: Int): Conversation {
        return transaction(database) {
            H2Conversation.findById(id)?.let { toConversation(it) }!!
        }
    }

    override fun search(s: String): Conversation? {
        return transaction(database) {
            H2Conversation.wrapRows(Conversations.innerJoin(Messages)
                    .select { Messages.text like "%$s%" or (Conversations.title like "%$s%") }
                    .orderBy(org.jetbrains.exposed.sql.Random())
                    .limit(1)
            ).firstOrNull()?.let { toConversation(it) }
        }
    }

    override fun random(): Conversation? {
        return transaction(database) {
            H2Conversation.wrapRows(Conversations
                    .selectAll()
                    .orderBy(org.jetbrains.exposed.sql.Random())
                    .limit(1))
                    .first()?.let { toConversation(it) }
        }
    }

    private fun toConversation(h2: H2Conversation): Conversation {
        return Conversation(
                h2.date.toDate(),
                h2.messages.map { ConversationMessage(it.from, it.text) },
                h2.title,
                Source.valueOf(h2.src),
                h2.uploadedBy)
    }
}
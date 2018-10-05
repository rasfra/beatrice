package com.rf.beatrice.conversation

import com.rf.beatrice.Conversations
import com.rf.beatrice.H2Conversation
import com.rf.beatrice.Messages
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

class H2ConversationRepository(private val database: Database) : ConversationRepository {
    override fun store(cMessages: List<ConversationMessage>, cTitle: String?,
                       cSource: Source, cUploadedBy: String?): Conversation {
        var id: EntityID<Int>? = null
        transaction(database) {
            id = Conversations.insert {
                it[title] = cTitle
                it[date] = DateTime.now()
                it[src] = cSource.name
                it[uploadedBy] = cUploadedBy
            } get Conversations.id

            cMessages.forEach { m ->
                Messages.insert {
                    it[conversation] = id!!
                    it[from] = m.from
                    it[text] = m.text
                }
            }
        }
        return H2Conversation[id!!].toConversation()
    }

    override fun all(): Collection<Conversation> {
        return transaction(database) {
            H2Conversation.wrapRows(Conversations.selectAll()).map { it.toConversation() }
        }
    }

    override fun get(id: Int): Conversation {
        return transaction(database) {
            H2Conversation.findById(id)?.let { it.toConversation() }!!
        }
    }

    override fun search(s: String): Conversation? {
        return transaction(database) {
            H2Conversation.wrapRows(Conversations.innerJoin(Messages)
                    .select { Messages.text like "%$s%" or (Conversations.title like "%$s%") }
                    .orderBy(org.jetbrains.exposed.sql.Random())
                    .limit(1)
            ).firstOrNull()?.let { it.toConversation() }
        }
    }

    override fun random(): Conversation? {
        return transaction(database) {
            H2Conversation.wrapRows(Conversations
                    .selectAll()
                    .orderBy(org.jetbrains.exposed.sql.Random())
                    .limit(1))
                    .firstOrNull()?.let { it.toConversation() }
        }
    }

    private fun H2Conversation.toConversation(): Conversation {
        return Conversation(
                id.value,
                date,
                messages.map { ConversationMessage(it.from, it.text) },
                title,
                Source.valueOf(src),
                uploadedBy)
    }
}
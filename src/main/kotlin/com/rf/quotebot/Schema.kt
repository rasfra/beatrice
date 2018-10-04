package com.rf.quotebot

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable

object Conversations : IntIdTable() {
    val title = varchar("title", 100).nullable()
    val date = datetime("date")
    val src = varchar("src", 30)
    val uploadedBy = varchar("from", 50).nullable()
}

class H2Conversation(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<H2Conversation>(Conversations)

    var title by Conversations.title
    var date by Conversations.date
    var src by Conversations.src
    val messages by H2Message referrersOn Messages.conversation
    val uploadedBy by Conversations.uploadedBy
}

object Messages : IntIdTable() {
    val conversation = reference("conversation", Conversations)
    val from = varchar("from", 50).nullable()
    val text = varchar("text", 2000)
}

class H2Message(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<H2Message>(Messages)

    var conversation by H2Conversation referencedOn Messages.conversation
    var from by Messages.from
    var text by Messages.text
}

object Events : IntIdTable() {
    val date = datetime("date")
    val title = varchar("title", 300)
    val organizer = varchar("from", 50)
}

class H2Event(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<H2Event>(Events)

    var date by Events.date
    var title by Events.title
    val organizer by Events.organizer
}
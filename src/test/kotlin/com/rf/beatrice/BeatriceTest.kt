package com.rf.beatrice

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.User
import com.rf.beatrice.conversation.ConversationMessage
import com.rf.beatrice.conversation.InMemoryConversationRepository
import com.rf.beatrice.conversation.Source
import com.rf.beatrice.event.EventBooking
import com.rf.beatrice.event.InMemoryEventRepository
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

class BeatriceTest {
    private val recordingMessageSender = RecordingMessageSender()
    private val conversationRepo = InMemoryConversationRepository()
    private val eventRepo = InMemoryEventRepository()
    private val beatrice = Beatrice(recordingMessageSender, conversationRepo, eventRepo)

    @Before
    fun init() {
        conversationRepo.clear()
        eventRepo.clear()
        recordingMessageSender.clear()

    }

    @Test
    fun randomFindsRandomQuote() {
        val conversation = conversationRepo.store(listOf(
                ConversationMessage("user1", "hi"),
                ConversationMessage("user2", "no")),
                "hello", Source.TELEGRAM, "user1")
        val updates = updates("from", "/random")
        beatrice.process(updates)
        assertEquals(recordingMessageSender.messages.size, 1)
        val text = recordingMessageSender.messages[0]
        assertTrue(text.contains(conversation.prettyDate()))
        assertTrue(text.contains(conversation.text()))
    }

    @Test
    fun findFindsQuote() {
        val conversation = conversationRepo.store(listOf(
                ConversationMessage("user1", "hi"),
                ConversationMessage("user2", "no")),
                "hello", Source.TELEGRAM, "user1")
        val updates = updates("from", "/find hello")
        beatrice.process(updates)
        assertEquals(recordingMessageSender.messages.size, 1)
        val text = recordingMessageSender.messages[0]
        assertTrue(text.contains(conversation.prettyDate()))
        assertTrue(text.contains(conversation.text()))
    }

    @Test
    fun findNoQuote() {
        val updates = updates("from", "/find hello")
        beatrice.process(updates)
        assertEquals(recordingMessageSender.messages.size, 1)
        val text = recordingMessageSender.messages[0]
        assertTrue(text.contains("Inga resultat"))
    }

    @Test
    fun bookEventSuccess() {
        val eventDate = LocalDate.now().plusDays(1).toDateTime(LocalTime.parse("18:00"))
        val title = "my title"
        val me = "me"
        val event = bookEvent(eventDate, title, me)
        assertEquals(event.date, eventDate)
        assertEquals(event.title, title)
        assertEquals(event.user, me)
    }

    @Test
    fun cancelEvent() {
        val event1 = bookEvent(DateTime.now().plusHours(8), "title", "me")
        val event2 = bookEvent(DateTime.now().plusHours(16), "title2", "me")
        assertEquals(eventRepo.list().size, 2)
        beatrice.process(updates("me", "/eventcancel"))
        beatrice.process(updates("me", event1.toString()))
        assertEquals(eventRepo.list().size, 1)
    }

    private fun bookEvent(date: DateTime, title: String, by: String): EventBooking {
        beatrice.process(updates(by, "/eventcreate"))
        assertEquals(recordingMessageSender.pop(), "Ange tid för eventet (dd/mm hh:mm)")

        val dateStr = DateTimeFormat.forPattern("dd/MM HH:mm").print(date)
        beatrice.process(updates(by, "$dateStr"))
        assertEquals(recordingMessageSender.pop(), "Ange titel för eventet")

        beatrice.process(updates(by, title))
        assertEquals(recordingMessageSender.pop(), "Boka *$dateStr - $title*?")

        beatrice.process(updates(by, "yupp"))
        assertEquals(recordingMessageSender.pop(), "*$dateStr - $title* **bokad!**")

        return eventRepo.list().first()
    }

    private fun update(from: String, text: String): Update {
        val from = mock<User> {
            on { username() } doReturn from
        }
        val message = mock<Message> {
            on { text() } doReturn text
            on { from() } doReturn from
        }
        return mock {
            on { message() } doReturn message
        }
    }

    private fun updates(from: String, text: String): List<Update> {
        return listOf(update(from, text))
    }
}
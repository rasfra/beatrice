package com.rf.beatrice

import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.Update
import com.rf.beatrice.conversation.Conversation
import com.rf.beatrice.conversation.ConversationRepository
import com.rf.beatrice.conversation.QuoteConversation
import com.rf.beatrice.event.EventBooker
import com.rf.beatrice.event.EventCanceller
import com.rf.beatrice.event.EventNotifier
import com.rf.beatrice.event.EventRepository
import org.slf4j.LoggerFactory


class Beatrice(private val messageSender: MessageSender,
               private val conversationRepository: ConversationRepository,
               private val eventRepository: EventRepository) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val eventNotifier = EventNotifier(messageSender, eventRepository)
    private val conversationHandlers = HashMap<String, ConversationHandler>()

    init {
        eventNotifier.start()
    }

    fun process(updates: List<Update>): Int {
        val message = updates.first().message()
        if (message != null) {
            logger.debug("Recieved update: ${message.from().username()}: ${message.text()}")
            try {
                val handler = conversationHandlers[message.from().username()]
                if (handler != null) {
                    handler.handle(message)
                } else if (message.text() != null) {
                    handleCommand(message)
                }
            } catch (e: Exception) {
                logger.error("Error handling update", e)
                sendError(message, e)
            }
        }
        return updates.first().updateId()
    }

    private fun handleCommand(message: Message) {
        // new command
        val input = message.text()
        val commandPattern = Regex("""/(\w+)\s*(.*)""")
        val matchResult = commandPattern.matchEntire(input)
        matchResult?.let { match ->
            val commandString = match.groupValues[1]
            val params = match.groupValues[2]
            logger.info("Command: $commandString $params")
            when (commandString) {
                "save" -> save(message, params)
                "random" -> with(conversationRepository.random()) {
                    if (this != null) {
                        messageSender.replyTo(message, formatConversation(this))
                    } else
                        messageSender.replyTo(message, "Inga resultat \uD83D\uDE31") // screaming
                }
                "find" -> with(conversationRepository.search(params)) {
                    if (this != null)
                        messageSender.replyTo(message, formatConversation(this))
                    else
                        messageSender.replyTo(message, "Inga resultat \uD83D\uDE31") // screaming
                }
                "eventcreate" -> book(message)
                "eventcancel" -> removeEvent(message)
                "eventlist" -> messageSender.replyTo(message, eventList())
                else -> {
                }
            }
        }
    }

    private fun removeEvent(message: Message) {
        setHandler(message.from().username(), EventCanceller(messageSender, message, eventRepository,
                clearHandler(message.from().username())))
    }

    private fun eventList(): String {
        val eventList = eventRepository.list().joinToString(separator = "\n")
        return if (eventList.isEmpty()) "Inga events bokade" else eventList
    }

    private fun book(message: Message) {
        setHandler(message.from().username(), EventBooker(messageSender, message, eventRepository,
                clearHandler(message.from().username())))
    }

    private fun save(message: Message, title: String?) {
        setHandler(message.from().username(), QuoteConversation(messageSender, message, title, conversationRepository,
                clearHandler(message.from().username())))
    }

    private fun setHandler(user: String, handler: ConversationHandler) {
        val currentHandler = conversationHandlers[user]
        if (currentHandler != null) {
            currentHandler.cancel()
            conversationHandlers.remove(user)
        }
        conversationHandlers[user] = handler
    }

    private fun clearHandler(user: String): () -> Unit = { conversationHandlers.remove(user) }

    private fun sendError(m: Message, e: Exception) {
        try {
            messageSender.replyTo(m, "@${m.from().username()} ${e.javaClass.name} \uD83D\uDD25\uD83D\uDD25\uD83D\uDD25")
        } catch (e: Exception) {
            logger.error("Unable to send error response", e)
        }
    }

    private fun formatConversation(c: Conversation): String =
            "*${c.prettyDate()} ${c.title ?: ""}*\n\n```\n${c.text()}\n```"

}
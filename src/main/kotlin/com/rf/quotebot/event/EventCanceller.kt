package com.rf.quotebot.event

import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup
import com.rf.quotebot.ConversationHandler
import com.rf.quotebot.MessageSender
import org.slf4j.LoggerFactory
import java.util.*


class EventCanceller(private val messageSender: MessageSender,
                     private val originalMessage: Message,
                     private val eventRepository: EventRepository,
                     private val destroyMe: () -> Unit) : ConversationHandler {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val timeout = Timer()
    private val events = eventRepository.list()

    init {
        if (events.isNotEmpty()) {
            logger.info("Starting cancel event flow $originalMessage")
            val keyboard = ReplyKeyboardMarkup(events.map { arrayOf(it.toString()) }.toTypedArray(), true, true, true)
            messageSender.replyWithMarkup(originalMessage, "Välj bokning att ta bort", keyboard)
            timeout.schedule(object : TimerTask() {
                override fun run() {
                    messageSender.send(originalMessage.chat().id(), "Avbokning avbruten")
                    destroy()
                }
            }, 60_000)
        } else {
            destroy()
        }
    }

    override fun handle(message: Message) {
        val event = events.firstOrNull { it.toString() == message.text() }
        event?.let {
            eventRepository.cancel(it.id)
        }
        messageSender.replyTo(message, "Bokning $event borttagen")
        destroy()
    }

    private fun destroy() {
        timeout.cancel()
        destroyMe.invoke()
    }

    override fun cancel() {
        timeout.cancel()
    }
}


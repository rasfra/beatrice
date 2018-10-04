package com.rf.quotebot.event

import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup
import com.rf.quotebot.ConversationHandler
import com.rf.quotebot.MessageSender
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.slf4j.LoggerFactory
import java.util.*


class EventBooker(private val messageSender: MessageSender,
                  private val originalMessage: Message,
                  private val eventRepository: EventRepository,
                  private val destroyMe: () -> Unit) : ConversationHandler {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val timeInputFormat = DateTimeFormat.forPattern("dd/MM HH:mm")
    private val timeout = Timer()

    init {
        logger.info("Starting event booker flow $originalMessage")
        messageSender.requireInput(originalMessage, "Ange tid för eventet (dd/mm hh:mm)")
        timeout.schedule(object : TimerTask() {
            override fun run() {
                messageSender.replyTo(originalMessage, "Bokning avbruten")
                destroy()
            }
        }, 60_000)
    }

    private var date: DateTime? = null
    private var title: String? = null

    private val nope = "nope"
    private val yes = "yupp"

    override fun handle(message: Message) {
        val text = message.text()
        // set date -> set title -> confirm
        when {
            date == null -> try {
                val d = DateTime.parse(text, timeInputFormat).withYear(DateTime.now().year)
                date = if (d.isBeforeNow) d.withYear(d.year + 1) else d
                messageSender
                messageSender.requireInput(message, "Ange titel för eventet")
            } catch (e: IllegalArgumentException) {
                messageSender.replyTo(message, "Felaktigt datum, använd dd/mm hh:mm")
            }
            title == null -> {
                title = text
                val keyboard = ReplyKeyboardMarkup(arrayOf(arrayOf(nope, yes)), true, true, true)
                messageSender.replyWithMarkup(message, "Boka *${timeInputFormat.print(date)} - $title*?", keyboard)
            }
            else -> {
                if (message.text() == yes) {
                    eventRepository.book(originalMessage.from().username(), date!!, title!!)
                    messageSender.removeKeyboard(originalMessage, "*${timeInputFormat.print(date)} - $title* **bokad!**")
                } else {
                    messageSender.removeKeyboard(originalMessage, "Bokning avbruten")
                }
                destroy()
            }
        }

    }

    private fun destroy() {
        timeout.cancel()
        destroyMe.invoke()
    }

    override fun cancel() {
        timeout.cancel()
    }
}


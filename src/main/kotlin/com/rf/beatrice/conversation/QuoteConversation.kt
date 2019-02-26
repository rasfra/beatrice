package com.rf.beatrice.conversation

import com.pengrad.telegrambot.model.Message
import com.rf.beatrice.MessageSender
import org.slf4j.LoggerFactory
import java.util.*

class QuoteConversation(private val messageSender: MessageSender,
                        private val message: Message,
                        private val conversationRepository: ConversationRepository,
                        private val destroyMe: () -> Unit) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val timeout = 60000L
    private val uploadedBy = message.from().username();
    private val messages = ArrayList<ConversationMessage>()
    private val timer = Timer()

    init {
        val user = message.from().username()
        logger.info("Starting session for $user, killing in $timeout ms")
        messages.add(ConversationMessage(message.forwardFrom().username(), message.text()))
        timer.schedule(object : TimerTask() {
            override fun run() {
                logger.warn("Quote conversation by $user timeout!")
                messageSender.replyTo(message, "Spara konversation avbruten, du skrev inte /save inom en minut efter att ha forwardat meddelanden")
                destroyMe.invoke()
            }
        }, timeout)
    }

    fun commit(title: String?) {
        if (messages.isNotEmpty()) {
            try {
                conversationRepository.store(messages, title, Source.TELEGRAM, uploadedBy)
                logger.info("Saved conversation by $uploadedBy")
                messageSender.replyTo(message, "Konversation '${title ?: "ingen titel"}' sparad!")
            } finally {
                timer.cancel()
                destroyMe.invoke()
            }
        }
    }

    fun add(message: Message) {
        require(message.forwardFrom() != null)
        messages.add(ConversationMessage(message.forwardFrom().username(), message.text()))
    }

    fun cancel() {
        timer.cancel()
    }
}
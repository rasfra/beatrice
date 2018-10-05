package com.rf.beatrice.conversation

import com.pengrad.telegrambot.model.Message
import com.rf.beatrice.ConversationHandler
import com.rf.beatrice.MessageSender
import org.slf4j.LoggerFactory
import java.util.*

class QuoteConversation(private val messageSender: MessageSender,
                        private val message: Message,
                        private val title: String?,
                        private val conversationRepository: ConversationRepository,
                        destroyMe: () -> Unit) : ConversationHandler {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val timeout = 3000L
    private val messages = ArrayList<ConversationMessage>()
    private val timer = Timer()

    init {
        val user = message.from().username()
        logger.info("Starting session for $user, killing in $timeout ms")
        timer.schedule(object : TimerTask() {
            override fun run() {
                if (messages.isNotEmpty()) {
                    conversationRepository.store(messages, title, Source.TELEGRAM, user)
                    logger.info("Saved conversation by $user")
                    messageSender.replyTo(message, "Konversation '${title ?: "ingen titel"}' sparad!")
                    destroyMe.invoke()
                }
            }
        }, timeout)
    }

    override fun handle(message: Message) {
        messages.add(ConversationMessage(message.forwardFrom().username(), message.text()))
    }

    override fun cancel() {
        timer.cancel()
    }
}
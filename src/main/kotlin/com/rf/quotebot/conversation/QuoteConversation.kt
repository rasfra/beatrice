package com.rf.quotebot.conversation

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.User
import com.pengrad.telegrambot.request.SendMessage
import com.rf.quotebot.ConversationHandler
import org.slf4j.LoggerFactory
import java.util.*

class QuoteConversation(private val bot: TelegramBot,
                        private val user: User,
                        private val chatId: Long,
                        private val title: String?,
                        private val conversationRepository: ConversationRepository,
                        destroyMe: () -> Unit) : ConversationHandler {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val timeout = 3000L
    private val messages = ArrayList<ConversationMessage>()
    private val timer = Timer()

    init {
        logger.info("Starting session for $user, killing in $timeout ms")
        timer.schedule(object : TimerTask() {
            override fun run() {
                if (messages.isNotEmpty()) {
                    val conversation = Conversation(Date(), messages, title, Source.TELEGRAM, user.username())
                    conversationRepository.store(conversation)
                    logger.info("Saved conversation $conversation by $user")
                    bot.execute(SendMessage(chatId, "Konversation '${title ?: "ingen titel"}' sparad!"))
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
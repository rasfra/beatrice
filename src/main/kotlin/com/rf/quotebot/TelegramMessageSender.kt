package com.rf.quotebot

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.request.ForceReply
import com.pengrad.telegrambot.model.request.Keyboard
import com.pengrad.telegrambot.model.request.ParseMode
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove
import com.pengrad.telegrambot.request.SendMessage
import org.slf4j.LoggerFactory

interface MessageSender {
    fun send(chatId: Long, text: String)
    fun replyTo(message: Message, text: String)
    fun requireInput(message: Message, text: String)
    fun replyWithMarkup(message: Message, text: String, keyboard: Keyboard)
    fun removeKeyboard(message: Message, text: String)
}

class TelegramMessageSender(private val bot: TelegramBot) : MessageSender {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private fun send(message: SendMessage) {
        logger.info("Sending ${message.parameters}")
        val response = bot.execute(message)
        assert(response.isOk)
    }

    override fun send(chatId: Long, text: String) {
        send(msg(chatId, text))
    }

    override fun replyTo(message: Message, text: String) {
        send(replyMsg(message, text))
    }

    override fun requireInput(message: Message, text: String) {
        send(replyMsg(message, text)
                .replyMarkup(ForceReply(true)))
    }

    override fun replyWithMarkup(message: Message, text: String, keyboard: Keyboard) {
        send(replyMsg(message, text)
                .replyMarkup(keyboard))
    }

    override fun removeKeyboard(message: Message, text: String) {
        send(replyMsg(message, text)
                .replyMarkup(ReplyKeyboardRemove()))
    }

    private fun msg(chatId: Long, text: String) =
            SendMessage(chatId, text)
                    .parseMode(ParseMode.Markdown)

    private fun replyMsg(message: Message, text: String) =
            msg(message.chat().id(), text)
                    .replyToMessageId(message.messageId())
}
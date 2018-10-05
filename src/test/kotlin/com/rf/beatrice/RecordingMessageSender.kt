package com.rf.beatrice

import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.request.Keyboard

class RecordingMessageSender : MessageSender {
    val messages = ArrayList<String>()

    override fun send(chatId: Long, text: String) {
        messages.add(text)
    }

    override fun sendToMainChat(text: String) {
        messages.add(text)
    }

    override fun replyTo(message: Message, text: String) {
        messages.add(text)
    }

    override fun requireInput(message: Message, text: String) {
        messages.add(text)
    }

    override fun replyWithMarkup(message: Message, text: String, keyboard: Keyboard) {
        messages.add(text)
    }

    override fun removeKeyboard(message: Message, text: String) {
        messages.add(text)
    }

    fun pop(): String {
        assert(messages.size == 1)
        val message = messages[0]
        messages.clear()
        return message
    }

    fun clear() = messages.clear()

}
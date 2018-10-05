package com.rf.beatrice

import com.pengrad.telegrambot.model.Message

interface ConversationHandler {
    fun handle(message: Message)
    fun cancel()
}
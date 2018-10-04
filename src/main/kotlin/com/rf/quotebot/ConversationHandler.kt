package com.rf.quotebot

import com.pengrad.telegrambot.model.Message

interface ConversationHandler {
    fun handle(message: Message)
    fun cancel()
}
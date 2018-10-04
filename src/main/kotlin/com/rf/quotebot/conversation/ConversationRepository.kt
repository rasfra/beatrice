package com.rf.quotebot.conversation

interface ConversationRepository {
    fun all(): Collection<Conversation>
    fun store(c: Conversation)
    fun get(id: Int): Conversation
    fun search(s: String): Conversation?
    fun random(): Conversation?
}
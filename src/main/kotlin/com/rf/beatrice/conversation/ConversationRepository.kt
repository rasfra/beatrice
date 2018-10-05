package com.rf.beatrice.conversation

interface ConversationRepository {
    fun all(): Collection<Conversation>
    fun store(messages: List<ConversationMessage>, title: String?, source: Source, uploadedBy: String?): Conversation
    fun get(id: Int): Conversation
    fun search(s: String): Conversation?
    fun random(): Conversation?
}
package com.example.wikispot.modelsForAdapters

data class Message(val author: String, val content: String)


object MessagesSupplier {

    var messages = arrayOf<Message?>()

    fun appendMessage(message: Message) {
        val array = messages.copyOf(messages.size + 1)
        array[messages.size] = message
        messages = array
    }

    fun wipeData() {
        messages = arrayOf()
    }

}

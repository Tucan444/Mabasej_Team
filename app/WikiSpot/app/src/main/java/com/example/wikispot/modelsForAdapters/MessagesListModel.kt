package com.example.wikispot.modelsForAdapters

import com.example.wikispot.GeneralVariables
import com.example.wikispot.databases.NamesDatabase
import java.util.*

data class Message(var senderId: String, val content: String, var timestamp: String="0") {

    var senderName: String

    init {
        val r = getRandomGenerator(senderId)

        senderName = "${NamesDatabase.names[r.nextInt(NamesDatabase.names.size)]} - ${r.nextInt(9999).toString()}"
    }

    private fun getRandomGenerator(seedString: String): Random {
        var n: Long = 0
        for (element in seedString) {
            n += element.toInt()
        }

        return Random(n)
    }

}


object MessagesSupplier {

    var messages = arrayOf<Message?>()

    fun appendMessage(message: Message) {
        val array = messages.copyOf(messages.size + 1)
        array[messages.size] = message
        messages = array
    }

    fun checkIfContains(message: Message): Boolean {
        for (i in messages.indices) {
            messages[i]?.let {
                if (message.senderId == it.senderId) {
                    if (message.content == it.content) {
                        if (message.timestamp == it.timestamp) {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    fun getIndexOfLastMessageFromSelf(): Int? {
        var i: Int? = null
        for (n in messages.indices) {
            if (messages[n]!!.senderId == GeneralVariables.id) {
                i = n
            }
        }
        return i
    }

    fun wipeData() {
        messages = arrayOf()
    }

}

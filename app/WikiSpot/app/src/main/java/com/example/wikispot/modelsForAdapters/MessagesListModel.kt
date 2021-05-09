package com.example.wikispot.modelsForAdapters

import com.example.wikispot.GeneralVariables
import com.example.wikispot.databases.NamesDatabase
import java.util.*

data class Message(var senderId: String, val content: String, var timestamp: String="0") {

    var senderName: String

    init {
        val r = getRandomGenerator(senderId)

        senderName = "${NamesDatabase.names[r.nextInt(NamesDatabase.names.size)]} - ${r.nextInt(9999)}"
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

        if (messages.size > GeneralVariables.max_amount_of_saved_messages) {
            deleteMessageByIndex(0)
            println(messages.size)
        }
    }

    private fun deleteMessageByIndex(i: Int) {
        messages = messages.copyOfRange(0, i) + messages.copyOfRange(i + 1, messages.size)
    }

    fun checkIfContains(message: Message, checkTimestamp: Boolean=true): Boolean {
        for (i in messages.indices) {
            messages[i]?.let {
                if (message.senderId == it.senderId) {
                    if (message.content == it.content) {
                        if (checkTimestamp) {
                            if (message.timestamp == it.timestamp) {
                                return true
                            }
                        } else {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    fun clearWaitingMessages() {
        val positionsOfItemsToRemove = mutableListOf<Int>()
        for (i in messages.indices) {
            if (messages[i]!!.timestamp == "waiting") {
                positionsOfItemsToRemove.add(i)
                println("waiting at: $i")
            }
        }

        var subtractAmount = 0
        for (index in positionsOfItemsToRemove) {
            deleteMessageByIndex(index - subtractAmount)
            subtractAmount += 1
        }
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

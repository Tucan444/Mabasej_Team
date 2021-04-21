package com.example.wikispot.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wikispot.*
import com.example.wikispot.adapters.ChatMessagesAdapter
import com.example.wikispot.databases.NamesDatabase
import com.example.wikispot.modelClasses.JsonManager
import com.example.wikispot.modelsForAdapters.Message
import com.example.wikispot.modelsForAdapters.MessagesSupplier
import kotlinx.android.synthetic.main.fragment_chat.*
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONArray
import java.io.IOException


class chatFragment : Fragment(R.layout.fragment_chat) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateRecyclerView()

        sendMessageBtn.setOnClickListener {
            GeneralVariables.id?.let {
                if (userMessageText.text.toString() != "" ) {
                    val message = Message(GeneralVariables.id!!, userMessageText.text.toString(), "waiting")
                    MessagesSupplier.appendMessage(message)
                    userMessageText.setText("")

                    val messagePostThread = Thread(MessagePost(message))
                    messagePostThread.start()

                    updateRecyclerView()
                } else {
                    requireContext().showToast("sending empty messages is not permitted")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadNamesCache()

        val dataReceiver: (String) -> Unit = { data: String ->
            val json = JsonManager(requireContext(), data, "JSONObject")

            try {
                when (json.getAttributeContent("source")) {
                    "messages/register" -> {
                        GeneralVariables.id = json.getAttributeContentByPath("data/0")

                        val r = requireContext().getRandomGenerator(GeneralVariables.id!!)

                        GeneralVariables.name = "${NamesDatabase.names[r.nextInt(NamesDatabase.names.size)]} - ${r.nextInt(9999).toString()}"

                        json.getAttributeContent("data")
                        json.getAttributeContent("1")
                        val length = json.currentJsonAttribute1!!.length()
                        json.clearSelectedAttribute()

                        for (i in 0 until length) {
                            val jsonOfMessage = JsonManager(requireContext(), json.getAttributeContentByPath("data/1/$i"), "JSONObject")
                            val message = Message(jsonOfMessage.getAttributeContent("sender"),
                                    jsonOfMessage.getAttributeContent("message"),
                                    jsonOfMessage.getAttributeContent("timestamp"))
                            if (!MessagesSupplier.checkIfContains(message)) {
                                MessagesSupplier.appendMessage(message)
                                updateRecyclerView()
                            }
                        }
                    }
                    "messages/get" -> {
                        json.getAttributeContent("data")
                        val length = json.currentJsonAttribute1!!.length()
                        json.clearSelectedAttribute()

                        for (i in 0 until length) {
                            println("message at index n: ${json.getAttributeContentByPath("data/$i")}")
                            val jsonOfMessage = JsonManager(requireContext(), json.getAttributeContentByPath("data/$i"), "JSONObject")
                            val message = Message(jsonOfMessage.getAttributeContent("sender"),
                                    jsonOfMessage.getAttributeContent("message"),
                                    jsonOfMessage.getAttributeContent("timestamp"))

                            val lastMessageSentIndex = MessagesSupplier.getIndexOfLastMessageFromSelf()
                            if (message.senderId != GeneralVariables.id) {
                                if (!MessagesSupplier.checkIfContains(message)) {
                                    MessagesSupplier.appendMessage(message)
                                    updateRecyclerView()
                                }
                            } else {
                                lastMessageSentIndex?.let {
                                    MessagesSupplier.messages[lastMessageSentIndex]!!.timestamp = jsonOfMessage.getAttributeContent("timestamp")
                                }
                            }
                        }
                    }
                }
            } catch (e: Throwable) { println("[debug][chat fragment] Exception: $e") }
        }

        ServerManagement.serverManager.addChatConnection(dataReceiver, requireContext(), "chatConnection")
    }

    override fun onPause() {
        super.onPause()
        saveNamesCache()

        ServerManagement.serverManager.deleteConnection("chatConnection")
    }

    private fun updateRecyclerView() {

        try {
            chat_messages_recycler_view.post {
                val layoutManager = LinearLayoutManager(context)
                layoutManager.orientation = LinearLayoutManager.VERTICAL
                chat_messages_recycler_view.layoutManager = layoutManager

                val adapter = context?.let { ChatMessagesAdapter(it, MessagesSupplier.messages) }
                chat_messages_recycler_view.adapter = adapter
                chat_messages_recycler_view.scrollToPosition(MessagesSupplier.messages.size - 1)
            }
        } catch (e: Throwable) { println("[debug] e5 Exception: $e") }

    }

    inner class MessagePost(private val message: Message): Runnable {
        override fun run() {
            val urlBuilder: HttpUrl.Builder = "${ServerManagement.baseUrl}messages/post".toHttpUrlOrNull()!!.newBuilder()
                    .addQueryParameter("m_sender", message.senderId)
                    .addQueryParameter("message", message.content)
            val url: String = urlBuilder.build().toString()

            val formBody: FormBody = FormBody.Builder().build()

            val request: Request = Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build()
            val client = OkHttpClient()

            client.newCall(request).enqueue(object : Callback {

                override fun onResponse(call: Call, response: Response) {
                    response.body?.let {
                        val receivedString = response.body!!.string()
                        println("[debug][message post] received string from post request: $receivedString")
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    println("Request Failed")
                    println(e)
                }

            })
        }

    }

    // loading and saving last names

    private fun loadNamesCache() {
        val namesCache = requireContext().getStringFromSharedPreferences("namesCache", "chatPreferences")
        if (namesCache != "") {
            ChatManagement.lastNames = JSONArray(namesCache)
        }

    }

    private fun saveNamesCache() {
        GeneralVariables.id?.let {
            ChatManagement.lastNames.let {
                val namesCache = JSONArray()

                var subtractAmount = it.length()
                if (it.length() > (ChatManagement.numberOfNamesToCache - 1)) {
                    subtractAmount = ChatManagement.numberOfNamesToCache - 1
                }

                for (i in it.length() - subtractAmount until it.length()) {
                    namesCache.put(it[i])
                }
                namesCache.put(GeneralVariables.id)

                requireContext().saveString("namesCache", namesCache.toString(), "chatPreferences")
            }
        }
    }

}
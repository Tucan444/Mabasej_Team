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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException


class chatFragment : Fragment(R.layout.fragment_chat) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateRecyclerView()

        writeBar.setOnClickListener {
            val scrollDownThread = Thread(ScrollDown(300))
            scrollDownThread.start()
        }
        userMessageText.setOnClickListener {
            val scrollDownThread = Thread(ScrollDown(300))
            scrollDownThread.start()
        }
        userMessageText.setOnFocusChangeListener { _, _ ->
            val scrollDownThread = Thread(ScrollDown(300))
            scrollDownThread.start()
        }

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
        loadIdFromCache()

        val dataReceiver: (String) -> Unit = { data: String ->

            try {
                val json = JsonManager(requireContext(), data, "JSONObject")

                when (json.getAttributeContent("source")) {
                    "messages/register" -> {
                        GeneralVariables.id = json.getAttributeContentByPath("data/0")

                        val r = requireContext().getRandomGenerator(GeneralVariables.id!!)

                        GeneralVariables.name = "${NamesDatabase.names[r.nextInt(NamesDatabase.names.size)]} - ${r.nextInt(9999)}"

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

                        MessagesSupplier.clearWaitingMessages()

                        for (i in 0 until length) {
                            val jsonOfMessage = JsonManager(requireContext(), json.getAttributeContentByPath("data/$i"), "JSONObject")
                            val message = Message(jsonOfMessage.getAttributeContent("sender"),
                                    jsonOfMessage.getAttributeContent("message"),
                                    jsonOfMessage.getAttributeContent("timestamp"))

                            if (!MessagesSupplier.checkIfContains(message)) {
                                MessagesSupplier.appendMessage(message)
                                updateRecyclerView()
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
        saveIdToCache()

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

    inner class ScrollDown(private val after: Long): Runnable {
        override fun run() {
            Thread.sleep(after)
            try {
                chat_messages_recycler_view.post {
                    chat_messages_recycler_view.scrollToPosition(MessagesSupplier.messages.size - 1)
                }
            } catch (e: Throwable) { println("[debug] e6 Exception: $e") }
        }
    }

    inner class MessagePost(private val message: Message): Runnable {
        override fun run() {

            val url = "${ServerManagement.baseUrl}messages/post"

            val json: MediaType? = "application/json; charset=utf-8".toMediaTypeOrNull()

            val body:RequestBody = RequestBody.create(json, JSONObject()
                    .put("m_sender", message.senderId)
                    .put("message", message.content).toString())

            val request: Request = Request.Builder()
                    .url(url)
                    .post(body)
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

    private fun loadIdFromCache() {
        val id = requireContext().getStringFromSharedPreferences("id")
        if (id != "") {
            GeneralVariables.id = id
        }

    }

    private fun saveIdToCache() {
        GeneralVariables.id?.let {
            requireContext().saveString("id", GeneralVariables.id!!)
        }
    }

}

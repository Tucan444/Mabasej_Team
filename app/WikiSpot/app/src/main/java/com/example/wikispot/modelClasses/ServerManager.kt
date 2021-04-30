package com.example.wikispot.modelClasses

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.TextView
import com.example.wikispot.GeneralVariables
import com.example.wikispot.ScreenParameters
import com.example.wikispot.ServerManagement
import com.example.wikispot.modelsForAdapters.MessagesSupplier
import com.github.barteksc.pdfviewer.PDFView
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException


class ServerManager {

    private var receiverConnections = mutableListOf<ReceiverConnection>()
    private var viewConnections = mutableListOf<ViewConnection>()
    private var chatConnections = mutableListOf<ChatConnection>()

    // single time requests

    fun getData(dataReceiver: (String) -> Unit, context: Context, serverId: Int, path: String, attributePath: String = "", numberOfAttempts: Int = 2) {
        val dataRequestThread = Thread(DataRequest(dataReceiver, context, serverId, path, attributePath, numberOfAttempts))
        dataRequestThread.start()
    }

    inner class DataRequest(val dataReceiver: (String) -> Unit, val context: Context, val serverId: Int, val path: String = "", val attributePath: String, private val numberOfAttempts: Int = 2): Runnable{
        override fun run() {
            for (n in 0 until numberOfAttempts) {
                var url = "${ServerManagement.baseUrl}devices_list"

                if (path != "") {
                    if (path.contains(ServerManagement.sensors_keyword)){
                        url = "${ServerManagement.baseUrl}$serverId/sensors"
                    } else {
                        url = "${ServerManagement.baseUrl}files/$serverId/$path"
                    }
                }

                val request = Request.Builder().url(url).build()
                val client = OkHttpClient()

                client.newCall(request).enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        response.body?.let {
                            val receivedString = response.body!!.string()
                            if (receivedString == "Internal Server Error") {
                                return
                            }

                            try {
                                JSONArray(receivedString)

                                val jsonManager = JsonManager(context, receivedString)
                                if (path == "") {
                                    if (attributePath == "GET_WHOLE_ARRAY") {
                                        dataReceiver(jsonManager.jsonArray.toString())
                                        return
                                    }
                                    jsonManager.getJsonObject(0)
                                } else {
                                    if (attributePath == "") {
                                        throw Throwable()
                                    }
                                }

                                if (attributePath != "") {
                                    dataReceiver(jsonManager.getAttributeContentByPath(attributePath))
                                } else {
                                    dataReceiver(jsonManager.currentJsonObject.toString())
                                }

                            } catch (exception: Throwable) {
                                try {
                                    JSONObject(receivedString)

                                    val jsonManager = JsonManager(context, receivedString, "JSONObject")

                                    if (attributePath != "") {
                                        dataReceiver(jsonManager.getAttributeContentByPath(attributePath))
                                    } else {
                                        dataReceiver(jsonManager.currentJsonObject.toString())
                                    }

                                } catch (exception: Throwable) {
                                    dataReceiver(receivedString)
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call, e: IOException) {
                        println("Request Failed")
                        println(e)
                    }
                })

                ServerManagement.totalNumberOfRequestsSent += 1

                Thread.sleep(ServerManagement.dataRequestOnAttemptWait)
            }
        }
    }

    fun getImage(imageReceiver: (Bitmap) -> Unit, serverId: Int, path: String, numberOfAttempts: Int = 2) {
        val imageRequestThread = Thread(ImageRequest(imageReceiver, serverId, path, numberOfAttempts))
        imageRequestThread.start()
    }

    inner class ImageRequest(val imageReceiver: (Bitmap) -> Unit, val serverId: Int, val path: String, private val numberOfAttempts: Int): Runnable {
        override fun run() {
            for (i in 0 until numberOfAttempts) {
                val url = "${ServerManagement.baseUrl}files/$serverId/$path"

                try {
                    val inputStream = java.net.URL(url).openStream()
                    var bitmap = BitmapFactory.decodeStream(inputStream)

                    if (bitmap.width > ScreenParameters.width) {
                        val ratio = bitmap.height.toFloat() / bitmap.width.toFloat()
                        bitmap = Bitmap.createScaledBitmap(bitmap, ScreenParameters.width, (ScreenParameters.width * ratio).toInt(), false)
                    }

                    imageReceiver(bitmap)
                    break
                } catch (e: Throwable) { println(e) }

                ServerManagement.totalNumberOfRequestsSent += 1

                Thread.sleep(ServerManagement.imageRequestOnAttemptWait)
            }
        }
    }

    fun loadPdfView(view: PDFView, url: String, swipeHorizontal: Boolean = false) {
        val pdfLoadingRequestThread = Thread(PdfLoadingRequest(view, url, swipeHorizontal))
        pdfLoadingRequestThread.start()
    }

    inner class PdfLoadingRequest(val view: PDFView, val url: String, private val swipeHorizontal: Boolean = false): Runnable {
        override fun run() {
            val inputStream = java.net.URL(url).openStream()
            view.post {
                view.fromStream(inputStream).swipeHorizontal(swipeHorizontal).load()
                view.zoomTo(view.width / 490.0F)
            }
        }

    }

    // connections

    fun clearConnections() {
        for (i in 0 until receiverConnections.size) {
            try {
                receiverConnections[i].running = false
            } catch (e: Throwable) { println("In clearConnections: $e") }
        }
        receiverConnections = mutableListOf()

        for (i in 0 until viewConnections.size) {
            try {
                viewConnections[i].running = false
            } catch (e: Throwable) { println("In clearConnections: $e") }
        }
        viewConnections = mutableListOf()

        for (i in 0 until chatConnections.size) {
            try {
                chatConnections[i].running = false
            } catch (e: Throwable) { println("In clearConnections: $e") }
        }
        chatConnections = mutableListOf()
    }

    fun checkIfConnectionAlreadyExists(connectionName: String, connectionType: String = "any"): Boolean{
        if ((connectionType == "any") or (connectionType == "receiver")) {
            for (n in 0 until receiverConnections.size) {
                if (receiverConnections[n].connectionName == connectionName) {
                    return true
                }
            }
        }
        if ((connectionType == "any") or (connectionType == "view")) {
            for (n in 0 until viewConnections.size) {
                if (viewConnections[n].connectionName == connectionName) {
                    return true
                }
            }
        }
        if ((connectionType == "any") or (connectionType == "chat")) {
            for (n in 0 until chatConnections.size) {
                if (chatConnections[n].connectionName == connectionName) {
                    return true
                }
            }
        }

        return false
    }

    fun deleteConnection(connectionName: String, connectionType: String = "any") {  // other types are any, activity and view
        if ((connectionType == "any") or (connectionType == "receiver")) {
            val indexesToRemove = mutableListOf<Int>()
            for (i in 0 until receiverConnections.size) {  // checking in connections
                try {
                    if (receiverConnections[i].connectionName == connectionName) {
                        receiverConnections[i].running = false
                        indexesToRemove.add(i)
                    }
                } catch (e: Throwable) { println("In deleteConnection: $e") }
            }

            for (i in 0 until indexesToRemove.size) {
                receiverConnections.removeAt(indexesToRemove[i] - i)
            }
        }

        if ((connectionType == "any") or (connectionType == "view")) {
            val indexesToRemove = mutableListOf<Int>()
            for (i in 0 until viewConnections.size) {  // checking in connections
                try {
                    if (viewConnections[i].connectionName == connectionName) {
                        viewConnections[i].running = false
                        indexesToRemove.add(i)
                    }
                } catch (e: Throwable) { println("In deleteConnection: $e") }
            }

            for (i in 0 until indexesToRemove.size) {
                viewConnections.removeAt(indexesToRemove[i] - i)
            }
        }

        if ((connectionType == "any") or (connectionType == "chat")) {
            val indexesToRemove = mutableListOf<Int>()
            for (i in 0 until chatConnections.size) {  // checking in connections
                try {
                    if (chatConnections[i].connectionName == connectionName) {
                        chatConnections[i].running = false
                        indexesToRemove.add(i)
                    }
                } catch (e: Throwable) { println("In deleteConnection: $e") }
            }

            for (i in 0 until indexesToRemove.size) {
                chatConnections.removeAt(indexesToRemove[i] - i)
            }
        }
    }

    fun addReceiverConnection(dataReceiver: (String) -> Unit, context: Context, connectionName: String, serverId: Int, path: String = "", attributePath: String = "", waitTime: Long = ServerManagement.receiverConnectionOnCheckWait) {
        receiverConnections.add(ReceiverConnection(dataReceiver, context, connectionName, serverId, path, attributePath, waitTime))
    }

    inner class ReceiverConnection(val dataReceiver: (String) -> Unit, val context: Context, val connectionName: String, val serverId: Int, val path: String = "", val attributePath: String, val waitTime: Long) {

        var running = true

        init {
            val checkingServerDataThread = Thread(CheckingServerData())
            checkingServerDataThread.start()
        }

        inner class CheckingServerData : Runnable {
            override fun run() {
                while (running) {
                    var url = "${ServerManagement.baseUrl}devices_list"

                    if (path != "") {
                        if (path.contains(ServerManagement.sensors_keyword)){
                            url = "${ServerManagement.baseUrl}$serverId/sensors"
                        } else {
                            url = "${ServerManagement.baseUrl}files/$serverId/$path"
                        }
                    }

                    val request = Request.Builder().url(url).build()
                    val client = OkHttpClient()

                    client.newCall(request).enqueue(object : Callback {
                        override fun onResponse(call: Call, response: Response) {
                            response.body?.let {
                                val receivedString = response.body!!.string()
                                if (receivedString == "Internal Server Error") {
                                    return
                                }

                                try {
                                    JSONArray(receivedString)

                                    val jsonManager = JsonManager(context, receivedString)
                                    if (path == "") {
                                        if (attributePath == "GET_WHOLE_ARRAY") {
                                            dataReceiver(jsonManager.jsonArray.toString())
                                            return
                                        }
                                        jsonManager.getJsonObject(0)
                                    } else {
                                        if (attributePath == "") {
                                            throw Throwable()
                                        }
                                    }

                                    if (attributePath != "") {
                                        dataReceiver(jsonManager.getAttributeContentByPath(attributePath))
                                    } else {
                                        dataReceiver(jsonManager.currentJsonObject.toString())
                                    }

                                } catch (exception: Throwable) {
                                    try {
                                        JSONObject(receivedString)

                                        val jsonManager = JsonManager(context, receivedString, "JSONObject")

                                        if (attributePath != "") {
                                            dataReceiver(jsonManager.getAttributeContentByPath(attributePath))
                                        } else {
                                            dataReceiver(jsonManager.currentJsonObject.toString())
                                        }

                                    } catch (exception: Throwable) {
                                        dataReceiver(receivedString)
                                    }
                                }
                            }
                        }

                        override fun onFailure(call: Call, e: IOException) {
                            println("Request Failed")
                            println(e)
                        }
                    })

                    ServerManagement.totalNumberOfRequestsSent += 1

                    Thread.sleep(waitTime)
                }
            }
        }

    }

    fun addViewConnection(context: Context, view: TextView, connectionName: String, serverId: Int, path: String = "", attributePath: String = "") {
        viewConnections.add(ViewConnection(context, view, connectionName, serverId, path, attributePath))
    }

    inner class ViewConnection(val context: Context, val view: TextView, val connectionName: String, val serverId: Int, val path: String = "", var attributePath: String) {

        var running = true

        init {
            val checkingServerDataThread = Thread(CheckingServerData())
            checkingServerDataThread.start()
        }

        inner class CheckingServerData: Runnable {
            override fun run() {
                while (running) {
                    var url = "${ServerManagement.baseUrl}devices_list"

                    if (path != "") {
                        if (path.contains(ServerManagement.sensors_keyword)){
                            url = "${ServerManagement.baseUrl}$serverId/sensors"
                        } else {
                            url = "${ServerManagement.baseUrl}files/$serverId/$path"
                        }
                    }

                    val request = Request.Builder().url(url).build()
                    val client = OkHttpClient()

                    client.newCall(request).enqueue(object : Callback {
                        override fun onResponse(call: Call, response: Response) {
                            response.body?.let {
                                val receivedString = response.body!!.string()

                                if (receivedString == "Internal Server Error") {
                                    return
                                }

                                try {
                                    JSONArray(receivedString)

                                    val jsonManager = JsonManager(context, receivedString)
                                    if (path == "") {
                                        if (attributePath == "GET_WHOLE_ARRAY") {
                                            view.text = jsonManager.jsonArray.toString()
                                            return
                                        }
                                        jsonManager.getJsonObject(0)
                                    } else {
                                        if (attributePath == "") {
                                            throw Throwable()
                                        }
                                    }

                                    if (attributePath != "") {
                                        view.post {
                                            view.text = jsonManager.getAttributeContentByPath(attributePath)
                                        }
                                    } else {
                                        view.post {
                                            view.text = jsonManager.currentJsonObject.toString()
                                        }
                                    }

                                } catch (exception: Throwable) {
                                    try {
                                        JSONObject(receivedString)

                                        val jsonManager = JsonManager(context, receivedString, "JSONObject")

                                        if (attributePath != "") {
                                            view.post {
                                                view.text = jsonManager.getAttributeContentByPath(attributePath)
                                            }
                                        } else {
                                            view.post {
                                                view.text = jsonManager.currentJsonObject.toString()
                                            }
                                        }

                                    } catch (exception: Throwable) {
                                        view.post {
                                            view.text = receivedString
                                        }
                                    }
                                }
                            }
                        }

                        override fun onFailure(call: Call, e: IOException) {
                            println("Request Failed")
                            println(e)
                        }
                    })

                    ServerManagement.totalNumberOfRequestsSent += 1

                    Thread.sleep(ServerManagement.viewConnectionOnCheckWait)
                }
            }
        }

    }

    fun addChatConnection(dataReceiver: (String) -> Unit, context: Context, connectionName: String) {
        chatConnections.add(ChatConnection(dataReceiver, context, connectionName))
    }

    inner class ChatConnection(val dataReceiver: (String) -> Unit, val context: Context, val connectionName: String) {

        var running = true

        init {
            val checkingServerDataThread = Thread(CheckingServerData())
            checkingServerDataThread.start()
        }

        inner class CheckingServerData : Runnable {
            override fun run() {
                while (running) {

                    if (GeneralVariables.id == null) {

                        val url = "${ServerManagement.baseUrl}messages/register"
                        println(url)

                        val request = Request.Builder().url(url).build()
                        val client = OkHttpClient()

                        client.newCall(request).enqueue(object : Callback {
                            override fun onResponse(call: Call, response: Response) {
                                response.body?.let {
                                    val receivedString = response.body!!.string()
                                    if (receivedString == "Internal Server Error") {
                                        return
                                    }

                                    val returnJsonObject = JSONObject()
                                    returnJsonObject.put("source", "messages/register")
                                    returnJsonObject.put("data", JSONArray(receivedString))
                                    dataReceiver(returnJsonObject.toString())
                                }
                            }

                            override fun onFailure(call: Call, e: IOException) {
                                println("Request Failed")
                                println(e)
                            }
                        })
                    } else {
                        var timestamp = "0"
                        if (MessagesSupplier.messages.isNotEmpty()) {
                            val messagesReversed = MessagesSupplier.messages.reversed()
                            for (i in messagesReversed.indices) {
                                if (messagesReversed[i]!!.timestamp != "waiting") {
                                    timestamp = messagesReversed[i]!!.timestamp
                                    break
                                }
                            }
                        }

                        val urlBuilder: HttpUrl.Builder = "${ServerManagement.baseUrl}messages/get".toHttpUrlOrNull()!!.newBuilder()
                        urlBuilder.addQueryParameter("timestamp", timestamp)
                        val url: String = urlBuilder.build().toString()

                        val request: Request = Request.Builder()
                                .url(url)
                                .build()
                        val client = OkHttpClient()

                        client.newCall(request).enqueue(object : Callback {

                            override fun onResponse(call: Call, response: Response) {
                                response.body?.let {
                                    val receivedString = response.body!!.string()
                                    if (receivedString == "Internal Server Error") {
                                        return
                                    }
                                    val returnJsonObject = JSONObject()
                                    returnJsonObject.put("source", "messages/get")
                                    returnJsonObject.put("data", JSONArray(receivedString))
                                    dataReceiver(returnJsonObject.toString())
                                }
                            }

                            override fun onFailure(call: Call, e: IOException) {
                                println("Request Failed")
                                println(e)
                            }

                        })
                    }

                    Thread.sleep(ServerManagement.chatConnectionOnCheckWait)
                }
            }

        }

    }

}

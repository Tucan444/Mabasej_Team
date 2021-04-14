package com.example.wikispot.modelClasses

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.TextView
import com.example.wikispot.ServerManagement
import com.github.barteksc.pdfviewer.PDFView
import kotlinx.android.synthetic.main.file_view.view.*
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream

class ServerManager {

    private var receiverConnections = mutableListOf<ReceiverConnection>()
    private var viewConnections = mutableListOf<ViewConnection>()

    // single time requests

    fun getData(dataReceiver: (String) -> Unit, context: Context, serverId: Int, path: String, attributePath: String="", numberOfAttempts: Int=2) {
        val dataRequestThread = Thread(DataRequest(dataReceiver, context, serverId, path, attributePath, numberOfAttempts))
        dataRequestThread.start()
    }

    inner class DataRequest(val dataReceiver: (String) -> Unit, val context: Context, val serverId: Int, val path: String="", val attributePath: String, private val numberOfAttempts: Int=2): Runnable{
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

                                if(attributePath != "") {
                                    dataReceiver(jsonManager.getAttributeContentByPath(attributePath))
                                } else {
                                    dataReceiver(jsonManager.currentJsonObject.toString())
                                }

                            } catch (exception: Throwable) {
                                try {
                                    JSONObject(receivedString)

                                    val jsonManager = JsonManager(context, receivedString, "JSONObject")

                                    if(attributePath != "") {
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
                    val bitmap = BitmapFactory.decodeStream(inputStream)

                    imageReceiver(bitmap)
                } catch (e: Throwable) { println(e) }

                ServerManagement.totalNumberOfRequestsSent += 1

                Thread.sleep(ServerManagement.imageRequestOnAttemptWait)
            }
        }
    }

    fun loadPdfView(view: PDFView, url: String) {
        val pdfLoadingRequestThread = Thread(PdfLoadingRequest(view, url))
        pdfLoadingRequestThread.start()
    }

    inner class PdfLoadingRequest(val view: PDFView, val url: String): Runnable {
        override fun run() {
            val inputStream = java.net.URL(url).openStream()
            view.post {
                view.fromStream(inputStream).load()
                view.zoomTo(view.width / 490.0F)
            }
        }

    }

    // connections

    fun clearConnections() {
        for (i in 0 until receiverConnections.size) {
            try {
                receiverConnections[i].running = false
                receiverConnections.removeAt(i)
            } catch (e: Throwable) { println("In clearConnections: $e") }
        }
        for (i in 0 until viewConnections.size) {
            try {
                viewConnections[i].running = false
                viewConnections.removeAt(i)
            } catch (e: Throwable) { println("In clearConnections: $e") }
        }
    }

    fun checkIfConnectionAlreadyExists(connectionName: String, connectionType: String="any"): Boolean{
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

        return false
    }

    fun deleteConnection(connectionName: String, connectionType: String="any") {  // other types are any, activity and view
        if ((connectionType == "any") or (connectionType == "receiver")) {
            for (i in 0 until receiverConnections.size) {  // checking in connections
                try {
                    if (receiverConnections[i].connectionName == connectionName) {
                        receiverConnections[i].running = false
                        receiverConnections.removeAt(i)
                    }
                } catch (e: Throwable) { println("In deleteConnection: $e") }
            }
        }

        if ((connectionType == "any") or (connectionType == "view")) {
            for (i in 0 until viewConnections.size) {  // checking in connections
                try {
                    if (viewConnections[i].connectionName == connectionName) {
                        viewConnections[i].running = false
                        viewConnections.removeAt(i)
                    }
                } catch (e: Throwable) { println("In deleteConnection: $e") }
            }
        }
    }

    fun addReceiverConnection(dataReceiver: (String) -> Unit, context: Context, connectionName: String, serverId: Int, path: String="", attributePath: String="", waitTime: Long=ServerManagement.receiverConnectionOnCheckWait) {
        receiverConnections.add(ReceiverConnection(dataReceiver, context, connectionName, serverId, path, attributePath, waitTime))
    }

    inner class ReceiverConnection(val dataReceiver: (String) -> Unit, val context: Context, val connectionName: String, val serverId: Int, val path: String="", val attributePath: String, val waitTime: Long) {

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

                                    if(attributePath != "") {
                                        dataReceiver(jsonManager.getAttributeContentByPath(attributePath))
                                    } else {
                                        dataReceiver(jsonManager.currentJsonObject.toString())
                                    }

                                } catch (exception: Throwable) {
                                    try {
                                        JSONObject(receivedString)

                                        val jsonManager = JsonManager(context, receivedString, "JSONObject")

                                        if(attributePath != "") {
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

    fun addViewConnection(context: Context, view: TextView, connectionName: String, serverId: Int, path: String="", attributePath: String="") {
        viewConnections.add(ViewConnection(context, view, connectionName, serverId, path, attributePath))
    }

    inner class ViewConnection(val context: Context, val view: TextView, val connectionName: String, val serverId: Int, val path: String="", var attributePath: String) {

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

                                    if(attributePath != "") {
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

                                        if(attributePath != "") {
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

}

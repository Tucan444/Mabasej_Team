package com.example.wikispot.modelClasses

import android.content.Context
import android.widget.TextView
import com.example.wikispot.ServerManagement
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class ServerManager {

    private var receiverConnections = mutableListOf<ReceiverConnection>()
    private var viewConnections = mutableListOf<ViewConnection>()

    fun getData(dataReceiver: (String) -> Unit, context: Context, serverId: Int, path: String, attributePath: String="", numberOfAttempts: Int=2) {
        val dataRequestThread = Thread(DataRequest(dataReceiver, context, serverId, path, attributePath, numberOfAttempts))
        dataRequestThread.start()
    }

    inner class DataRequest(val dataReceiver: (String) -> Unit, val context: Context, val serverId: Int, val path: String="", val attributePath: String, private val numberOfAttempts: Int=2): Runnable{
        override fun run() {
            for (n in 0 until numberOfAttempts) {
                var url = "${ServerManagement.baseUrl}devices_list"

                if (path != "") {
                    url = "${ServerManagement.baseUrl}files/$serverId/$path"
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
                                    jsonManager.getJsonObject(serverId)
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

                Thread.sleep(ServerManagement.dataRequestOnAttemptWait)
            }
        }
    }

    // connections

    fun clearConnections() {
        for (i in 0 until receiverConnections.size) {
            receiverConnections[i].running = false
            receiverConnections.removeAt(i)
        }
        for (i in 0 until viewConnections.size) {
            viewConnections[i].running = false
            viewConnections.removeAt(i)
        }
    }

    fun deleteConnection(connectionName: String, connectionType: String="any") {  // other types are any, activity and view
        if ((connectionType == "any") or (connectionType == "activity")) {
            for (i in 0 until receiverConnections.size) {  // checking in connections
                if (receiverConnections[i].connectionName == connectionName) {
                    receiverConnections[i].running = false
                    receiverConnections.removeAt(i)
                }
            }
        }

        if ((connectionType == "any") or (connectionType == "view")) {
            for (i in 0 until viewConnections.size) {  // checking in connections
                if (viewConnections[i].connectionName == connectionName) {
                    viewConnections[i].running = false
                    viewConnections.removeAt(i)
                }
            }
        }
    }

    fun addReceiverConnection(dataReceiver: (String) -> Unit, context: Context, connectionName: String, serverId: Int, path: String?=null, attributePath: String="") {
        receiverConnections.add(ReceiverConnection(dataReceiver, context, connectionName, serverId, path, attributePath))
    }

    inner class ReceiverConnection(val dataReceiver: (String) -> Unit, val context: Context, val connectionName: String, val serverId: Int, val path: String?=null, val attributePath: String) {

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
                        url = "${ServerManagement.baseUrl}files/$serverId/$path"
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
                                        jsonManager.getJsonObject(serverId)
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

                    Thread.sleep(ServerManagement.dataRequestOnAttemptWait)
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
                        url = "${ServerManagement.baseUrl}files/$serverId/$path"
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
                                        jsonManager.getJsonObject(serverId)
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
                    Thread.sleep(ServerManagement.viewConnectionOnCheckWait)
                }
            }
        }

    }

}

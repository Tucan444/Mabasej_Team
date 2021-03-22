package com.example.wikispot.modelClasses

import android.app.Activity
import android.content.Context
import android.widget.TextView
import com.example.wikispot.ServerManagement
import com.example.wikispot.receiveData
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

class ServerManager {

    private var activityConnections = mutableListOf<ActivityConnection>()
    private var viewConnections = mutableListOf<ViewConnection>()

    fun getData(activity: Activity, context: Context, serverId: Int, path: String, attributePath: String, getWholeContent: Boolean=false, numberOfAttempts: Int=2) {
        val dataRequestThread = Thread(DataRequest(activity, context, serverId, path, attributePath, getWholeContent, numberOfAttempts))
        dataRequestThread.start()
    }

    inner class DataRequest(val activity: Activity, val context: Context, val serverId: Int, val path: String="", val attributePath: String, var getWholeContent: Boolean=false, val numberOfAttempts: Int=2): Runnable{
        override fun run() {
            for (n in 0 until numberOfAttempts) {
                var url = "http://192.168.1.230:8000/devices_list"

                if (path != "") {
                    url = "http://192.168.1.230:8000/files/$serverId/$path"
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
                                        getWholeContent = true
                                    }
                                }

                                if (getWholeContent) {
                                    activity.receiveData(jsonManager.currentJsonObject.toString())
                                    println("[debug] testing ${activity.localClassName} ; ${activity.componentName} ; ${activity.packageName}")
                                } else if(attributePath != "") {
                                    activity.receiveData(jsonManager.getAttributeContentByPath(attributePath))
                                } else {
                                    println("[debug] path or whole content needs to be chosen")
                                }

                            } catch (exception: Throwable) {
                                activity.receiveData(receivedString)
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

    fun deleteConnection(connectionName: String, connectionType: String="any") {  // other types are any, activity and view
        if ((connectionType == "any") or (connectionType == "activity")) {
            for (i in 0 until activityConnections.size) {  // checking in connections
                if (activityConnections[i].connectionName == connectionName) {
                    activityConnections[i].running = false
                    activityConnections.removeAt(i)
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

    fun addActivityConnection(activity: Activity, connectionName: String, serverId: Int, path: String?=null) {
        activityConnections.add(ActivityConnection(activity, connectionName, serverId, path))
    }

    inner class ActivityConnection(val activity: Activity, val connectionName: String, val serverId: Int, val path: String?=null) {

        var running = true

        init {
            val checkingServerDataThread = Thread(CheckingServerData())
            checkingServerDataThread.start()
        }

        inner class CheckingServerData : Runnable {
            override fun run() {
                while (running) {

                    println("[debug] connection thread running")

                    Thread.sleep(ServerManagement.activityConnectionOnCheckWait)
                }
            }
        }

    }

    fun addViewConnection(context: Context, view: TextView, connectionName: String, serverId: Int, path: String="", attributePath: String, getWholeContent: Boolean=false) {
        viewConnections.add(ViewConnection(context, view, connectionName, serverId, path, attributePath, getWholeContent))
    }

    inner class ViewConnection(val context: Context, val view: TextView, val connectionName: String, val serverId: Int, val path: String="", var attributePath: String, var getWholeContent: Boolean=false) {

        var running = true

        init {
            val checkingServerDataThread = Thread(CheckingServerData())
            checkingServerDataThread.start()
        }

        inner class CheckingServerData: Runnable {
            override fun run() {
                while (running) {
                    var url = "http://192.168.1.230:8000/devices_list"

                    if (path != "") {
                        url = "http://192.168.1.230:8000/files/$serverId/$path"
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

                                    if (getWholeContent) {
                                        view.post {
                                            view.text = jsonManager.currentJsonObject.toString()
                                        }
                                    } else if(attributePath != "") {
                                        view.post {
                                            view.text = jsonManager.getAttributeContentByPath(attributePath)
                                        }
                                    } else {
                                        println("[debug] path or whole content needs to be chosen")
                                    }

                                } catch (exception: Throwable) {
                                    view.post {
                                        view.text = receivedString
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

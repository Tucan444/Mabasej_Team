package com.example.wikispot

import android.content.Context
import android.view.View
import android.widget.Toast
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject

fun Context.showToast(message: String, length: Int=Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, length).show()
}

fun Context.showSnack(message: String, view: View, length: Int = Snackbar.LENGTH_LONG) {
    Snackbar.make(this, view, message, length).show()
}

fun Context.getDataFromServer(): MutableList<JSONObject> {
    // initing
    if (!Python.isStarted()) {
        Python.start(AndroidPlatform(this))
    }

    // getting file
    val python = Python.getInstance()
    val pythonFile = python.getModule("server_manager")

    // getting the data
    pythonFile.callAttr("init")
    val size = pythonFile.callAttr("get_length").toInt()
    val jsonList = mutableListOf<JSONObject>()

    for (n in 0 until size) {
        jsonList.add(n, JSONObject(pythonFile.callAttr("get_json", n).toString()))
    }

    return jsonList
}
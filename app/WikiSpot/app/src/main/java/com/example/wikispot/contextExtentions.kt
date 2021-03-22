package com.example.wikispot

import android.content.Context
import android.os.Environment
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import okhttp3.*
import org.json.JSONArray
import java.io.*
import java.nio.channels.FileChannel
import java.nio.charset.Charset

// for showing messages

fun Context.showToast(message: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, length).show()
}

fun Context.showSnack(message: String, view: View, length: Int = Snackbar.LENGTH_LONG) {
    Snackbar.make(this, view, message, length).show()
}

// for theme

fun Context.getThemeId(): Int {
    if (ThemeOptions.darkTheme) {
        if (ThemeOptions.actionBar) {
            return R.style.Theme_WikiSpotWithActionBarDark
        } else {
            return R.style.Theme_WikiSpotDark
        }
    } else {
        if (ThemeOptions.actionBar) {
            return R.style.Theme_WikiSpotWithActionBar
        } else {
            return R.style.Theme_WikiSpot
        }
    }
}

// for client
fun Context.getDataFromServer(): String {
    // requesting data
    val url = "http://192.168.1.230:8000/devices_list"
    val request = Request.Builder().url(url).build()
    val client = OkHttpClient()
    var receivedResponse = ""

    client.newCall(request).enqueue(object : Callback {
        override fun onResponse(call: Call, response: Response) {
            response.body?.let {
                receivedResponse = response.body!!.string()
            }
        }

        override fun onFailure(call: Call, e: IOException) {
            println("Request Failed")
            println(e)
        }
    })

    Thread.sleep(400)

    println("[debug] received string: $receivedResponse")
    try {
        JSONArray(receivedResponse)
        return receivedResponse
    } catch (exception: Throwable) {
        return "[]"
    }
}

// working with files
fun Context.createFile(filename: String, filetype: String): File {

    val storageDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
    if (!storageDir?.exists()!!) {
        storageDir.mkdir()
    }

    return File(storageDir, "$filename.$filetype")
}

fun Context.getFile(filename: String, filetype: String): String {

    val file = (getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.absolutePath ?: "") + "/$filename.$filetype"

    val stream = FileInputStream(file)

    var returnString = ""

    stream.use { streamInUse ->
        val fileChannel = streamInUse.channel
        val mappedByteBuffer = fileChannel.map(
                FileChannel.MapMode.READ_ONLY,
                0,
                fileChannel.size()
        )
        returnString = Charset.defaultCharset().decode(mappedByteBuffer).toString()
    }

    return returnString
}

fun Context.saveString(accessKey: String, stringValue: String, preferencesFilename: String="generalPreferences") {
    val sharedPreferences = getSharedPreferences(preferencesFilename, Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()

    editor.apply{
        putString(accessKey, stringValue)
    }.apply()
}

fun Context.getStringFromSharedPreferences(accessKey: String, preferencesFilename: String="generalPreferences"): String {
    val sharedPreferences = getSharedPreferences(preferencesFilename, Context.MODE_PRIVATE)
    val returnedString = sharedPreferences.getString(accessKey, "")

    returnedString?.let {
        return returnedString
    }

    return  ""
}

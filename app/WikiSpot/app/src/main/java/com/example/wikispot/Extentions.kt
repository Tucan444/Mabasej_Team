package com.example.wikispot

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Environment
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileInputStream
import java.nio.channels.FileChannel
import java.nio.charset.Charset
import java.util.*

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
        if (!ThemeOptions.moreColors) {
            return R.style.Theme_WikiSpotDark
        } else {
            return R.style.Theme_WikiSpotDark_
        }
    } else {
        if (!ThemeOptions.moreColors) {
            return R.style.Theme_WikiSpot
        } else {
            return R.style.Theme_WikiSpot_
        }
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

// Other

fun Context.getRandomGenerator(seedString: String): Random {
    var n: Long = 0
    for (element in seedString) {
        n += element.toInt()
    }

    println(n)
    return Random(n)
}

// Activity extensions

fun Activity.askToQuit() {
    val builder = AlertDialog.Builder(this)
    builder.setTitle("Confirm")
    builder.setMessage("Do you want to quit the application?")
    builder.setPositiveButton("Yes") { _, _ -> finish()}
    builder.setNegativeButton("No") { _, _ -> }
    builder.show()
}

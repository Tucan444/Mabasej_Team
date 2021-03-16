package com.example.wikispot

import android.content.Context
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

fun Context.showToast(message: String, length: Int=Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, length).show()
}

fun Context.showSnack(message: String, view: View, length: Int=Snackbar.LENGTH_LONG) {
    Snackbar.make(this, view, message, length).show()
}
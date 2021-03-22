package com.example.wikispot

import android.app.Activity
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.activity_main.*

// for serverManager
fun Activity.receiveData(data: String) {

    when (this.localClassName) {
        "activities.MainActivity" -> { this.receiveDataForMainActivity(data) }
    }
}

fun Activity.receiveDataForMainActivity(data: String) {

}
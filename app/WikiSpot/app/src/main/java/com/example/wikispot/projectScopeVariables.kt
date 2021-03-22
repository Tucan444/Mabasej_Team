package com.example.wikispot

import com.example.wikispot.modelClasses.ServerManager


object ManifestRelatedVariables {

    val REQUEST_READ_EXTERNAL = 1

}

object IntentsKeys {

    const val startFragment = "start_fragment"

}


object ServerManagement {
    val serverManager = ServerManager()
    const val activityConnectionOnCheckWait: Long = 4000
    const val viewConnectionOnCheckWait: Long = 5000
    const val dataRequestOnAttemptWait: Long = 2000
}


object ThemeOptions {

    var darkTheme = false
    var actionBar = true

}

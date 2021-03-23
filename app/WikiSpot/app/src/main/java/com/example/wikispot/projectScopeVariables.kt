package com.example.wikispot

import com.example.wikispot.modelClasses.ServerManager


object ManifestRelatedVariables {

    val REQUEST_READ_EXTERNAL = 1

}

object GeneralVariables {

    var appRunningFirstTime = true

}

object IntentsKeys {

    const val startFragment = "start_fragment"

}


object ServerManagement {
    var serverManager = ServerManager()
    const val activityConnectionOnCheckWait: Long = 4000
    const val viewConnectionOnCheckWait: Long = 5000
    const val dataRequestOnAttemptWait: Long = 2000
    const val baseUrl = "http://192.168.1.230:8000/"
}


object ThemeOptions {

    var darkTheme = false

}

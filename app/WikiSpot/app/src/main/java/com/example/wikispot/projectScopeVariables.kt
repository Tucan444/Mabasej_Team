package com.example.wikispot

import com.example.wikispot.modelClasses.ServerManager
import com.google.android.gms.maps.model.LatLng
import org.json.JSONArray


object GeneralVariables {

    var appRunningFirstTime = true

    var id: String? = null
    var name: String? = null

    var email:String? = null
    var phoneNumber: Int? = null

    const val max_amount_of_saved_messages = 32
    const val variableMissingKeyword = "_[{(V,a,r,i,a,b,l,e, ,m,i,s,s,i,n,g)}]_"

    const val translatePrefix = "[translate]-"

}

object IntentsKeys {

    const val startFragment = "start_fragment"

}


object ServerManagement {
    var serverManager = ServerManager()
    const val receiverConnectionOnCheckWait: Long = 4000
    const val viewConnectionOnCheckWait: Long = 5000
    const val chatConnectionOnCheckWait: Long = 1000
    const val dataRequestOnAttemptWait: Long = 2000
    const val imageRequestOnAttemptWait: Long = 2000
    var baseUrl = "http://192.168.1.156:8000/"
    var connectedServerId: Int? = null
    var selectedServerId = 0

    const val sensors_keyword = "_[{(S,e,n,s,o,r,s)}]_"

    var totalNumberOfRequestsSent = 0
}


object MapManagement {
    var connectedServerPosition: LatLng? = LatLng(0.toDouble(), 0.toDouble())
    var lastCoordinates = LatLng(0.toDouble(), 0.toDouble())
}


object ScreenParameters {
    var height = 1
    var width = 1
}


object CustomBackstackVariables {
    var infoFragmentBackDestination = "exploreFragment"
}


object ThemeOptions {
    var darkTheme = false
    var moreColors = false
}

object StartDirections {
    var settingsFragmentStartDirection: String? = null
}
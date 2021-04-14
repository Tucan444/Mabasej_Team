package com.example.wikispot.activities

import android.graphics.Bitmap
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wikispot.*
import com.example.wikispot.adapters.FileViewsAdapter
import com.example.wikispot.adapters.LabeledValuesAdapter
import com.example.wikispot.adapters.PlacePreviewsAdapter
import com.example.wikispot.fragments.*
import com.example.wikispot.modelClasses.JsonManager
import com.example.wikispot.modelClasses.JsonManagerLite
import com.example.wikispot.modelClasses.SettingsSaveManager
import com.example.wikispot.modelsForAdapters.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_explore.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_info.*
import kotlinx.android.synthetic.main.fragment_info.view.*


class MainActivity : AppCompatActivity() {

    override fun onBackPressed() {

        try {
            when (val currentlyShownFragment = mainFragmentHost.childFragmentManager.fragments[0]) {
                is chatFragment -> {
                    askToQuit()
                }
                is exploreFragment -> {
                    askToQuit()
                }
                is homeFragment -> {
                    askToQuit()
                }
                is mapFragment -> {
                    askToQuit()
                }
                is settingsFragment -> {
                    askToQuit()
                }
                is infoFragment -> {
                    println(CustomBackstackVariables.infoFragmentBackDestination)
                    when (CustomBackstackVariables.infoFragmentBackDestination) {
                        "exploreFragment" -> { currentlyShownFragment.goExploreFragment() }
                        "mapFragment" -> {currentlyShownFragment.goMapFragment()}
                    }

                }
            }
        } catch (e: Throwable) { println(e) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        doPreparations()
        loadSettings()
        setTheme(getThemeId())

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navController = findNavController(R.id.mainFragmentHost)
        mainBottomNavigationView.setupWithNavController(navController)

        handleExtras()
    }

    private fun doPreparations() {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        ScreenParameters.height = displayMetrics.heightPixels
        ScreenParameters.width = displayMetrics.widthPixels
    }

    override fun onResume() {
        super.onResume()
        // server communication

        val dataReceiver0: (String) -> Unit = { data0: String ->
            val dataReceiver1: (String) -> Unit = { data1: String ->
                val json = JsonManager(this, data1, "JSONObject")
                val names = json.currentJsonObject!!.names()

                try {
                    mainFragmentHost.childFragmentManager.fragments[0]?.let {
                        if (it is homeFragment) {
                            LabeledValuesSupplier.wipeData()

                            for (n in 0 until names!!.length()) {
                                val labeledValue = LabeledValue(names[n].toString(), json.getAttributeContent(names[n].toString()))
                                if (!LabeledValuesSupplier.checkIfContains(labeledValue)) {
                                    LabeledValuesSupplier.appendLabeledValue(labeledValue)
                                }
                            }

                            it.labeled_values_recycler_view.post {
                                val layoutManager = LinearLayoutManager(it.requireContext())
                                layoutManager.orientation = LinearLayoutManager.VERTICAL
                                it.labeled_values_recycler_view.layoutManager = layoutManager

                                val adapter = LabeledValuesAdapter(it.requireContext(), LabeledValuesSupplier.labeledValues)
                                it.labeled_values_recycler_view.adapter = adapter
                            }
                        }
                    }
                } catch (e: Throwable) { println("[debug] Exception in main activity, sensors connection : $e") }

            }

            if (!ServerManagement.serverManager.checkIfConnectionAlreadyExists("sensorsConnection")){
                ServerManagement.serverManager.addReceiverConnection(dataReceiver1, this, "sensorsConnection", data0.toInt(), ServerManagement.sensors_keyword)
            }

            // getting other needed information
            val dataReceiver2: (String) -> Unit = {data1: String ->
                var json = JsonManager(this, data1)
                json = JsonManager(this, json.findJsonObjectByAttribute("ID", data0.toInt()), "JSONObject")
                val positionsList = json.getAttributeContent("location").split(",")
                MapManagement.connectedServerPosition = LatLng(positionsList[0].toDouble(), positionsList[1].toDouble())
            }

            if (!ServerManagement.serverManager.checkIfConnectionAlreadyExists("mapConnection")){
                ServerManagement.serverManager.addReceiverConnection(dataReceiver2, this, "mapConnection", data0.toInt(), "", "GET_WHOLE_ARRAY")
            }

            val dataReceiver3: (String) -> Unit = { data1: String ->
                val json = JsonManager(this, data1)
                json.findJsonObjectByAttribute("ID", data0.toInt())

                fun updateFileViewsRecyclerView(fragment: Fragment) {
                    try {
                        fragment.homeFragmentInnerFragment?.let {
                            it.file_views_recycler_view.post {
                                val layoutManager = LinearLayoutManager(fragment.requireContext())
                                layoutManager.orientation = LinearLayoutManager.VERTICAL
                                it.file_views_recycler_view.layoutManager = layoutManager

                                val adapter = FileViewsAdapter(fragment.requireContext(), FileViewsSupplier.fileViews)
                                it.file_views_recycler_view.adapter = adapter
                            }
                        }
                    } catch (e: Throwable) { println("[debug] e1 that i  couldnt fix so try catch Exception: $e") }
                }

                try {
                    mainFragmentHost.childFragmentManager.fragments[0]?.let {
                        when (it) {

                            is homeFragment -> {

                                json.getAttributeContent("files")

                                for (n in 0 until json.currentJsonAttribute1!!.length()) {
                                    val fileInfo = JsonManagerLite(json.getAttributeContentByPath("files/$n"), "JSONObject")
                                    val filetype = fileInfo.getAttributeContentByPath("format").split(".")[1]
                                    val filename = fileInfo.getAttributeContentByPath("name")

                                    // handling text
                                    if ("txt json".contains(filetype)) {
                                        val fileView = FileView(filetype, filename, "$data0|||||$filename.$filetype")
                                        if (!FileViewsSupplier.checkIfContains(fileView)) {
                                            FileViewsSupplier.appendFileView(fileView)
                                            updateFileViewsRecyclerView(it)
                                        }
                                    }

                                    // handling images
                                    if ("jpg png".contains(filetype)) {
                                        val fileView = FileView(filetype, filename, null, "$data0|||||$filename.$filetype")
                                        if (!FileViewsSupplier.checkIfContains(fileView)) {
                                            FileViewsSupplier.appendFileView(fileView)
                                            updateFileViewsRecyclerView(it)
                                        }
                                    }

                                    // handling pdf files
                                    if ("pdf".contains(filetype)) {
                                        val fileView = FileView(filetype, filename, null, null, "${ServerManagement.baseUrl}files/$data0/$filename.$filetype")
                                        if (!FileViewsSupplier.checkIfContains(fileView)) {
                                            FileViewsSupplier.appendFileView(fileView)
                                            updateFileViewsRecyclerView(it)
                                        }

                                    }

                                }
                            }
                        }
                    }
                } catch (e: Throwable) { println("[debug] Exception in main activity, files data request : $e") }

            }

            ServerManagement.serverManager.addReceiverConnection(dataReceiver3, this, "fileViewsConnection", data0.toInt(), "", "GET_WHOLE_ARRAY")
        }

        ServerManagement.serverManager.getData(dataReceiver0, this, 0, "", "connected_id", 3)
        connectExploreFragmentAdapterModel()
    }

    override fun onPause() {
        PlaceSupplier.saveToCache(this)
        ServerManagement.serverManager.deleteConnection("sensorsConnection")
        ServerManagement.serverManager.deleteConnection("mapConnection")
        ServerManagement.serverManager.deleteConnection("fileViewsConnection")
        ServerManagement.serverManager.deleteConnection("exploreListConnection")
        super.onPause()
    }

    private fun handleExtras() {
        when (intent.getStringExtra(IntentsKeys.startFragment)) {
            "chatFragment" -> {
                mainBottomNavigationView.selectedItemId = R.id.chatFragment
            }
            "exploreFragment" -> {
                mainBottomNavigationView.selectedItemId = R.id.exploreFragment
            }
            // skipping home fragment because were already here
            "mapFragment" -> {
                mainBottomNavigationView.selectedItemId = R.id.mapFragment
            }
            "settingsFragment" -> {
                mainBottomNavigationView.selectedItemId = R.id.settingsFragment
            }
        }
    }

    private fun loadSettings() {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        val settingsSaveManager = SettingsSaveManager(this)
        settingsSaveManager.loadSettings()
    }

    private fun connectExploreFragmentAdapterModel () {
        // loading from cache
        PlaceSupplier.loadFromCache(this)

        // connecting to server
        val dataReceiver: (String) -> Unit = { data: String ->
            val json = JsonManager(this, data)

            PlaceSupplier.controlJson = JsonManagerLite(data)

            for (i in 1 until json.getLengthOfJsonArray()) {

                json.getJsonObject(i)
                val id = json.getAttributeContent("ID").toInt()
                val location = json.getAttributeContent("location")
                json.getAttributeContent("description")
                val title = json.getAttributeContent("title")
                val shortDescription = json.getAttributeContent("description_s")
                val place = PlacePreview(title, shortDescription, location, null, id)

                if (!PlaceSupplier.checkIfContains(place)) {

                    val imageReceiver: (Bitmap) -> Unit = { bitmap: Bitmap ->
                        place.img = bitmap
                    }

                    ServerManagement.serverManager.getImage(imageReceiver, id, json.getAttributeContentByPath("description/photo_s"), 3)

                    PlaceSupplier.appendPlace(place)
                } else {
                    val containingPlace = PlaceSupplier.getContainingInstance(place)
                    if ((containingPlace != null) and (containingPlace?.img == null)) {
                        val imageReceiver: (Bitmap) -> Unit = { bitmap: Bitmap ->
                            containingPlace?.img = bitmap
                        }

                        ServerManagement.serverManager.getImage(imageReceiver, id, json.getAttributeContentByPath("description/photo_s"), 3)
                    }

                    // checking if location wasn't changed
                    if ((containingPlace != null) and (containingPlace?.location != location)) {
                        containingPlace?.location = location

                        try {
                            mainFragmentHost.childFragmentManager.fragments[0]?.let {
                                if (it is exploreFragment) {
                                    it.explore_recycler_view.post {
                                        val layoutManager = LinearLayoutManager(it.requireContext())
                                        layoutManager.orientation = LinearLayoutManager.VERTICAL
                                        it.explore_recycler_view.layoutManager = layoutManager

                                        val adapter = PlacePreviewsAdapter(it.requireContext(), PlaceSupplier.places)
                                        it.explore_recycler_view.adapter = adapter
                                    }
                                }
                            }
                        } catch (e: Throwable) { println("[debug] e4 that i couldnt fix si try catch Exception: $e") }
                    }
                }

                json.clearSelectedAttribute()
            }
        }

        ServerManagement.serverManager.addReceiverConnection(dataReceiver, this, "exploreListConnection", 0, "", "GET_WHOLE_ARRAY", 10000)
    }

}

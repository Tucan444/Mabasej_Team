package com.example.wikispot.activities

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wikispot.*
import com.example.wikispot.adapters.LabeledValuesAdapter
import com.example.wikispot.fragments.*
import com.example.wikispot.modelClasses.JsonManager
import com.example.wikispot.modelClasses.JsonManagerLite
import com.example.wikispot.modelClasses.SettingsSaveManager
import com.example.wikispot.modelsForAdapters.LabeledValue
import com.example.wikispot.modelsForAdapters.LabeledValuesSupplier
import com.example.wikispot.modelsForAdapters.PlacePreview
import com.example.wikispot.modelsForAdapters.PlaceSupplier
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_info.*


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
                    currentlyShownFragment.goExploreFragment()
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
                println("[debug] $data1")

                try {
                    mainFragmentHost.childFragmentManager.fragments[0]?.let {
                        when (it) {
                            is chatFragment -> {
                            }
                            is exploreFragment -> {
                            }
                            is homeFragment -> {
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
                            is mapFragment -> {
                            }
                            is settingsFragment -> {
                            }
                            is infoFragment -> {
                            }
                            else -> println("[debug] unknown fragment in sensorsConnection")
                        }
                    }
                } catch (e: Throwable) { println(e) }

            }
            println(data0)

            if (!ServerManagement.serverManager.checkIfConnectionAlreadyExists("sensorsConnection")){
                ServerManagement.serverManager.addReceiverConnection(dataReceiver1, this, "sensorsConnection", data0.toInt(), ServerManagement.sensors_keyword)
            }

            // getting other needed information
            val dataReceiver2: (String) -> Unit = {data1: String ->
                var json = JsonManager(this, data1)
                json = JsonManager(this, json.findJsonObjectByAttribute("ID", data0.toInt()), "JSONObject")
                val positionsList = json.getAttributeContent("location").split(",")
                MapManagement.connectedServerTitle = json.getAttributeContentByPath("description/title")
                MapManagement.connectedServerPosition = LatLng(positionsList[0].toDouble(), positionsList[1].toDouble())
            }

            if (!ServerManagement.serverManager.checkIfConnectionAlreadyExists("mapConnection")){
                ServerManagement.serverManager.addReceiverConnection(dataReceiver2, this, "mapConnection", data0.toInt(), "", "GET_WHOLE_ARRAY")
            }
        }

        ServerManagement.serverManager.getData(dataReceiver0, this, 0, "", "connected_id", 3)
        connectExploreFragmentAdapterModel()
    }

    override fun onPause() {
        PlaceSupplier.saveToCache(this)
        ServerManagement.serverManager.deleteConnection("sensorsConnection")
        ServerManagement.serverManager.deleteConnection("mapConnection")
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

            if (PlaceSupplier.controlJson == null) {
                PlaceSupplier.controlJson = JsonManagerLite(data)
            }

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

                    ServerManagement.serverManager.getImage(imageReceiver, id, "test.png", 3)

                    PlaceSupplier.appendPlace(place)
                } else {
                    val containingPlace = PlaceSupplier.getContainingInstance(place)
                    if ((containingPlace != null) and (containingPlace?.img == null)) {
                        val imageReceiver: (Bitmap) -> Unit = { bitmap: Bitmap ->
                            containingPlace?.img = bitmap
                        }

                        ServerManagement.serverManager.getImage(imageReceiver, id, "test.png", 3)
                    }
                }

                json.clearSelectedAttribute()
            }
        }

        ServerManagement.serverManager.addReceiverConnection(dataReceiver, this, "exploreListConnection", 0, "", "GET_WHOLE_ARRAY", 10000)
    }

    private fun restartAppPartially() {  // todo remove if not used
        val intent = Intent(applicationContext, MainActivity::class.java)

        var currentNavHostFragmentName = "homeFragment"

        try {
            when (mainFragmentHost.childFragmentManager.fragments[0]) {
                is chatFragment -> {
                    currentNavHostFragmentName = "chatFragment"
                }
                is exploreFragment -> {
                    currentNavHostFragmentName = "exploreFragment"
                }
                is homeFragment -> {
                    currentNavHostFragmentName = "homeFragment"
                }
                is mapFragment -> {
                    currentNavHostFragmentName = "mapFragment"
                }
                is settingsFragment -> {
                    currentNavHostFragmentName = "settingsFragment"
                }
            }
        } catch (e: Throwable) { println(e) }

        intent.putExtra(IntentsKeys.startFragment, currentNavHostFragmentName)

        startActivity(intent)
        finish()
    }
}

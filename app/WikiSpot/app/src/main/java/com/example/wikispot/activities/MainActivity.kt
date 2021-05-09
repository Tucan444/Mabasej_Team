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

        val savedBaseUrl = this.getStringFromSharedPreferences("baseUrlSave")
        if (savedBaseUrl != "") {
            ServerManagement.baseUrl = savedBaseUrl
        }
    }

    override fun onResume() {
        super.onResume()
        // server communication

        connectExploreFragmentAdapterModel()
    }

    override fun onPause() {
        PlaceSupplier.saveToCache(this)
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
            "debugFragment" -> {
                StartDirections.settingsFragmentStartDirection = "debugFragment"
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

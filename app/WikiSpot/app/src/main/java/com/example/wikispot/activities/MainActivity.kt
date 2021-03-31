package com.example.wikispot.activities

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.wikispot.IntentsKeys
import com.example.wikispot.R
import com.example.wikispot.ServerManagement
import com.example.wikispot.fragments.*
import com.example.wikispot.getThemeId
import com.example.wikispot.modelClasses.JsonManager
import com.example.wikispot.modelClasses.JsonManagerLite
import com.example.wikispot.modelClasses.SettingsSaveManager
import com.example.wikispot.modelsForAdapters.PlacePreview
import com.example.wikispot.modelsForAdapters.PlaceSupplier
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm")
        builder.setMessage("Do you want to quit the application?")
        builder.setPositiveButton("Yes") { _, _ -> finish()}
        builder.setNegativeButton("No") { _, _ -> }
        builder.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        loadSettings()
        setTheme(getThemeId())

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navController = findNavController(R.id.mainFragmentHost)
        mainBottomNavigationView.setupWithNavController(navController)

        handleExtras()
    }

    override fun onResume() {
        super.onResume()
        // server communication

        val dataReceiver: (String) -> Unit = {data: String ->
            println("Data here: $data")

            try {
                when (mainFragmentHost.childFragmentManager.fragments[0]) {
                    is chatFragment -> {}
                    is exploreFragment -> {}
                    is homeFragment -> {}
                    is mapFragment -> {}
                    is settingsFragment -> {}
                }
            } catch (e: Throwable) { println(e) }

        }

        ServerManagement.serverManager.addReceiverConnection(dataReceiver, this, "mainConnection", 0, "test0.json")
        connectExploreFragmentAdapterModel()
    }

    override fun onPause() {
        PlaceSupplier.saveToCache(this)
        ServerManagement.serverManager.deleteConnection("mainConnection")
        ServerManagement.serverManager.deleteConnection("exploreListConnection")
        super.onPause()
    }

    private fun handleExtras() {
        when (intent.getStringExtra(IntentsKeys.startFragment)) {
            "chatFragment" -> {mainBottomNavigationView.selectedItemId = R.id.chatFragment}
            "exploreFragment" -> {mainBottomNavigationView.selectedItemId = R.id.exploreFragment}
            // skipping home fragment because were already here
            "mapFragment" -> {mainBottomNavigationView.selectedItemId = R.id.mapFragment}
            "settingsFragment" -> {mainBottomNavigationView.selectedItemId = R.id.settingsFragment}
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
        val dataReceiver: (String) -> Unit = {data: String ->
            val json = JsonManager(this, data)

            if (PlaceSupplier.controlJson == null) {
                PlaceSupplier.controlJson = JsonManagerLite(data)
            }

            for (i in 1 until json.getLengthOfJsonArray()) {  // todo change to 1

                json.getJsonObject(i)
                val id = json.getAttributeContent("ID").toInt()
                json.getAttributeContent("description")
                val title = json.getAttributeContent("title")
                val shortDescription = json.getAttributeContent("description_s")
                val place = PlacePreview(title, shortDescription, null, id)

                if (!PlaceSupplier.checkIfContains(place)) {

                    val imageReceiver: (Bitmap) -> Unit = {bitmap: Bitmap ->
                        place.img = bitmap
                    }

                    ServerManagement.serverManager.getImage(imageReceiver, id, "test.png", 3)

                    PlaceSupplier.appendPlace(place)
                } else {
                    val containingPlace = PlaceSupplier.getContainingInstance(place)
                    if ((containingPlace != null) and (containingPlace?.img == null)) {
                        val imageReceiver: (Bitmap) -> Unit = {bitmap: Bitmap ->
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
                is chatFragment -> {currentNavHostFragmentName = "chatFragment"}
                is exploreFragment -> {currentNavHostFragmentName= "exploreFragment"}
                is homeFragment -> {currentNavHostFragmentName = "homeFragment"}
                is mapFragment -> {currentNavHostFragmentName = "mapFragment"}
                is settingsFragment -> {currentNavHostFragmentName = "settingsFragment"}
            }
        } catch (e: Throwable) { println(e) }

        intent.putExtra(IntentsKeys.startFragment, currentNavHostFragmentName)

        startActivity(intent)
        finish()
    }
}

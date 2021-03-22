package com.example.wikispot.activities

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.wikispot.*
import com.example.wikispot.modelClasses.SettingsSaveManager
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
        /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    ManifestRelatedVariables.REQUEST_READ_EXTERNAL)
        } */
        loadSettings()
        ServerManagement.serverManager.addActivityConnection(this, "main",0)
        ServerManagement.serverManager.getData(this, this, 0, "", "", true)

        setTheme(getThemeId())
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navController = findNavController(R.id.mainFragmentHost)
        mainBottomNavigationView.setupWithNavController(navController)

        handleExtras()

        println("[debug] ${getDataFromServer()}")
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
        val settingsSaveManager = SettingsSaveManager(this)
        settingsSaveManager.loadSettings()
    }

}

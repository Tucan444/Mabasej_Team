package com.example.wikispot.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.wikispot.*
import com.example.wikispot.fragments.*
import com.example.wikispot.modelClasses.SettingsSaveManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*

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
                    is homeFragment -> {
                        val view = mainFragmentHost.childFragmentManager.fragments[0].homeFragmentTextIdTest
                        view.post {
                            view.text = data
                        }
                    }
                    is mapFragment -> {}
                    is settingsFragment -> {}
                }
            } catch (e: Throwable) { println(e) }

        }

        ServerManagement.serverManager.addReceiverConnection(dataReceiver, this, "mainConnection", 0, "test0.json")
    }

    override fun onPause() {
        super.onPause()
        ServerManagement.serverManager.deleteConnection("mainConnection")
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

    private fun restartAppPartially() {
        val intent = Intent(applicationContext, MainActivity::class.java)

        intent.putExtra(IntentsKeys.startFragment, "settingsFragment")

        startActivity(intent)
        finish()
    }
}

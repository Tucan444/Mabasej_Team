package com.example.wikispot.fragments

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.Navigation
import com.example.wikispot.*
import com.example.wikispot.activities.MainActivity
import com.example.wikispot.modelClasses.ServerManager
import com.example.wikispot.modelClasses.SettingsSaveManager
import kotlinx.android.synthetic.main.fragment_settings.*
import javax.xml.transform.sax.TemplatesHandler


class settingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var settingsSaveManager: SettingsSaveManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        StartDirections.settingsFragmentStartDirection?.let {
            when (StartDirections.settingsFragmentStartDirection) {
                "debugFragment" -> {
                    Navigation.findNavController(debugBtn).navigate(R.id.navigateToDebugFragment)
                    StartDirections.settingsFragmentStartDirection = null
                }
            }
        }

        settingsSaveManager = SettingsSaveManager(requireContext())

        loadSettings()

        debugBtn.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.navigateToDebugFragment)
        }

        darkThemeSwitch.setOnCheckedChangeListener { _, isChecked ->
            ThemeOptions.darkTheme = isChecked
            settingsSaveManager.saveSettings()
            restartAppPartially()
        }

        moreColorsSwitch.setOnCheckedChangeListener { _, isChecked ->
            ThemeOptions.moreColors = isChecked
            settingsSaveManager.saveSettings()
            restartAppPartially()
        }
    }

    private fun loadSettings() {
        darkThemeSwitch.isChecked = ThemeOptions.darkTheme
        moreColorsSwitch.isChecked = ThemeOptions.moreColors
    }

    private fun restartAppPartially() {
        val intent = Intent(context?.applicationContext, MainActivity::class.java)

        intent.putExtra(IntentsKeys.startFragment, "settingsFragment")

        ServerManagement.serverManager.clearConnections()

        startActivity(intent)
        activity?.finish()
    }
}

package com.example.wikispot.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.Navigation
import com.example.wikispot.IntentsKeys
import com.example.wikispot.R
import com.example.wikispot.ServerManagement
import com.example.wikispot.ThemeOptions
import com.example.wikispot.activities.MainActivity
import com.example.wikispot.modelClasses.ServerManager
import com.example.wikispot.modelClasses.SettingsSaveManager
import kotlinx.android.synthetic.main.fragment_settings.*


class settingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var settingsSaveManager: SettingsSaveManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
    }

    private fun loadSettings() {
        if (ThemeOptions.darkTheme) {
            darkThemeSwitch.isChecked = true
        }
    }

    private fun restartAppPartially() {
        val intent = Intent(context?.applicationContext, MainActivity::class.java)

        intent.putExtra(IntentsKeys.startFragment, "settingsFragment")

        ServerManagement.serverManager.clearConnections()

        startActivity(intent)
        activity?.finish()
    }
}

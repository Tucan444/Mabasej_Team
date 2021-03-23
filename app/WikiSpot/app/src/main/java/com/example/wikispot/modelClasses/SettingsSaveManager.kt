package com.example.wikispot.modelClasses

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.example.wikispot.GeneralVariables
import com.example.wikispot.IntentsKeys
import com.example.wikispot.ThemeOptions
import com.example.wikispot.activities.MainActivity

class SettingsSaveManager(val context: Context) {

    fun loadSettings() {
        val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

        ThemeOptions.darkTheme = sharedPreferences.getBoolean("darkMode", ThemeOptions.darkTheme)

        // checking if we want to use system default theme
        try {
            GeneralVariables.appRunningFirstTime = sharedPreferences.getBoolean("appRunningFirstTime", true)

            if (GeneralVariables.appRunningFirstTime) {
                ThemeOptions.darkTheme = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES)
            }
        } catch (e: Throwable) {
            println(e)
        }

        // saving settings cause some things might change based on system preferences
        saveSettings()
    }

    fun saveSettings() {
        val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.apply{
            putBoolean("appRunningFirstTime", false)
            putBoolean("darkMode", ThemeOptions.darkTheme)
        }.apply()
    }

}

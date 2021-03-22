package com.example.wikispot.modelClasses

import android.content.Context
import com.example.wikispot.ThemeOptions

class SettingsSaveManager(val context: Context) {

    fun loadSettings() {
        val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

        ThemeOptions.darkTheme = sharedPreferences.getBoolean("darkMode", ThemeOptions.darkTheme)
        ThemeOptions.actionBar = sharedPreferences.getBoolean("actionBar", ThemeOptions.actionBar)
    }

    fun saveSettings() {
        val sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.apply{
            putBoolean("darkMode", ThemeOptions.darkTheme)
            putBoolean("actionBar", ThemeOptions.actionBar)
        }.apply()
    }

}

package com.example.wikispot.modelClasses

import android.content.Context
import com.example.wikispot.getStringFromSharedPreferences
import com.example.wikispot.saveString
import com.example.wikispot.showToast
import org.json.JSONArray
import org.json.JSONObject

data class JsonManagerLite(val data: String, val inputType: String = "JSONArray") {

    var jsonArray: JSONArray? = null
    var currentJsonObject: JSONObject? = null
    private var currentJsonAttribute0: JSONObject? = null
    private var currentJsonAttribute1: JSONArray? = null

    init {
        if (inputType == "JSONArray") {
            jsonArray = JSONArray(data)
            try {
                currentJsonObject = jsonArray!!.getJSONObject(0)
            } catch (exception: Throwable) {}

        } else if (inputType == "JSONObject") {
            currentJsonObject = JSONObject(data)
        }
    }

    fun getJsonObject(i: Int): JSONObject? {
        jsonArray?.let {
            currentJsonObject = jsonArray?.getJSONObject(i)
            return currentJsonObject
        }

        return null
    }

    fun getAttributeContent(name: String): String {
        if (currentJsonObject != null) {
            if (currentJsonAttribute0 != null) {
                try {
                    currentJsonAttribute0 = currentJsonAttribute0!!.getJSONObject(name)
                    return currentJsonAttribute0.toString()
                } catch (exception: Throwable) {
                    try {
                        currentJsonAttribute1 = currentJsonAttribute0!!.getJSONArray(name)
                        currentJsonAttribute0 = null
                        return currentJsonAttribute1.toString()
                    } catch (exception: Throwable) {
                        try {
                            return currentJsonAttribute0!!.get(name).toString()
                        } catch (exception: Throwable) { }
                    }
                }
            } else if (currentJsonAttribute1 != null) {
                try {
                    currentJsonAttribute0 = currentJsonAttribute1!!.getJSONObject(name.toInt())
                    return currentJsonAttribute0.toString()
                } catch (exception: Throwable) {
                    try {
                        currentJsonAttribute1 = currentJsonAttribute1!!.getJSONArray(name.toInt())
                        currentJsonAttribute0 = null
                        return currentJsonAttribute1.toString()
                    } catch (exception: Throwable) {
                        try {
                            return currentJsonAttribute1!!.get(name.toInt()).toString()
                        } catch (exception: Throwable) { }
                    }
                }
            } else {
                try {
                    currentJsonAttribute0 = currentJsonObject!!.getJSONObject(name)
                    return currentJsonAttribute0.toString()
                } catch (exception: Throwable) {
                    try {
                        currentJsonAttribute1 = currentJsonObject!!.getJSONArray(name)
                        currentJsonAttribute0 = null
                        return currentJsonAttribute1.toString()
                    } catch (exception: Throwable) {
                        try {
                            return currentJsonObject!!.get(name).toString()
                        } catch (exception: Throwable) { }
                    }
                }
            }
        }

        return ""
    }

    fun getAttributeContentByPath(path: String): String {
        val steps = path.split("/")

        val currentJsonAttributesBackup = listOf(currentJsonAttribute0, currentJsonAttribute1)  // backing up selected jsonAttributes

        // getting the attribute
        clearSelectedAttribute()
        var result: Any? = null
        for (step in steps) {
            try {
                result = getAttributeContent(step)
            } catch (exception: Throwable) {
                // loading back saved json attributes
                currentJsonAttribute0 = currentJsonAttributesBackup[0] as JSONObject?
                currentJsonAttribute1 = currentJsonAttributesBackup[1] as JSONArray?
                return ""
            }
        }

        // loading back saved json attributes
        currentJsonAttribute0 = currentJsonAttributesBackup[0] as JSONObject?
        currentJsonAttribute1 = currentJsonAttributesBackup[1] as JSONArray?

        // returning result
        return result.toString()
    }

    fun clearSelectedAttribute() {
        currentJsonAttribute0 = null
        currentJsonAttribute1 = null
    }

    fun getLengthOfJsonArray(): Int {
        return if (jsonArray != null) {
            jsonArray!!.length()
        } else {
            0
        }
    }

    fun getCurrentJsonAttributeContent(): String {
        if (currentJsonAttribute0 != null) {
            return currentJsonAttribute0.toString()
        } else if (currentJsonAttribute1 != null) {
            return currentJsonAttribute1.toString()
        }
        return "get json attribute first"
    }
}

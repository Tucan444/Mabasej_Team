package com.example.wikispot.modelClasses

import android.content.Context
import com.example.wikispot.getStringFromSharedPreferences
import com.example.wikispot.saveString
import com.example.wikispot.showToast
import org.json.JSONArray
import org.json.JSONObject

data class JsonManager(private val context: Context, val data: String, val inputType: String = "JSONArray", val debug: Boolean = false) {

    var jsonArray: JSONArray? = null
    var currentJsonObject: JSONObject? = null
    var currentJsonAttribute0: JSONObject? = null
    var currentJsonAttribute1: JSONArray? = null

    init {
        if (inputType == "JSONArray") {
            jsonArray = JSONArray(data)
            try {
                currentJsonObject = jsonArray!!.getJSONObject(0)
            } catch (exception: Throwable) {}

            if (debug) {
                println("[debug] Content of received JSONArray is ${jsonArray.toString()}")
            }

        } else if (inputType == "JSONObject") {
            currentJsonObject = JSONObject(data)

            if (debug) {
                println("[debug] Content of received JSONObject is ${currentJsonObject.toString()}")
            }
        }
    }

    fun getJsonObject(i: Int): JSONObject? {
        jsonArray?.let {
            if ((i >= jsonArray!!.length()) or (i < 0)) {
                context.showToast("Index out of range")
            } else {
                currentJsonObject = jsonArray?.getJSONObject(i)
                return currentJsonObject
            }
        }

        if (jsonArray == null) {
            context.showToast("Json Array is null")
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
                        } catch (exception: Throwable) {
                            if (debug) {
                                context.showToast("Invalid attribute name: $name")
                            }
                        }
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
                        } catch (exception: Throwable) {
                            if (debug) {
                                context.showToast("Invalid attribute name: $name")
                            }
                        }
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
                        } catch (exception: Throwable) {
                            if (debug) {
                                context.showToast("Invalid attribute name: $name")
                            }
                        }
                    }
                }
            }
        } else {
            if (debug) {
                context.showToast("Invalid attribute name: $name")
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
                context.showToast("Invalid path")
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
            if (debug) {
                println("[debug] Length of json array is ${jsonArray!!.length()}")
            }
            jsonArray!!.length()
        } else {
            println("[debug] Length of json array is 0")
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

    fun findJsonObjectByAttribute(attributePath: String, value: Any): String {
        val currentJsonObjectSave = currentJsonObject

        for (i in 0 until getLengthOfJsonArray()) {

            getJsonObject(i)

            val attributeContent = getAttributeContentByPath(attributePath)

            if (attributeContent != "null") {

                if (attributeContent == value.toString()) {
                    return currentJsonObject.toString()
                }

            }

        }

        currentJsonObject = currentJsonObjectSave
        return currentJsonObject.toString()

    }

    // saving and loading

    fun saveJson(accessKey: String) {

        // finding data that could be saved
        if (jsonArray != null) {
            context.saveString(accessKey, jsonArray.toString(), "jsonStrings")
        } else if (currentJsonObject != null) {
            context.saveString(accessKey, currentJsonObject.toString(), "jsonStrings")
        } else if (currentJsonAttribute0 != null) {
            context.saveString(accessKey, currentJsonAttribute0.toString(), "jsonStrings")
        } else if (currentJsonAttribute1 != null) {
            context.saveString(accessKey, currentJsonAttribute1.toString(), "jsonStrings")
        } else {
            context.showToast("Nothing to save")
        }

    }

    fun loadJson(context: Context, accessKey: String, inputType: String = "JSONArray", debug: Boolean = false): JsonManager {
        return JsonManager(context, context.getStringFromSharedPreferences(accessKey, "jsonStrings"), inputType, debug)
    }

}

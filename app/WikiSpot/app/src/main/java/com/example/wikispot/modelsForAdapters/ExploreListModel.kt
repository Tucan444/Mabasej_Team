package com.example.wikispot.modelsForAdapters

import android.content.Context
import android.graphics.Bitmap
import android.media.Image
import com.example.wikispot.getStringFromSharedPreferences
import com.example.wikispot.modelClasses.JsonManager
import com.example.wikispot.modelClasses.JsonManagerLite
import com.example.wikispot.saveString
import org.json.JSONArray

data class PlacePreview(var title: String, var description: String, var img: Bitmap? = null, val id: Int?=null) {

    init {
        val words = description.split(" ")
        description = ""
        var lastLine = ""

        for (word in words) {
            if (lastLine.length + word.length < 40) {
                if (lastLine != "") {
                    lastLine += " "
                    description += " "
                }
                lastLine += word
                description += word
            } else {
                description += "\n$word"
                lastLine = word
            }
        }
    }
}

object PlaceSupplier {

    var controlJson: JsonManagerLite? = null

    var places = arrayOf<PlacePreview?>()

    fun appendPlace(place: PlacePreview) {
        val array = places.copyOf(places.size + 1)
        array[places.size] = place
        places = array
    }

    fun checkIfContains(place: PlacePreview): Boolean {
        for (n in places.indices) {
            places[n]?.let {
                if (places[n]?.title == place.title) {
                    if (places[n]?.description == place.description) {
                        return true
                    }
                }
            }
        }
        return false
    }

    fun getContainingInstance(place: PlacePreview): PlacePreview? {
        for (n in places.indices) {
            places[n]?.let {
                if (places[n]?.title == place.title) {
                    if (places[n]?.description == place.description) {
                        return places[n]
                    }
                }
            }
        }
        return null
    }

    // loading from and saving to cache
    fun loadFromCache(context: Context) {
        println("loading")
        var save = context.getStringFromSharedPreferences("placePreviews", "exploreFragmentCache")
        if (save.isEmpty()) {
            save = "[]"
        }
        val jsonManager = JsonManager(context, save)
        for (n in 0 until jsonManager.getLengthOfJsonArray()) {
            val savedData = jsonManager.jsonArray?.get(n).toString().split("|||||")
            val place = PlacePreview(savedData[0], savedData[1], null, savedData[2].toInt())
            if (!checkIfContains(place)) {
                appendPlace(place)
            }
        }
    }

    fun saveToCache(context: Context) {
        val save = JSONArray()

        for (n in places.indices) {
            val place = places[n]
            if (getSavePermission(place)) {
                save.put(n, "${place!!.title}|||||${place.description}|||||${place.id}")
            }
        }
        //save.put("hi|||||sdhsiujdghsiuy|||||45")

        context.saveString("placePreviews", save.toString(), "exploreFragmentCache")
    }

    private fun getSavePermission(place: PlacePreview?): Boolean {
        if (controlJson == null) {
            return true
        }

        place?.let {
            for (n in 1 until controlJson!!.getLengthOfJsonArray()) {
                controlJson!!.getJsonObject(n)
                if (place.id == controlJson!!.getAttributeContent("ID").toInt()) {
                    if (place.title == controlJson!!.getAttributeContentByPath("description/title")) {
                        val tempPlace = PlacePreview("", controlJson!!.getAttributeContentByPath("description/description_s"))
                        if (place.description == tempPlace.description) {
                            return true
                        }
                    }
                }
            }
        }

        return false
    }

}
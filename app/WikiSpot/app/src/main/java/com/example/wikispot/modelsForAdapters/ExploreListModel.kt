package com.example.wikispot.modelsForAdapters

import android.content.Context
import android.graphics.Bitmap
import com.example.wikispot.getStringFromSharedPreferences
import com.example.wikispot.modelClasses.JsonManager
import com.example.wikispot.modelClasses.JsonManagerLite
import com.example.wikispot.saveString
import org.json.JSONArray

data class PlacePreview(var title: String, var description: String, var location: String? = null, var img: Bitmap? = null, val id: Int?=null) {

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

    var places = arrayOf<PlacePreview?>(
            PlacePreview("River", "", "39.94071648123733,-85.9346308193051", null, 12),
            PlacePreview("Velky Manin", "", "49.12590793522579,18.49571849264312", null, 16),
            PlacePreview("Klapy", "", "49.161527643132175,18.41231657316252", null, 18)
    )

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
            println("[debug] saved data is $savedData")
            val place = PlacePreview(savedData[0], savedData[1], savedData[2], null, savedData[3].toInt())
            if (!checkIfContains(place)) {
                appendPlace(place)
            }
        }
    }

    fun saveToCache(context: Context) {
        val save = JSONArray()

        var i = 0
        for (n in places.indices) {
            val place = places[n]
            if (getSavePermission(place)) {
                save.put(i, "${place!!.title}|||||${place.description}|||||${place.location}|||||${place.id}")
                i++
            }
        }

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
                        val tempPlace = PlacePreview("", controlJson!!.getAttributeContentByPath("description/description_s"),
                        controlJson!!.getAttributeContentByPath("location"))
                        if (place.description == tempPlace.description) {
                            if (place.location == tempPlace.location) {
                                return true
                            }
                        }
                    }
                }
            }
        }
        return false
    }

}

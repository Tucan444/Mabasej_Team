package com.example.wikispot.modelsForAdapters

import android.media.Image

data class PlacePreview(var title: String, var description: String, var img: Image? = null) {

    init {
        val words = description.split(" ")
        description = ""
        var lastLine = ""

        for (word in words) {
            if (lastLine.length + word.length < 40) {
                lastLine += " $word"
                description += " $word"
            } else {
                description += "\n $word"
                lastLine = " $word"
            }
        }
    }
}

object PlaceSupplier {

    var places = arrayOf<PlacePreview?>(
        PlacePreview("Castle", "Its ruins had been repaired to stable state."),
        PlacePreview("Library", "You can find books here."),
        PlacePreview("Bakery", "You can buy bread here."),
        PlacePreview("School", "You can learn stuff here."),
        PlacePreview("Castle", "Its ruins had been repaired to stable state."),
        PlacePreview("Library", "You can find books here."),
        PlacePreview("Bakery", "You can buy bread here."),
        PlacePreview("School", "You can learn stuff here."),
        PlacePreview("Library", "You can find books here."),
        PlacePreview("Bakery", "You can buy bread here.")
    )

    fun appendPlace(place: PlacePreview) {
        val array = places.copyOf(places.size + 1)
        array[places.size] = place
        places = array
    }

}
package com.example.wikispot.modelsForAdapters

import android.graphics.Bitmap

data class FileView(val filetype: String, val filename: String, var textInfo: String? = null, var imgInfo: String? = null, var pdfUrl: String? = null)


object FileViewsSupplier {

    var fileViews = arrayOf<FileView?>()

    fun appendFileView(fileView: FileView) {
        val array = fileViews.copyOf(fileViews.size + 1)
        array[fileViews.size] = fileView
        fileViews = array
    }

    fun checkIfContains(fileView: FileView): Boolean{
        for (n in fileViews.indices) {
            if (fileViews[n]!!.filename == fileView.filename) {
                if (fileViews[n]!!.filetype == fileView.filetype) {
                    return true
                }
            }
        }
        return false
    }

    fun wipeData() {
        fileViews = arrayOf()
    }

}


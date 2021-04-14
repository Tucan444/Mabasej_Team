package com.example.wikispot.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wikispot.R
import com.example.wikispot.ServerManagement
import com.example.wikispot.adapters.FileViewsAdapter
import com.example.wikispot.adapters.LabeledValuesAdapter
import com.example.wikispot.modelClasses.JsonManager
import com.example.wikispot.modelClasses.JsonManagerLite
import com.example.wikispot.modelsForAdapters.FileView
import com.example.wikispot.modelsForAdapters.FileViewsSupplier
import com.example.wikispot.modelsForAdapters.LabeledValue
import com.example.wikispot.modelsForAdapters.LabeledValuesSupplier
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.fragment_info.*
import java.io.File


class infoFragment : Fragment(R.layout.fragment_info) {

    private val args: infoFragmentArgs by navArgs()
    var location: LatLng? = null
    var loadAutomatically = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        LabeledValuesSupplier.wipeData()
        FileViewsSupplier.wipeData()
        updateSensorsRecyclerView()
        updateFileViewsRecyclerView()

        try {
            loadAutomatically = args.loadAutomatically
        } catch (e: Throwable) { println("[debug] Exception in Info Fragment while getting args: $e") }

        if (loadAutomatically) {
            load()
        }

        locationBtn.setOnClickListener {
            if (loadAutomatically) {
                if (location != null) {
                    val action = infoFragmentDirections.infoFragmentToMapFragment(location!!)
                    Navigation.findNavController(it).navigate(action)
                }
            } else {
                Navigation.findNavController(it).navigate(R.id.homeFragment_to_mapFragment)
            }
        }
    }

    private fun load() {
        val serverId = ServerManagement.selectedServerId

        val dataReceiver: (String) -> Unit = { data: String ->
            context?.let {
                try {
                    val json = JsonManager(requireContext(), data)
                    json.findJsonObjectByAttribute("ID", serverId)

                    mainTitle.post {
                        mainTitle.text = json.getAttributeContentByPath("description/title")
                    }

                    mainDescription.post {
                        this.mainDescription.text = json.getAttributeContentByPath("description/description_s")
                    }

                    val imageReceiver1: (Bitmap) -> Unit = { bitmap: Bitmap ->
                        mainImage.post {
                            mainImage?.let {
                                mainImage.setImageBitmap(bitmap)
                            }
                        }
                    }

                    val coordinates = json.getAttributeContent("location").split(",")
                    location = LatLng(coordinates[0].toDouble(), coordinates[1].toDouble())

                    ServerManagement.serverManager.getImage(imageReceiver1, json.getAttributeContent("ID").toInt(), "test0.jpg", 2)

                    // getting files

                    json.getAttributeContent("files")

                    for (n in 0 until json.currentJsonAttribute1!!.length()) {
                        val fileInfo = JsonManagerLite(json.getAttributeContentByPath("files/$n"), "JSONObject")
                        val filetype = fileInfo.getAttributeContentByPath("format").split(".")[1]
                        val filename = fileInfo.getAttributeContentByPath("name")

                        // handling text
                        if ("txt json".contains(filetype)) {
                            val fileView = FileView(filetype, filename, "$serverId|||||$filename.$filetype")
                            if (!FileViewsSupplier.checkIfContains(fileView)) {
                                FileViewsSupplier.appendFileView(fileView)
                                updateFileViewsRecyclerView()
                            }
                        }

                        // handling images
                        if ("jpg png".contains(filetype)) {
                            val fileView = FileView(filetype, filename, null, "$serverId|||||$filename.$filetype")
                            if (!FileViewsSupplier.checkIfContains(fileView)) {
                                FileViewsSupplier.appendFileView(fileView)
                                updateFileViewsRecyclerView()
                            }
                        }

                        // handling pdf files
                        if ("pdf".contains(filetype)) {
                            val fileView = FileView(filetype, filename, null, null, "${ServerManagement.baseUrl}files/$serverId/$filename.$filetype")
                            if (!FileViewsSupplier.checkIfContains(fileView)) {
                                FileViewsSupplier.appendFileView(fileView)
                                updateFileViewsRecyclerView()
                            }

                        }
                    }
                } catch (e: Throwable) { println("[debug] exception in infoFragment load data request Exception: $e") }
            }
        }

        val sensorsDataReceiver: (String) -> Unit = {data: String ->
            try {
                context?.let {
                    val json = JsonManager(requireContext(), data, "JSONObject")
                    val names = json.currentJsonObject!!.names()

                    LabeledValuesSupplier.wipeData()

                    if (names != null) {
                        for (n in 0 until names.length()) {
                            val labeledValue = LabeledValue(names[n].toString(), json.getAttributeContent(names[n].toString()))
                            if (!LabeledValuesSupplier.checkIfContains(labeledValue)) {
                                LabeledValuesSupplier.appendLabeledValue(labeledValue)
                            }
                        }
                    }

                    updateSensorsRecyclerView()
                }
            } catch (e: Throwable) { println("[debug] Exception in info fragment, load, sensorsDataReceiver : $e") }
        }

        context?.let {
            ServerManagement.serverManager.getData(dataReceiver, requireContext(), serverId, "", "GET_WHOLE_ARRAY")
            ServerManagement.serverManager.addReceiverConnection(sensorsDataReceiver, requireContext(), "infoFragmentSensorsConnection",
                    serverId, ServerManagement.sensors_keyword)
        }
    }

    override fun onPause() {
        ServerManagement.serverManager.deleteConnection("infoFragmentSensorsConnection")
        super.onPause()
    }

    private fun updateSensorsRecyclerView() {

        try {
            labeled_values_recycler_view.post {
                val layoutManager = LinearLayoutManager(context)
                layoutManager.orientation = LinearLayoutManager.VERTICAL
                labeled_values_recycler_view.layoutManager = layoutManager

                val adapter = context?.let { LabeledValuesAdapter(it, LabeledValuesSupplier.labeledValues) }
                labeled_values_recycler_view.adapter = adapter
            }
        } catch (e: Throwable) { println("[debug] e3 that i couldnt fix so try catch Exception: $e") }

    }

    private fun updateFileViewsRecyclerView() {

        try {
            file_views_recycler_view.post {
                val layoutManager = LinearLayoutManager(context)
                layoutManager.orientation = LinearLayoutManager.VERTICAL
                file_views_recycler_view.layoutManager = layoutManager

                val adapter = context?.let { FileViewsAdapter(it, FileViewsSupplier.fileViews) }
                file_views_recycler_view.adapter = adapter
            }
        } catch (e: Throwable) { println("[debug] e2 that i couldnt fix so try catch Exception: $e") }

    }

    fun goExploreFragment() {
        Navigation.findNavController(mainTitle).navigate(R.id.navigateBackToExploreFragment)
    }

    fun goMapFragment() {
        Navigation.findNavController(mainTitle).navigate(R.id.infoFragment_to_mapFragment)
    }

}

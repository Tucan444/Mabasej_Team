package com.example.wikispot.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wikispot.GeneralVariables
import com.example.wikispot.MapManagement
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
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_info.*
import kotlinx.android.synthetic.main.fragment_info.view.*


class homeFragment : Fragment(R.layout.fragment_home) {

    var infoFragmentLoadedIn = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadCache()

        chatBtn.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.homeFragment_to_chatFragment)
        }
    }

    override fun onResume() {
        super.onResume()

        // connecting to server

        val serverConnectorThread = Thread(ServerConnector())
        serverConnectorThread.start()

        val dataReceiver0: (String) -> Unit = { data: String ->
            try {
                val json = JsonManager(requireContext(), data)

                json.findJsonObjectByAttribute("ID", json.getAttributeContent("connected_id"))

                if (!infoFragmentLoadedIn) {

                    infoFragmentLoadedIn = true

                    val phoneNumberLoaded = json.getAttributeContentByPath("description/phone_number")
                    if (phoneNumberLoaded != GeneralVariables.variableMissingKeyword) {
                        GeneralVariables.phoneNumber = phoneNumberLoaded.toInt()
                    }

                    val emailLoaded = json.getAttributeContentByPath("description/email")
                    if (emailLoaded != GeneralVariables.variableMissingKeyword) {
                        GeneralVariables.email = emailLoaded
                    }

                    homeFragmentInnerFragment.post {
                        homeFragmentInnerFragment?.let { fragment ->
                            val title = json.getAttributeContentByPath("description/title")
                            val description = json.getAttributeContentByPath("description/description_l")

                            if (title != GeneralVariables.variableMissingKeyword) {
                                fragment.mainTitle.text = title
                            }

                            if (description != GeneralVariables.variableMissingKeyword) {
                                fragment.mainDescription.text = description
                            }
                        }
                    }

                    val imageReceiver: (Bitmap) -> Unit = { bitmap: Bitmap ->
                        homeFragmentInnerFragment?.let {
                            homeFragmentInnerFragment.post {
                                try {
                                    homeFragmentInnerFragment.mainImage.setImageBitmap(bitmap)
                                } catch (e: Throwable) { println("[debug] e7 Exception: $e") }
                            }
                        }
                    }

                    ServerManagement.serverManager.getImage(imageReceiver, json.getAttributeContent("ID").toInt(),
                            json.getAttributeContentByPath("description/photo_b"), 3)

                }
            } catch (e: Throwable) { println(e) }
        }

        val dataReceiver1: (String) -> Unit = {connectedId: String ->
            ServerManagement.connectedServerId = connectedId.toInt()
        }

        ServerManagement.serverManager.getData(dataReceiver0, requireContext(), 0, "", "GET_WHOLE_ARRAY", 4)
        ServerManagement.serverManager.getData(dataReceiver1, requireContext(), 0, "", "connected_id", 3)

    }

    override fun onPause() {
        super.onPause()
        ServerManagement.serverManager.deleteConnection("sensorsConnection")
        ServerManagement.serverManager.deleteConnection("mapConnection")
        ServerManagement.serverManager.deleteConnection("fileViewsConnection")
        saveCache()
    }

    private fun tryConnectingToServer() {
        ServerManagement.connectedServerId?.let{ connectedServerId: Int ->
            context?.let {
                val dataReceiver1: (String) -> Unit = { data1: String ->

                    try {
                        val json = JsonManager(requireContext(), data1, "JSONObject")
                        val names = json.currentJsonObject!!.names()

                        names?.let {
                            LabeledValuesSupplier.wipeData()

                            for (n in 0 until names.length()) {
                                val labeledValue = LabeledValue(names[n].toString(), json.getAttributeContent(names[n].toString()))
                                if (!LabeledValuesSupplier.checkIfContains(labeledValue)) {
                                    LabeledValuesSupplier.appendLabeledValue(labeledValue)
                                }
                            }

                            labeled_values_recycler_view.post {
                                val layoutManager = LinearLayoutManager(requireContext())
                                layoutManager.orientation = LinearLayoutManager.VERTICAL
                                labeled_values_recycler_view.layoutManager = layoutManager

                                val adapter = LabeledValuesAdapter(requireContext(), LabeledValuesSupplier.labeledValues)
                                labeled_values_recycler_view.adapter = adapter
                            }
                        }
                    } catch (e: Throwable) { println("[debug] Exception in main activity, sensors connection : $e") }

                }

                if (!ServerManagement.serverManager.checkIfConnectionAlreadyExists("sensorsConnection")){
                    ServerManagement.serverManager.addReceiverConnection(dataReceiver1, requireContext(), "sensorsConnection", connectedServerId, ServerManagement.sensors_keyword)
                }

                // getting other needed information
                val dataReceiver2: (String) -> Unit = {data1: String ->
                    context?.let {
                        var json = JsonManager(requireContext(), data1)
                        json = JsonManager(requireContext(), json.findJsonObjectByAttribute("ID", connectedServerId), "JSONObject")  // todo doesnt return correct result
                        val positionsList = json.getAttributeContent("location").split(",")
                        MapManagement.connectedServerPosition = LatLng(positionsList[0].toDouble(), positionsList[1].toDouble())
                    }
                }

                if (!ServerManagement.serverManager.checkIfConnectionAlreadyExists("mapConnection")){
                    ServerManagement.serverManager.addReceiverConnection(dataReceiver2, requireContext(), "mapConnection", connectedServerId, "", "GET_WHOLE_ARRAY")
                }

                val dataReceiver3: (String) -> Unit = { data1: String ->

                    fun updateFileViewsRecyclerView(fragment: Fragment) {
                        try {
                            fragment.context?.let {
                                fragment.homeFragmentInnerFragment?.let {
                                    it.file_views_recycler_view.post {
                                        val layoutManager = LinearLayoutManager(fragment.requireContext())
                                        layoutManager.orientation = LinearLayoutManager.VERTICAL
                                        it.file_views_recycler_view.layoutManager = layoutManager

                                        val adapter = FileViewsAdapter(fragment.requireContext(), FileViewsSupplier.fileViews)
                                        it.file_views_recycler_view.adapter = adapter
                                    }
                                }
                            }
                        } catch (e: Throwable) { println("[debug] e1 that i couldnt fix so try catch Exception: $e") }
                    }

                    try {
                        val json = JsonManager(requireContext(), data1)
                        json.findJsonObjectByAttribute("ID", connectedServerId)

                        json.getAttributeContent("files")

                        for (n in 0 until json.currentJsonAttribute1!!.length()) {
                            val fileInfo = JsonManagerLite(json.getAttributeContentByPath("files/$n"), "JSONObject")
                            val filetype = fileInfo.getAttributeContentByPath("format").split(".")[1]
                            val filename = fileInfo.getAttributeContentByPath("name")
                            val fileDescription = fileInfo.getAttributeContentByPath("description")

                            // handling text
                            if ("txt json".contains(filetype)) {
                                val fileView = FileView(filetype, filename, fileDescription, "$connectedServerId|||||$filename.$filetype")
                                if (!FileViewsSupplier.checkIfContains(fileView)) {
                                    FileViewsSupplier.appendFileView(fileView)
                                    updateFileViewsRecyclerView(this)
                                }
                            }

                            // handling images
                            if ("jpg png".contains(filetype)) {
                                val fileView = FileView(filetype, filename, fileDescription, null, "$connectedServerId|||||$filename.$filetype")
                                if (!FileViewsSupplier.checkIfContains(fileView)) {
                                    FileViewsSupplier.appendFileView(fileView)
                                    updateFileViewsRecyclerView(this)
                                }
                            }

                            // handling pdf files
                            if ("pdf".contains(filetype)) {
                                val fileView = FileView(filetype, filename, fileDescription, null, null, "${ServerManagement.baseUrl}files/$connectedServerId/$filename.$filetype")
                                if (!FileViewsSupplier.checkIfContains(fileView)) {
                                    FileViewsSupplier.appendFileView(fileView)
                                    updateFileViewsRecyclerView(this)
                                }

                            }

                        }
                    } catch (e: Throwable) { println("[debug] Exception in home fragment, files data request : $e") }

                }

                if (!ServerManagement.serverManager.checkIfConnectionAlreadyExists("fileViewsConnection")) {
                    ServerManagement.serverManager.addReceiverConnection(dataReceiver3, requireContext(), "fileViewsConnection", connectedServerId, "", "GET_WHOLE_ARRAY")
                }
            }
        }
    }

    inner class ServerConnector(private val numberOfAttempts: Int=3): Runnable {
        override fun run() {
            for (n in 0 until numberOfAttempts) {
                tryConnectingToServer()
                Thread.sleep(1000)
            }
        }
    }

    private fun loadCache() {}

    private fun saveCache() {}
}

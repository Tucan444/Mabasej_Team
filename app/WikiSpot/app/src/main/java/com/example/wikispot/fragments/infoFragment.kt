package com.example.wikispot.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wikispot.GeneralVariables
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
import com.example.wikispot.showToast
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.fragment_info.*


class infoFragment : Fragment(R.layout.fragment_info) {

    private val args: infoFragmentArgs by navArgs()
    var location: LatLng? = null
    var phoneNumber: Int? = null
    var email: String? = null
    var executeLoadFuntion = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        LabeledValuesSupplier.wipeData()
        FileViewsSupplier.wipeData()
        updateSensorsRecyclerView()
        updateFileViewsRecyclerView()

        try {
            executeLoadFuntion = args.executeLoadFuntion
        } catch (e: Throwable) { println("[debug] Exception in Info Fragment while getting args: $e") }

        if (executeLoadFuntion) {
            load()
        } else {
            getContactInfoFromGeneralVariables()
        }

        locationBtn.setOnClickListener {
            if (executeLoadFuntion) {
                if (location != null) {
                    val action = infoFragmentDirections.infoFragmentToMapFragment(location!!)
                    Navigation.findNavController(it).navigate(action)
                }
            } else {
                Navigation.findNavController(it).navigate(R.id.homeFragment_to_mapFragment)
            }
        }

        phoneBtn.setOnClickListener {
            if (phoneNumber != null) {
                phoneNumber?.let {
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse("tel:$phoneNumber")
                    startActivity(intent)
                }
            } else {
                requireContext().showToast("Phone number not found.")
            }
        }

        emailBtn.setOnClickListener {
            if (email != null) {
                email?.let{
                    val intent = Intent(Intent.ACTION_SEND)

                    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))

                    intent.type = "text/plain"

                    startActivity(Intent.createChooser(intent, "Send Email"))
                }
            } else {
                requireContext().showToast("Email address not found.")
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

                    val phoneNumberLoaded = json.getAttributeContentByPath("description/phone_number")
                    if (phoneNumberLoaded != GeneralVariables.variableMissingKeyword) {
                        phoneNumber = phoneNumberLoaded.toInt()
                        checkContactInformation()
                    }

                    val emailLoaded = json.getAttributeContentByPath("description/email")
                    if (emailLoaded != GeneralVariables.variableMissingKeyword) {
                        email = emailLoaded
                        checkContactInformation()
                    }

                    mainTitle?.let {
                        mainTitle.post {
                            val title = json.getAttributeContentByPath("description/title")
                            if (title != GeneralVariables.variableMissingKeyword) {
                                mainTitle.text = title
                            }
                        }
                    }

                    mainDescription?.let {
                        mainDescription.post {
                            val description = json.getAttributeContentByPath("description/description_l")
                            if (description != GeneralVariables.variableMissingKeyword) {
                                mainDescription.text = description
                            }
                        }
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

                    ServerManagement.serverManager.getImage(imageReceiver1, json.getAttributeContent("ID").toInt(),
                            json.getAttributeContentByPath("description/photo_b"), 2)

                    // getting files

                    json.getAttributeContent("files")

                    for (n in 0 until json.currentJsonAttribute1!!.length()) {
                        val fileInfo = JsonManagerLite(json.getAttributeContentByPath("files/$n"), "JSONObject")
                        val filetype = fileInfo.getAttributeContentByPath("format").split(".")[1]
                        val filename = fileInfo.getAttributeContentByPath("name")
                        val fileDescription = fileInfo.getAttributeContent("description")

                        // handling text
                        if ("txt json".contains(filetype)) {
                            val fileView = FileView(filetype, filename, fileDescription, "$serverId|||||$filename.$filetype")
                            if (!FileViewsSupplier.checkIfContains(fileView)) {
                                FileViewsSupplier.appendFileView(fileView)
                                updateFileViewsRecyclerView()
                            }
                        }

                        // handling images
                        if ("jpg png".contains(filetype)) {
                            val fileView = FileView(filetype, filename, fileDescription, null, "$serverId|||||$filename.$filetype")
                            if (!FileViewsSupplier.checkIfContains(fileView)) {
                                FileViewsSupplier.appendFileView(fileView)
                                updateFileViewsRecyclerView()
                            }
                        }

                        // handling pdf files
                        if ("pdf".contains(filetype)) {
                            val fileView = FileView(filetype, filename, fileDescription, null, null, "${ServerManagement.baseUrl}files/$serverId/$filename.$filetype")
                            if (!FileViewsSupplier.checkIfContains(fileView)) {
                                FileViewsSupplier.appendFileView(fileView)
                                updateFileViewsRecyclerView()
                            }

                        }
                    }
                } catch (e: Throwable) { println("[debug] exception in infoFragment load data request Exception: $e") }
            }
        }

        val sensorsDataReceiver: (String) -> Unit = { data: String ->
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
            ServerManagement.serverManager.getData(dataReceiver, requireContext(), serverId, "", "GET_WHOLE_ARRAY", 3)
            ServerManagement.serverManager.addReceiverConnection(sensorsDataReceiver, requireContext(), "infoFragmentSensorsConnection",
                    serverId, ServerManagement.sensors_keyword)
        }
    }

    private fun getContactInfoFromGeneralVariables(numberOfAttempts: Int = 8) {
        class RetrieveContactInfoFromGeneralVariables(val attemptsCount: Int): Runnable {
            override fun run() {
                for (i in 0 until attemptsCount) {
                    phoneNumber = GeneralVariables.phoneNumber
                    email = GeneralVariables.email

                    if (phoneNumber != null) {
                        checkContactInformation()
                        break
                    }
                    if (email != null) {
                        checkContactInformation()
                        break
                    }
                    Thread.sleep(300)
                }
            }
        }

        val retrieveContactInfoFromGeneralVariables = Thread(RetrieveContactInfoFromGeneralVariables(numberOfAttempts))
        retrieveContactInfoFromGeneralVariables.start()
    }

    private fun checkContactInformation() {
        phoneNumber?.let {
            try {
                phoneBtn.post {
                    phoneBtn.visibility = View.VISIBLE
                }
            } catch (e: Throwable) { println("[debug] Exception in checkContactInformation: $e") }
        }

        email?.let {
            try {
                emailBtn.post {
                    emailBtn.visibility = View.VISIBLE
                }
            } catch (e: Throwable) { println("[debug] Exception in checkContactInformation: $e") }
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
        } catch (e: Throwable) { println("[debug] e3 Exception: $e") }

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
        } catch (e: Throwable) { println("[debug] e2 Exception: $e") }

    }

    fun goExploreFragment() {
        Navigation.findNavController(mainTitle).navigate(R.id.navigateBackToExploreFragment)
    }

    fun goMapFragment() {
        val action = infoFragmentDirections.infoFragmentToMapFragment(LatLng(0.toDouble(), 0.toDouble()), true)
        Navigation.findNavController(mainTitle).navigate(action)
    }

}

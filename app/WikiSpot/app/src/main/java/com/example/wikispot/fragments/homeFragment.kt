package com.example.wikispot.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.wikispot.GeneralVariables
import com.example.wikispot.R
import com.example.wikispot.ServerManagement
import com.example.wikispot.modelClasses.JsonManager
import kotlinx.android.synthetic.main.fragment_home.*
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
        val dataReceiver: (String) -> Unit = {data: String ->
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

        ServerManagement.serverManager.getData(dataReceiver, requireContext(), 0, "", "GET_WHOLE_ARRAY", 4)

    }

    override fun onPause() {
        super.onPause()
        saveCache()
    }

    private fun loadCache() {}

    private fun saveCache() {}
}

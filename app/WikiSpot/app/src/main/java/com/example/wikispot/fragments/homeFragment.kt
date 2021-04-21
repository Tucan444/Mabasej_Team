package com.example.wikispot.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
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

                    homeFragmentInnerFragment.post {
                        homeFragmentInnerFragment?.let { fragment ->
                            fragment.mainTitle.text = json.getAttributeContentByPath("description/title")
                            fragment.mainDescription.text = json.getAttributeContentByPath("description/description_l")
                        }
                    }

                    val imageReceiver: (Bitmap) -> Unit = { bitmap: Bitmap ->
                        homeFragmentInnerFragment?.let {
                            homeFragmentInnerFragment.post {
                                homeFragmentInnerFragment.mainImage.setImageBitmap(bitmap)
                            }
                        }
                    }

                    ServerManagement.serverManager.getImage(imageReceiver, json.getAttributeContent("ID").toInt(),
                            json.getAttributeContentByPath("description/photo_b"), 3)

                }
            } catch (e: Throwable) { println(e) }
        }

        ServerManagement.serverManager.getData(dataReceiver, requireContext(), 0, "", "GET_WHOLE_ARRAY", 3)

    }

    override fun onPause() {
        super.onPause()
        saveCache()
    }

    private fun loadCache() {}

    private fun saveCache() {}
}

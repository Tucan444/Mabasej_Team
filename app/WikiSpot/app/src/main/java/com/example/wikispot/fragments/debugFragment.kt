package com.example.wikispot.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.wikispot.R
import com.example.wikispot.getDataFromServer
import com.example.wikispot.modelClasses.JsonManager
import kotlinx.android.synthetic.main.fragment_debug.*


class debugFragment : Fragment(R.layout.fragment_debug) {

    private lateinit var jsonManager: JsonManager

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getDataBtn.setOnClickListener {
            context?.let {
                jsonManager = JsonManager(requireContext(), requireContext().getDataFromServer(), "JSONArray", true)
                sizeView.text = jsonManager.getLengthOfJsonArray().toString()
            }
        }

        getJsonFileBtn.setOnClickListener {
            val id = idInput.text.toString().toInt()
            jsonFileContentView.text = jsonManager.getJsonObject(id).toString()
        }

        getAttributeContentBtn.setOnClickListener {
            val attributeName = attributeNameInput.text.toString()
            if ("/" in attributeName) {
                attributeContentView.text = jsonManager.getAttributeContentByPath(attributeNameInput.text.toString())
            } else {
                attributeContentView.text = jsonManager.getAttributeContent(attributeNameInput.text.toString())
            }
        }

        clearAttributeBtn.setOnClickListener {
            jsonManager.clearSelectedAttribute()
            attributeContentView.text = "attribute content"
        }

        attributeNameInput.setOnClickListener {
            attributeContentView.text = jsonManager.getCurrentJsonAttributeContent()
        }

        // handling navigation between debug fragments
        goSecondDebugFragmentBtn.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.navigateToAnotherDebugFragment)
        }
    }
}
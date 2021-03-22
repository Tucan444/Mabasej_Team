package com.example.wikispot.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.wikispot.R
import com.example.wikispot.ServerManagement
import com.example.wikispot.modelClasses.JsonManager
import kotlinx.android.synthetic.main.fragment_another_debug.*
import org.json.JSONObject
import kotlin.random.Random


class anotherDebugFragment : Fragment(R.layout.fragment_another_debug) {

    private lateinit var jsonManager: JsonManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        jsonManager = JsonManager(requireContext(), "[]", "JSONArray", true)

        generateAndSaveDataBtn.setOnClickListener {
            val generatedData = JSONObject()

            for (n in 0 until 10) {
                generatedData.put("$n", Random.nextInt(0, 100))
            }
            context?.let {
                jsonManager = JsonManager(requireContext(), generatedData.toString(), "JSONObject", true)
                jsonManager.saveJson("test")
            }
            println("[debug] saved generated data")
        }

        loadAndShowDataBtn.setOnClickListener {
            val json = jsonManager.loadJson(requireContext(), "test", "JSONObject")
            dataContentView.text = json.currentJsonObject.toString()
        }

        stopConnectionBtn.setOnClickListener {
            ServerManagement.serverManager.deleteConnection("debug", "view")
        }

        createViewConnectionBtn.setOnClickListener {
            val attributePath = attributePathInput.text.toString()
            val setWholeContent = wholeContentCheckBox.isChecked
            val filePath = filePathInput.text.toString()

            ServerManagement.serverManager.deleteConnection("debug", "view")
            ServerManagement.serverManager.addViewConnection(requireContext(), dataContentView, "debug",0, filePath, attributePath, setWholeContent)
        }

        // handling navigation between debug fragments
        goFirstDegubFragmentBtn.setOnClickListener {
            cleanup()
            Navigation.findNavController(it).navigate(R.id.navigateBackToDebugFragment)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanup()
    }

    private fun cleanup() {
        ServerManagement.serverManager.deleteConnection("debug")
    }

}
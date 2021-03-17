package com.example.wikispot.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.wikispot.R
import com.example.wikispot.getDataFromServer
import com.example.wikispot.showSnack
import kotlinx.android.synthetic.main.fragment_debug.*
import org.json.JSONObject


class debugFragment : Fragment(R.layout.fragment_debug) {

    private var jsonList: MutableList<JSONObject> = mutableListOf<JSONObject>()

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getDataBtn.setOnClickListener {
            context?.let {
                jsonList = requireContext().getDataFromServer()
                sizeView.text = "Amount of json's: ${jsonList.size}"
            }
        }

        displayJsonFileBtn.setOnClickListener {
            val id = idInput.text.toString().toInt()
            if (id >= jsonList.size) {
                context?.let {
                    requireContext().showSnack("Id out of range.", displayJsonFileBtn)
                }
            } else {
                jsonFileOutputView.text = jsonList[id].toString()
            }
        }
    }
}
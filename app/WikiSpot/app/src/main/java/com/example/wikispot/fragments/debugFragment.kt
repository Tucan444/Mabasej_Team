package com.example.wikispot.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.wikispot.*
import com.example.wikispot.activities.MainActivity
import com.example.wikispot.modelsForAdapters.MessagesSupplier
import kotlinx.android.synthetic.main.fragment_debug.*


class debugFragment : Fragment(R.layout.fragment_debug) {

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        goSecondDebugFragmentBtn.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.navigateToAnotherDebugFragment)
        }

        getNumberOfSentRequestsBtn.setOnClickListener {
            outputText.text = ServerManagement.totalNumberOfRequestsSent.toString()
        }

        clearServerConnectionsBtn.setOnClickListener {
            ServerManagement.serverManager.clearConnections()
        }

        editTextIp.setText(ServerManagement.baseUrl)

        changeUrlBtn.setOnClickListener {
            ServerManagement.baseUrl = editTextIp.text.toString()
            requireContext().saveString("baseUrlSave", editTextIp.text.toString())
            restartAppPartially()
        }

        restartAppPartiallyBtn.setOnClickListener {
            restartAppPartially()
        }

        closeAppBtn.setOnClickListener {
            activity?.finish()
        }

    }

    private fun restartAppPartially() {
        val intent = Intent(context?.applicationContext, MainActivity::class.java)

        intent.putExtra(IntentsKeys.startFragment, "debugFragment")

        ServerManagement.serverManager.clearConnections()
        MessagesSupplier.wipeData()
        GeneralVariables.id = null

        startActivity(intent)
        activity?.finish()
    }
}
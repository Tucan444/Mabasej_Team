package com.example.wikispot.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.wikispot.R
import com.example.wikispot.ServerManagement
import com.example.wikispot.modelClasses.JsonManager
import kotlinx.android.synthetic.main.fragment_debug.*


class debugFragment : Fragment(R.layout.fragment_debug) {

    private lateinit var jsonManager: JsonManager

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        goSecondDebugFragmentBtn.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.navigateToAnotherDebugFragment)
        }

        getNumberOfSentRequestsBtn.setOnClickListener {
            outputText.text = ServerManagement.totalNumberOfRequestsSent.toString()
        }

        val pdfRequestThread = Thread(PdfRequest())
        pdfRequestThread.start()



    }

    inner class PdfRequest : Runnable {

        override fun run() {
            val inputStream = java.net.URL("${ServerManagement.baseUrl}files/1/sample.pdf").openStream()

            Thread.sleep(500)

            pdfContent.post {
                pdfContent.fromStream(inputStream).load()
                pdfContent.zoomTo(pdfContent.width / 490.0F)
                println("[debug] zoom is ${pdfContent.width / 490.0F}")
                println(pdfContent.width)
                Thread.sleep(1000)
                println(pdfContent.currentPage)
            }

            ServerManagement.totalNumberOfRequestsSent += 1
        }
    }
}
package com.example.wikispot.fragments

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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
            val uri = Uri.parse("${ServerManagement.baseUrl}files/1/sample.pdf")

            Thread.sleep(500)

            pdfView.post {
                println("asdfsdfsdfs")
                //pdfView.fromUri(uri).load()
                pdfView.fromStream(inputStream).pages(0).load()
                pdfView.zoomTo(pdfView.width / 490.0F)
                println("[debug] zoom is ${pdfView.width / 490.0F}")
                println(pdfView.width)
                Thread.sleep(1000)
                println(pdfView.currentPage)
            }

            ServerManagement.totalNumberOfRequestsSent += 1
        }
    }
}
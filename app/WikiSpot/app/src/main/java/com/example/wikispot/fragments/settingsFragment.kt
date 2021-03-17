package com.example.wikispot.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import com.example.wikispot.R
import kotlinx.android.synthetic.main.fragment_settings.*


class settingsFragment : Fragment(R.layout.fragment_settings) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        debugBtn.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.navigateToDebugFragment)
        }
    }

}
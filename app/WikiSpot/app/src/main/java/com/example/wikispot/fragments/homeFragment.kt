package com.example.wikispot.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.wikispot.R
import com.example.wikispot.getStringFromSharedPreferences
import com.example.wikispot.saveString
import kotlinx.android.synthetic.main.fragment_home.*


class homeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadCache()
    }

    override fun onPause() {
        super.onPause()
        saveCache()
    }

    private fun loadCache() {
        homeFragmentTextIdTest.text = requireContext().getStringFromSharedPreferences("title", "homeFragmentCache" )
    }

    private fun saveCache() {
        requireContext().saveString("title", homeFragmentTextIdTest.text.toString(), "homeFragmentCache")
    }
}
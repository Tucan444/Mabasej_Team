package com.example.wikispot.fragments

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wikispot.R
import com.example.wikispot.models_and_adapters.PlacePreviewsAdapter
import com.example.wikispot.models_and_adapters.PlaceSupplier
import kotlinx.android.synthetic.main.fragment_explore.*


class exploreFragment : Fragment(R.layout.fragment_explore) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {

        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        explore_recycler_view.layoutManager = layoutManager

        val adapter = context?.let { PlacePreviewsAdapter(it, PlaceSupplier.places) }
        explore_recycler_view.adapter = adapter
    }

}
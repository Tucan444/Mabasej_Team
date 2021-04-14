package com.example.wikispot.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wikispot.R
import com.example.wikispot.adapters.ChatMessagesAdapter
import com.example.wikispot.adapters.FileViewsAdapter
import com.example.wikispot.modelsForAdapters.MessagesSupplier
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.fragment_info.*


class chatFragment : Fragment(R.layout.fragment_chat) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateRecyclerView()
    }

    private fun updateRecyclerView() {

        chat_messages_recycler_view.post {
            val layoutManager = LinearLayoutManager(context)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            chat_messages_recycler_view.layoutManager = layoutManager

            val adapter = context?.let { ChatMessagesAdapter(it, MessagesSupplier.messages) }
            chat_messages_recycler_view.adapter = adapter
        }

    }

}
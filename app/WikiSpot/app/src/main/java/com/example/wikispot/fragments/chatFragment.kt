package com.example.wikispot.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wikispot.R
import com.example.wikispot.ServerManagement
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

    override fun onResume() {
        super.onResume()

        val dataReceiver: (String) -> Unit = {data: String ->
            println("[debug][chat connection] data: $data")
        }

        ServerManagement.serverManager.addReceiverConnection(dataReceiver, requireContext(), "chatConnection", 0, ServerManagement.chat_keyword)
    }

    override fun onPause() {
        super.onPause()
        ServerManagement.serverManager.deleteConnection("chatConnection")
    }

    private fun updateRecyclerView() {

        try {
            chat_messages_recycler_view.post {
                val layoutManager = LinearLayoutManager(context)
                layoutManager.orientation = LinearLayoutManager.VERTICAL
                chat_messages_recycler_view.layoutManager = layoutManager

                val adapter = context?.let { ChatMessagesAdapter(it, MessagesSupplier.messages) }
                chat_messages_recycler_view.adapter = adapter
            }
        } catch (e: Throwable) { println("[debug] e5 Exception: $e") }

    }

}
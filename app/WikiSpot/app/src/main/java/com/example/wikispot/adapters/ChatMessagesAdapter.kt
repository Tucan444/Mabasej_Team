package com.example.wikispot.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wikispot.ChatManagement
import com.example.wikispot.GeneralVariables
import com.example.wikispot.R
import com.example.wikispot.modelsForAdapters.Message
import kotlinx.android.synthetic.main.message.view.*


class ChatMessagesAdapter(private val context: Context, private val messages: Array<Message?>) : RecyclerView.Adapter<ChatMessagesAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        var message: Message? = null
        var pos: Int = 0

        fun setData(message: Message?, pos: Int) {
            message?.let {
                itemView.message_author_text.text = message.senderName
                itemView.message_content_text.text = message.content

                if (GeneralVariables.id == message.senderId) {
                    itemView.message_author_text.textAlignment = View.TEXT_ALIGNMENT_TEXT_END
                    itemView.message_content_text.textAlignment = View.TEXT_ALIGNMENT_TEXT_END
                }
                for (n in 0 until ChatManagement.lastNames.length()) {
                    if (ChatManagement.lastNames[n] == message.senderId) {
                        itemView.message_author_text.textAlignment = View.TEXT_ALIGNMENT_TEXT_END
                        itemView.message_content_text.textAlignment = View.TEXT_ALIGNMENT_TEXT_END
                        itemView.message_author_text.text = GeneralVariables.name
                    }
                }
            }

            this.message = message
            this.pos = pos
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val message = messages[position]
        holder.setData(message, position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.message, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return messages.size
    }
}

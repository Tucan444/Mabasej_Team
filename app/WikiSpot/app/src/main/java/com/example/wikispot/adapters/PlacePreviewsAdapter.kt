package com.example.wikispot.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wikispot.R
import com.example.wikispot.modelsForAdapters.PlacePreview
import kotlinx.android.synthetic.main.explore_list_item.view.*


class PlacePreviewsAdapter(private val context: Context, private val placePreviews: Array<PlacePreview?>) : RecyclerView.Adapter<PlacePreviewsAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        var currentPlacePreview: PlacePreview? = null
        var pos: Int = 0

        init {
        }

        fun setData(placePreview: PlacePreview?, pos: Int) {
            placePreview?.let {
                itemView.item_title.text = placePreview.title
                itemView.item_description.text = placePreview.description
                placePreview.img?.let {
                    // TODO set image somehow
                }
            }

            this.currentPlacePreview = placePreview
            this.pos = pos
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val placePreview = placePreviews[position]
        holder.setData(placePreview, position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.explore_list_item, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return placePreviews.size
    }
}

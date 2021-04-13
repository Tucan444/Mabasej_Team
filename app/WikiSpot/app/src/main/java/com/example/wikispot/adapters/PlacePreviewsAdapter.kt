package com.example.wikispot.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.example.wikispot.R
import com.example.wikispot.ServerManagement
import com.example.wikispot.fragments.exploreFragmentDirections
import com.example.wikispot.modelsForAdapters.PlacePreview
import com.example.wikispot.showToast
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.explore_list_item.view.*


class PlacePreviewsAdapter(private val context: Context, private val placePreviews: Array<PlacePreview?>) : RecyclerView.Adapter<PlacePreviewsAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        var currentPlacePreview: PlacePreview? = null
        var pos: Int = 0
        var location: LatLng? = null

        init {
            itemView.setOnClickListener {
                ServerManagement.selectedServerId = currentPlacePreview?.id!!
                val action = exploreFragmentDirections.navigateToInfoFragment(true)
                Navigation.findNavController(it).navigate(action)
            }

            itemView.item_location_img.setOnClickListener {
                if (location != null) {
                    val action = exploreFragmentDirections.navigateToMapFragment(location!!, currentPlacePreview!!.title)
                    Navigation.findNavController(it).navigate(action)
                }
            }
        }

        fun setData(placePreview: PlacePreview?, pos: Int) {
            placePreview?.let {
                itemView.item_title.text = placePreview.title
                itemView.item_description.text = placePreview.description

                try {
                    val coordinates = placePreview.location!!.split(",")
                    location = LatLng(coordinates[0].toDouble(), coordinates[1].toDouble())
                } catch (e: Throwable) {
                    println("[debug] Failed getting coordinates in ${placePreview.title}, explore fragment list.  Exception: $e")}


                placePreview.img?.let {
                    itemView.item_img.setImageBitmap(placePreview.img)
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

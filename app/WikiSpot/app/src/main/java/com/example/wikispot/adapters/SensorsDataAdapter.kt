package com.example.wikispot.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wikispot.R
import com.example.wikispot.modelsForAdapters.LabeledValue
import kotlinx.android.synthetic.main.labeled_value_item.view.*


class LabeledValuesAdapter(private val context: Context, private val labeledValues: Array<LabeledValue?>) : RecyclerView.Adapter<LabeledValuesAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        var currentLabeledValue: LabeledValue? = null
        var pos: Int = 0

        fun setData(labeledValue: LabeledValue?, pos: Int) {
            labeledValue?.let {
                itemView.label.text = labeledValue.label
                itemView.value.text = labeledValue.value
            }

            this.currentLabeledValue = labeledValue
            this.pos = pos
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val labeledValue = labeledValues[position]
        holder.setData(labeledValue, position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.labeled_value_item, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return labeledValues.size
    }
}

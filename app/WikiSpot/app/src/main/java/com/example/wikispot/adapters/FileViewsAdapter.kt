package com.example.wikispot.adapters

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.example.wikispot.R
import com.example.wikispot.ServerManagement
import com.example.wikispot.modelsForAdapters.FileView
import kotlinx.android.synthetic.main.file_view.view.*


class FileViewsAdapter(private val context: Context, private val fileViews: Array<FileView?>) : RecyclerView.Adapter<FileViewsAdapter.MyViewHolder>() {

    private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.open_rotation_anim) }
    private val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(context, R.anim.close_rotation_anim) }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        var fileView: FileView? = null
        var pos: Int = 0
        var textInfo: String? = null
        var imgInfo: String? = null
        var pdfUrl: String? = null
        var opened = false

        init {
            itemView.setOnClickListener {
                if (!opened) {
                    itemView.showFileBtn.startAnimation(rotateOpen)

                    fileView?.let {
                        textInfo?.let {
                            itemView.textContent.textSize = 18F
                            val dataReceiver: (String) -> Unit = { data: String ->
                                itemView.textContent.post {
                                    itemView.textContent.text = data
                                }
                            }

                            val textInformation = textInfo!!.split("|||||")

                            ServerManagement.serverManager.getData(dataReceiver, itemView.context, textInformation[0].toInt(), textInformation[1])
                        }
                        imgInfo?.let {
                            itemView.imageContent.visibility = View.VISIBLE
                            val imageReceiver2: (Bitmap) -> Unit = { bitmap: Bitmap ->
                                itemView.imageContent.post {
                                    itemView.imageContent.setImageBitmap(bitmap)
                                }
                            }

                            val imgInformation = imgInfo!!.split("|||||")

                            ServerManagement.serverManager.getImage(imageReceiver2, imgInformation[0].toInt(), imgInformation[1])
                        }
                        pdfUrl?.let {
                            itemView.pdfContent.visibility = View.VISIBLE
                            ServerManagement.serverManager.loadPdfView(itemView.pdfContent, pdfUrl!!)
                        }
                    }
                } else {
                    itemView.showFileBtn.startAnimation(rotateClose)
                    itemView.textContent.textSize = 0F
                    itemView.imageContent.visibility = View.GONE
                    itemView.pdfContent.visibility = View.GONE
                }

                opened = !opened
            }
        }

        fun setData(fileView: FileView?, pos: Int) {
            fileView?.let {
                fileView.textInfo?.let {
                    textInfo = it
                }
                fileView.imgInfo?.let {
                    imgInfo = it
                }
                fileView.pdfUrl?.let {
                    pdfUrl = it
                }

                itemView.filename_text.text = fileView.filename
            }

            this.fileView = fileView
            this.pos = pos
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val fileView = fileViews[position]
        holder.setData(fileView, position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.file_view, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return fileViews.size
    }
}

package com.viceboy.shazamlayoutmanager

import android.content.res.ColorStateList
import android.graphics.Color
import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_layout.view.*

class DataAdapter(private val listOfItem : ArrayList<DataModel>) : RecyclerView.Adapter<DataAdapter.DataViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder = DataViewHolder(parent)

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.bind(listOfItem[position])
    }

    override fun getItemCount(): Int = listOfItem.size

    class DataViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_layout,parent,false)
    ) {
        fun bind(dataModel: DataModel) {
            itemView.ivImgCard.setImageResource(dataModel.image)
            itemView.rootItem.setBackgroundColor(Utils.darkenColor(ContextCompat.getColor(itemView.context,dataModel.colorTint),0.4f))
            itemView.fabPlay.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(itemView.context,dataModel.colorTint))
        }
    }

}

data class DataModel (
    val image : Int,
    val colorTint : Int
)

object Utils {

    @ColorInt
    fun darkenColor(@ColorInt color: Int,darkPercent : Float): Int {
        return Color.HSVToColor(FloatArray(3).apply {
            Color.colorToHSV(color, this)
            this[2] *= darkPercent
        })
    }
}
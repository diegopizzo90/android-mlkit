package com.diegopizzo.androidmlkit.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.diegopizzo.androidmlkit.databinding.ItemMainAdapterBinding
import com.diegopizzo.androidmlkit.view.ItemFeature.*

class MainAdapter(private val itemList: List<ItemFeatureData>, private val listener: OnViewAdapterInteraction) :
    RecyclerView.Adapter<MainAdapter.MainViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainViewHolder {
        val binding =
            ItemMainAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MainViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MainViewHolder, position: Int) {
        holder.setItem(itemList[position])
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class MainViewHolder(private val itemViewBinding: ItemMainAdapterBinding) :
        RecyclerView.ViewHolder(itemViewBinding.root) {
            fun setItem(item: ItemFeatureData) {
                itemViewBinding.itemFeature.setItemFeatureData(item)
                itemViewBinding.itemFeature.setOnClickListener {
                    listener.onItemClick(item)
                }
            }
        }

    interface OnViewAdapterInteraction {
        fun onItemClick(item: ItemFeatureData)
    }
}
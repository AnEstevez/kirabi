package com.andresestevez.kirabi.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.andresestevez.kirabi.data.models.Media

abstract class BaseMediaAdapter(
    private val layoutId: Int,
) : ListAdapter<Media, BaseMediaAdapter.ViewHolder>(DiffCallback) {

    var onMediaItemClickListener: ((Media) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return ViewHolder(view)
    }

    fun setOnItemClickListener(action: (Media) -> Unit) {
        onMediaItemClickListener = action
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private object DiffCallback : DiffUtil.ItemCallback<Media>() {
        override fun areItemsTheSame(oldItem: Media, newItem: Media): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Media, newItem: Media): Boolean =
            oldItem == newItem
    }
}


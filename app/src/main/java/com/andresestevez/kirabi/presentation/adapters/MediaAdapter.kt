package com.andresestevez.kirabi.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.andresestevez.kirabi.R
import com.andresestevez.kirabi.data.models.Media
import com.andresestevez.kirabi.databinding.MediaItemBinding
import com.bumptech.glide.RequestManager
import javax.inject.Inject

class MediaAdapter @Inject constructor(
    private val glide: RequestManager,
) : ListAdapter<Media, MediaAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.media_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private var onItemClickListener: ((Media) -> Unit)? = null

    fun setOnItemClickListener(action: (Media) -> Unit) {
        onItemClickListener = action
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = MediaItemBinding.bind(itemView)

        fun bind(item: Media) {
            with(binding) {
                glide.load(item.imageUrl).into(ivItemImage)
                tvPrimary.text = item.title
                tvSecondary.text = item.artist
            }
            itemView.setOnClickListener {
                onItemClickListener?.let { click ->
                    click(item)
                }
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Media>() {
        override fun areItemsTheSame(oldItem: Media, newItem: Media): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Media, newItem: Media): Boolean =
            oldItem == newItem
    }
}


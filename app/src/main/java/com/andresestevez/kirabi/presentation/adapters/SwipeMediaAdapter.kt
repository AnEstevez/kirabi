package com.andresestevez.kirabi.presentation.adapters

import com.andresestevez.kirabi.R
import com.andresestevez.kirabi.databinding.SwipeMediaItemBinding

class SwipeMediaAdapter : BaseMediaAdapter(R.layout.swipe_media_item) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        SwipeMediaItemBinding.bind(holder.itemView).apply {
            tvTitle.text = item.title
            tvArtist.text = item.artist
        }

        holder.itemView.setOnClickListener {
            onMediaItemClickListener?.let { click ->
                click(item)
            }
        }
    }
}


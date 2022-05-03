package com.andresestevez.kirabi.presentation.adapters

import com.andresestevez.kirabi.R
import com.andresestevez.kirabi.databinding.MediaItemBinding
import com.bumptech.glide.RequestManager
import javax.inject.Inject

class MediaAdapter @Inject constructor(
    private val glide: RequestManager,
) : BaseMediaAdapter(R.layout.media_item) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        MediaItemBinding.bind(holder.itemView).apply {
            glide.load(item.imageUrl).into(ivItemImage)
            tvPrimary.text = item.title
            tvSecondary.text = item.artist
        }

        holder.itemView.setOnClickListener {
            onMediaItemClickListener?.let { click ->
                click(item)
            }
        }
    }
}


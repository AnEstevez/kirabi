package com.andresestevez.kirabi.presentation

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.andresestevez.kirabi.R
import com.andresestevez.kirabi.data.Status
import com.andresestevez.kirabi.data.models.Media
import com.andresestevez.kirabi.databinding.ActivityMainBinding
import com.andresestevez.kirabi.exoplayer.isPlaying
import com.andresestevez.kirabi.exoplayer.toMedia
import com.andresestevez.kirabi.presentation.adapters.SwipeMediaAdapter
import com.andresestevez.kirabi.presentation.viewmodels.MainViewModel
import com.bumptech.glide.RequestManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val vm: MainViewModel by viewModels()

    private var curPlayingMedia: Media? = null

    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = _binding!!

    private var playbackState: PlaybackStateCompat? = null

    @Inject
    lateinit var adapter: SwipeMediaAdapter

    @Inject
    lateinit var glide: RequestManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.vpSong.adapter = adapter

        binding.vpSong.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (playbackState?.isPlaying == true) {
                    vm.playOrToggleMedia(adapter.currentList[position])
                } else {
                    curPlayingMedia = adapter.currentList[position]
                    glide.load(adapter.currentList[position].imageUrl).into(binding.ivCurSongImage)
                }
            }
        })

        subscribeToObservers()

        binding.ivPlayPause.setOnClickListener {
            curPlayingMedia?.let {
                vm.playOrToggleMedia(it, true)
            }
        }

    }

    private fun switchViewPagerToCurrentMedia(media: Media) {
        val newItemIndex = adapter.currentList.indexOf(media)
        if (newItemIndex != -1) {
            binding.vpSong.currentItem = newItemIndex
            curPlayingMedia = media
        }
    }

    private fun subscribeToObservers() {
        vm.mediaItems.observe(this) {
            it?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { medias ->
                            adapter.submitList(medias)
                            if (medias.isNotEmpty()) {
                                glide.load((curPlayingMedia ?: medias[0]).imageUrl)
                                    .into(binding.ivCurSongImage)
                            }
                            switchViewPagerToCurrentMedia(curPlayingMedia ?: return@observe)
                        }
                    }
                    Status.ERROR -> Unit
                    Status.LOADING -> Unit
                }
            }
        }

        vm.curlPlayingMedia.observe(this) {
            if (it == null) return@observe
            curPlayingMedia = it.toMedia()
            glide.load(curPlayingMedia?.imageUrl).into(binding.ivCurSongImage)
            switchViewPagerToCurrentMedia(curPlayingMedia ?: return@observe)
        }

        vm.playbackState.observe(this) {
            playbackState = it
            binding.ivPlayPause.setImageResource(
                if (playbackState?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play
            )
        }

        vm.isConnected.observe(this) {
            it?.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.ERROR -> Snackbar.make(
                        binding.rootLayout,
                        result.message ?: "An unknown error occurred",
                        Snackbar.LENGTH_LONG
                    ).show()
                    else -> Unit
                }
            }
        }

        vm.networkError.observe(this) {
            it?.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.ERROR -> Snackbar.make(
                        binding.rootLayout,
                        result.message ?: "An unknown error occurred",
                        Snackbar.LENGTH_LONG
                    ).show()
                    else -> Unit
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
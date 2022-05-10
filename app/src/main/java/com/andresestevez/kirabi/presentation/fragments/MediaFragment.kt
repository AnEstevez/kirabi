package com.andresestevez.kirabi.presentation.fragments

import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.andresestevez.kirabi.R
import com.andresestevez.kirabi.data.Status
import com.andresestevez.kirabi.data.models.Media
import com.andresestevez.kirabi.databinding.FragmentMediaBinding
import com.andresestevez.kirabi.exoplayer.isPlaying
import com.andresestevez.kirabi.exoplayer.toMedia
import com.andresestevez.kirabi.presentation.viewmodels.MainViewModel
import com.andresestevez.kirabi.presentation.viewmodels.MediaViewModel
import com.bumptech.glide.RequestManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MediaFragment : Fragment() {

    @Inject
    lateinit var glide: RequestManager

    private val mainViewModel: MainViewModel by activityViewModels()
    private val mediaViewModel: MediaViewModel by viewModels()

    private val navArgs: MediaFragmentArgs by navArgs()

    private var curPlayingMedia: Media? = null

    private var playbackState: PlaybackStateCompat? = null

    private var shouldUpdateSeekbar = true

    private var _binding: FragmentMediaBinding? = null
    private val binding: FragmentMediaBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMediaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()

        with(binding) {
            ivPlayPauseDetail.setOnClickListener {
                curPlayingMedia?.let {
                    mainViewModel.playOrToggleMedia(it, true)
                }
            }

            ivSkipPrevious.setOnClickListener {
                mainViewModel.skipToPreviousMedia()
            }

            ivSkip.setOnClickListener {
                mainViewModel.skipToNextMedia()
            }

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean,
                ) {
                    if (fromUser) {
                        setCurPlayerTimeToTextView(progress.toLong())
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    shouldUpdateSeekbar = false
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    seekBar?.let {
                        mainViewModel.seekTo(it.progress.toLong())
                        shouldUpdateSeekbar = true
                    }
                }

            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateTitleAndImage(media: Media) {
        val title = "${media.title} - ${media.artist}"
        binding.tvMediaName.text = title
        glide.load(media.imageUrl).into(binding.ivMediaImage)
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner) {
            it?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { medias ->
                            if (curPlayingMedia == null && medias.isNotEmpty()) {
                                val curMedia = medias.first { media -> media.id == navArgs.mediaId }
                                curPlayingMedia = curMedia
                                updateTitleAndImage(curMedia)
                            }
                        }
                    }
                    else -> Unit
                }
            }
        }

        mainViewModel.curPlayingMedia.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            curPlayingMedia = it.toMedia().also { media -> updateTitleAndImage(media) }
        }

        mainViewModel.playbackState.observe(viewLifecycleOwner) {
            playbackState = it
            with(binding) {
                ivPlayPauseDetail.setImageResource(
                    if (playbackState?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play
                )
                seekBar.progress = it?.position?.toInt() ?: 0
            }
        }

        mediaViewModel.curPlayerPosition.observe(viewLifecycleOwner) {
            if (shouldUpdateSeekbar) {
                binding.seekBar.progress = it.toInt()
                setCurPlayerTimeToTextView(it)
            }
        }

        mediaViewModel.curMediaDuration.observe(viewLifecycleOwner) {
            with(binding) {
                seekBar.max = it.toInt()
                val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
                tvMediaDuration.text = dateFormat.format(it)
            }
        }
    }

    private fun setCurPlayerTimeToTextView(ms: Long) {
        val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        binding.tvCurTime.text = dateFormat.format(ms)
    }


}
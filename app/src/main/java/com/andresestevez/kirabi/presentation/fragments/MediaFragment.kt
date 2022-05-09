package com.andresestevez.kirabi.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.andresestevez.kirabi.R
import com.andresestevez.kirabi.data.Status
import com.andresestevez.kirabi.data.models.Media
import com.andresestevez.kirabi.databinding.FragmentMediaBinding
import com.andresestevez.kirabi.exoplayer.toMedia
import com.andresestevez.kirabi.presentation.viewmodels.MainViewModel
import com.andresestevez.kirabi.presentation.viewmodels.MediaViewModel
import com.bumptech.glide.RequestManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MediaFragment : Fragment(R.layout.fragment_media) {

    @Inject
    lateinit var glide: RequestManager

    private val mainViewModel : MainViewModel by activityViewModels()
    private val mediaViewModel: MediaViewModel by viewModels()

    private var curPlayingMedia: Media? = null

    private var _binding : FragmentMediaBinding? = null
    val binding : FragmentMediaBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMediaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()
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
                            if(curPlayingMedia == null && medias.isNotEmpty()) {
                                curPlayingMedia = medias[0]
                                updateTitleAndImage(medias[0])
                            }
                        }
                    }
                    else -> Unit
                }
            }
        }

        mainViewModel.curlPlayingMedia.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            curPlayingMedia = it.toMedia().also { media ->  updateTitleAndImage(media) }
        }

    }
}
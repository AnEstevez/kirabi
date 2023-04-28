package com.andresestevez.kirabi.presentation.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.palette.graphics.Palette
import com.andresestevez.kirabi.R
import com.andresestevez.kirabi.data.resolveUriAsBitmap
import com.andresestevez.kirabi.databinding.FragmentMediaBinding
import com.andresestevez.kirabi.exoplayer.extensions.displayIconUri
import com.andresestevez.kirabi.exoplayer.extensions.subtitle
import com.andresestevez.kirabi.exoplayer.extensions.title
import com.andresestevez.kirabi.presentation.extensions.createGradientDrawable
import com.andresestevez.kirabi.presentation.extensions.startBackgroundDrawableTransition
import com.andresestevez.kirabi.presentation.viewmodels.MediaViewModel
import com.bumptech.glide.RequestManager
import com.google.android.exoplayer2.ExoPlayer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MediaFragment : Fragment() {

    @Inject
    lateinit var glide: RequestManager

    @Inject
    lateinit var exoPlayer: ExoPlayer

    private val mediaViewModel: MediaViewModel by viewModels()

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
        binding.playerControls.player = exoPlayer

        mediaViewModel.mediaMetadata.observe(viewLifecycleOwner) {
            updateUI(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.playerControls.player = null
        _binding = null
    }

    private fun updateUI(it: MediaMetadataCompat) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val bitmap = it.displayIconUri.resolveUriAsBitmap(glide)
                setBackgroundDynamicColors(bitmap)
            }
        }
        binding.exoTitle.text = it.title
        binding.exoSubtitle.text = it.subtitle
        glide.load(it.displayIconUri).into(binding.exoArtwork)
    }

    private suspend fun setBackgroundDynamicColors(bitmap: Bitmap?) = withContext(Dispatchers.IO) {
        bitmap?.let {
            val palette: Palette = Palette.from(bitmap).generate()

            val vibrantColor = palette.vibrantSwatch?.rgb ?: ContextCompat.getColor(
                requireContext(),
                R.color.darkBackground)

            val lightVibrantColor = palette.lightVibrantSwatch?.rgb ?: ContextCompat.getColor(
                requireContext(),
                R.color.darkBackground)

            val gradient = binding.root.createGradientDrawable(vibrantColor, lightVibrantColor)

            binding.root.startBackgroundDrawableTransition(gradient)
        }
    }

}
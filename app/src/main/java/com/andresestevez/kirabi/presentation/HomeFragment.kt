package com.andresestevez.kirabi.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.SimpleItemAnimator
import com.andresestevez.kirabi.R
import com.andresestevez.kirabi.data.Status
import com.andresestevez.kirabi.databinding.FragmentHomeBinding
import com.andresestevez.kirabi.presentation.adapters.MediaAdapter
import com.andresestevez.kirabi.presentation.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val vm: MainViewModel by activityViewModels()

    @Inject
    lateinit var mediaAdapter: MediaAdapter

    private var _binding: FragmentHomeBinding? = null
    private val binding: FragmentHomeBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycler()
        subscribeToObservers()
    }

    private fun setupRecycler() {
        mediaAdapter.setOnItemClickListener {
            vm.playOrToggleMedia(it)
        }
        binding.rvAllMedia.adapter = mediaAdapter
        (binding.rvAllMedia.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
    }

    private fun subscribeToObservers() {
        vm.mediaItems.observe(viewLifecycleOwner) { result ->
            when (result.status) {
                Status.SUCCESS -> {
                    binding.allMediaProgressBar.isVisible = false
                    mediaAdapter.submitList(result.data)
                }
                Status.ERROR -> {}
                Status.LOADING -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
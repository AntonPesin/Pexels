package com.projects.pexels_app.ui.fragments

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.projects.pexels_app.ui.navigation.Navigation
import com.projects.pexels_app.R
import com.projects.pexels_app.data.network.models.MediaModel
import com.projects.pexels_app.databinding.BookmarksBinding
import com.projects.pexels_app.ui.adapters.BookmarkAdapter
import com.projects.pexels_app.ui.viewmodels.BookmarksViewModel
import com.projects.pexels_app.utils.Keys
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class Bookmarks : Fragment() {
    private var _binding: BookmarksBinding? = null
    private val binding get() = _binding!!
    private lateinit var navigationHandler: Navigation
    private val viewModel: BookmarksViewModel by viewModels()
    private val adapter = BookmarkAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = BookmarksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navigationHandler = Navigation(findNavController())

        setupNavBindings()

        lifecycleScope.launch {
            showProgressBar()
            delay(1000)
            checkData()
            loadBookmarksPhoto()
            hideProgressBar()
        }

        binding.navigationBar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home_menu -> {
                    navigationHandler.navigateBookmarksToHome()
                }
            }
            true
        }

        adapter.setOnImageClickListener(
            object : BookmarkAdapter.PhotoClickListener {
                override fun onPhotoClick(photo: MediaModel?) {
                    navigationHandler.navigateBookmarksToDetails(
                        photo?.id,
                        Keys.BOOKMARK.name
                    )
                }
            })


        binding.bookmarksExplore.setOnClickListener {
            navigationHandler.navigateBookmarksToHome()
        }
    }

    private fun setupNavBindings(){
        binding.navigationBar.itemIconTintList = null
        binding.navigationBar.menu.findItem(R.id.bookmarks_menu).isChecked = true
        binding.navigationBar.menu.findItem(R.id.bookmarks_menu)
            .setIcon(R.drawable.menu_bookmark_active)
        binding.navigationBar.menu.findItem(R.id.home_menu)
            .setIcon(R.drawable.menu_home_icon_inactive)
    }

    private suspend fun checkData() {
        val hasPhotos = viewModel.hasPhotos()
        if (!hasPhotos) {
            binding.haventSaved.visibility = View.VISIBLE
            binding.bookmarksExplore.visibility = View.VISIBLE
        } else {
            binding.haventSaved.visibility = View.GONE
            binding.bookmarksExplore.visibility = View.GONE
        }
    }

    private fun loadBookmarksPhoto() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getPagingPhotos().collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
        binding.bookmarksRecyclerview.visibility = View.VISIBLE
        binding.bookmarksRecyclerview.adapter = adapter
    }

    private fun showProgressBar() {
        _binding?.let {
            it.progressbar.visibility = View.VISIBLE
            it.progressbar.progress = 0
            val animator = ValueAnimator.ofInt(0, 100)
            animator.duration = 1000
            animator.addUpdateListener { animation ->
                _binding?.progressbar?.progress = animation.animatedValue as Int
            }
            animator.start()
        }
    }

    private fun hideProgressBar() {
        _binding?.let {
            it.progressbar.visibility = View.GONE
            it.progressbar.progress = 0
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
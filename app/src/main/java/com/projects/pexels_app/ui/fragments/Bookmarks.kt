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
import com.projects.pexels_app.R
import com.projects.pexels_app.databinding.BookmarksBinding
import com.projects.pexels_app.ui.adapter.PhotoAdapter
import com.projects.pexels_app.ui.viewmodels.BookmarksViewModel
import com.projects.pexels_app.utils.Keys
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class Bookmarks : Fragment() {
    private var _binding: BookmarksBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BookmarksViewModel by viewModels({ requireActivity() })
    private lateinit var adapter: PhotoAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = BookmarksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setupNavBindings()

        lifecycleScope.launch {
            checkData()
            loadBookmarksPhoto()
        }

        binding.navigationBar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home_menu -> {
                    androidx.navigation.Navigation.findNavController(view)
                        .navigate(R.id.action_bookmarks_fragment_to_home_fragment)
                }
            }
            true
        }

        adapter = PhotoAdapter(Keys.BOOKMARK.name) { photo, sourceFragment ->
            val bundle = Bundle().apply {
                if (photo != null) {
                    putInt(Keys.ID.name, photo.id)
                    putString(Keys.FRAGMENT_NAME.name, sourceFragment)
                }
            }
            if (findNavController().currentDestination?.id != R.id.details_fragment) {
                findNavController().navigate(
                    R.id.action_bookmarks_fragment_to_details_fragment,
                    bundle
                )
            }
        }


        binding.bookmarksExplore.setOnClickListener {
            androidx.navigation.Navigation.findNavController(view)
                .navigate(R.id.action_bookmarks_fragment_to_home_fragment)
        }
    }

    private fun setupNavBindings() {
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
            lifecycleScope.launch {
                viewModel.isLoadingData.collect { isLoading ->
                    if (isLoading) {
                        showProgressBar()
                    } else {
                        hideProgressBar()
                    }
                }
            }

            lifecycleScope.launch {
                viewModel.getPagingPhotos().collectLatest { pagingData ->
                    adapter.submitData(pagingData)
                }
            }
            binding.bookmarksRecyclerview.adapter = adapter
            binding.bookmarksRecyclerview.visibility = View.VISIBLE
        }

    private fun showProgressBar() {
        binding.progressbar.visibility = View.VISIBLE
        binding.progressbar.progress = 0
        val animator = ValueAnimator.ofInt(0, 100)
        animator.duration = 1000
        animator.addUpdateListener { animation ->
            binding.progressbar.progress = animation.animatedValue as Int
        }
        animator.start()
    }

    private fun hideProgressBar() {
        binding.progressbar.visibility = View.GONE
        binding.progressbar.progress = 0
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
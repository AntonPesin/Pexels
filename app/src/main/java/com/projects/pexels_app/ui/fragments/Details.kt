package com.projects.pexels_app.ui.fragments

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.projects.pexels_app.ui.navigation.Navigation
import com.projects.pexels_app.R
import com.projects.pexels_app.data.network.models.MediaModel
import com.projects.pexels_app.databinding.DetailsBinding
import com.projects.pexels_app.ui.viewmodels.DetailsViewModel
import com.projects.pexels_app.utils.ImageDownloader
import com.projects.pexels_app.utils.Keys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Details : Fragment() {
    private var _binding: DetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var navigationHandler: Navigation
    private val viewModel: DetailsViewModel by viewModels({ requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = DetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val downloader = ImageDownloader(requireContext())
        navigationHandler = Navigation(findNavController())
        val photoId = arguments?.getInt(Keys.ID.name)

        photoId?.let { id ->
            lifecycleScope.launch {
                showProgressBar()
                delay(1000)
                try {
                    val data = getData(id)
                    val photo = data.src.medium
                    val name = data.photographer
                    binding.name.text = name
                    Glide.with(requireContext())
                        .load(photo)
                        .transform(CenterCrop(), RoundedCorners(30))
                        .error(R.drawable.placeholder)
                        .into(binding.zoomablePhoto)

                    val exists = viewModel.photoExists(id)
                    updateBookmarkIcon(exists)

                    binding.backIcon.setOnClickListener {
                        findNavController().navigateUp()
                    }

                    binding.download.setOnClickListener {
                        lifecycleScope.launch {
                            downloader.downloadImage(photo, "${R.string.image} $name")
                        }
                    }

                    binding.bookmarkDetails.setOnClickListener {
                        lifecycleScope.launch {
                            if (viewModel.photoExists(id)) {
                                viewModel.deletePhoto(id)
                                updateBookmarkIcon(false)
                            } else {
                                viewModel.addPhoto(id, name, photo)
                                updateBookmarkIcon(true)
                            }
                        }
                    }
                } finally {
                    hideProgressBar()
                }
            }
        }

        binding.explore.setOnClickListener {
            navigationHandler.navigateDetailsToHome()
        }
    }

    private fun updateBookmarkIcon(exists: Boolean) {
        val drawableId = if (exists) {
            R.drawable.bookmark_details_button_active
        } else {
            R.drawable.bookmark_details_button_inactive
        }
        binding.bookmarkDetails.setImageDrawable(
            ContextCompat.getDrawable(requireContext(), drawableId)
        )
    }

    private fun checkFragment(): Boolean {
        return arguments?.getString(Keys.FRAGMENT_NAME.name) == Keys.HOME.name
    }

    private suspend fun getData(id: Int?): MediaModel {
        return if (checkFragment()) {
            getDataFromApi(id)
        } else {
            getDataFromDataBase(id)
        }
    }

    private suspend fun getDataFromApi(id: Int?): MediaModel {
        return viewModel.getPhotoFromApi(id)
    }

    private suspend fun getDataFromDataBase(id: Int?): MediaModel {
        return withContext(Dispatchers.IO) {
            viewModel.photoDao.get(id)
        }
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
        binding.downloadText.visibility = View.VISIBLE
        binding.backIcon.visibility = View.VISIBLE
        binding.download.visibility = View.VISIBLE
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

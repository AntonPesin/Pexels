package com.projects.pexels_app.ui.fragments

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.projects.pexels_app.R
import com.projects.pexels_app.databinding.DetailsBinding
import com.projects.pexels_app.domain.models.Photo
import com.projects.pexels_app.ui.viewmodels.DetailsViewModel
import com.projects.pexels_app.utils.downloader.ImageDownloader
import com.projects.pexels_app.utils.Keys
import kotlinx.coroutines.launch

class Details : Fragment() {
    private var _binding: DetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DetailsViewModel by viewModels({ requireActivity() })
    private val downloader by lazy { ImageDownloader(requireContext()) }
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

        val photoId = arguments?.getInt(Keys.ID.name)
        if (photoId != null) {
            setupListeners(photoId)
        }
        lifecycleScope.launch {
            photoId?.let {
                loadData(it)
            }
        }


        collectErrorMessage()
    }

    private fun setupListeners(photoId: Int) {
        binding.backIcon.setOnClickListener { findNavController().navigateUp() }

        binding.download.setOnClickListener {

            lifecycleScope.launch {
                viewModel.isConnected.collect { isConnected ->
                    if (!isConnected) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.internet_off),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    } else {
                        photoId.let { id ->
                            lifecycleScope.launch {
                                val photo = viewModel.getPhotoByIdFromApi(id).src.medium
                                downloader.downloadImage(
                                    photo,
                                    getString(R.string.image) + " " + binding.name.text
                                )
                            }
                        }
                    }
                }
            }
        }

        binding.bookmark.setOnClickListener {
            lifecycleScope.launch {
                val exists = viewModel.photoExists(photoId)
                if (exists) {
                    viewModel.deletePhoto(photoId)
                } else {
                    val photoInfo = viewModel.getPhotoByIdFromApi(photoId)
                    viewModel.addPhoto(photoId, photoInfo.photographer, photoInfo.src.medium)
                }
                updateBookmarkIcon(!exists)
            }
        }

        binding.explore.setOnClickListener {
            findNavController().navigate(R.id.action_details_fragment_to_home_fragment)
        }
    }

    private suspend fun loadData(id: Int) {
        try {
            showProgressBar()
            val data = getData(id)
            displayPhotoData(data)
            val exists = viewModel.photoExists(id)
            updateBookmarkIcon(exists)
        } catch (e: Exception) {
            handleError(e)
        } finally {
            hideProgressBar()
        }
    }

    private fun displayPhotoData(photo: Photo) {
        binding.name.text = photo.photographer
        Glide.with(requireContext())
            .load(photo.src.medium)
            .transform(CenterCrop(), RoundedCorners(30))
            .error(R.drawable.placeholder)
            .into(binding.zoomablePhoto)
    }

    private fun updateBookmarkIcon(exists: Boolean) {
        val drawableId = if (exists) {
            R.drawable.bookmark_details_button_active
        } else {
            R.drawable.bookmark_details_button_inactive
        }
        binding.bookmark.setImageDrawable(
            ContextCompat.getDrawable(requireContext(), drawableId)
        )
    }

    private suspend fun getData(id: Int): Photo {
        return if (checkFragment()) {
            viewModel.getPhotoByIdFromApi(id)
        } else {
            viewModel.getPhotoByIdFromDataBase(id)
        }
    }

    private fun checkFragment(): Boolean {
        return arguments?.getString(Keys.FRAGMENT_NAME.name) == Keys.HOME.name
    }

    private fun collectErrorMessage() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorMessage().collect { errorMessage ->
                errorMessage?.let {
                    Toast.makeText(requireContext(), "$errorMessage", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun handleError(exception: Exception) {
        hideProgressBar()
        binding.explore.visibility = View.VISIBLE
        binding.errorImageNotFound.visibility = View.VISIBLE
        binding.download.visibility = View.GONE
        binding.downloadText.visibility = View.GONE
        binding.bookmark.visibility = View.GONE
        Toast.makeText(
            requireContext(),
            getString(R.string.error_tag) + " " + exception.message,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showProgressBar() {
        binding.progressbar.apply {
            visibility = View.VISIBLE
            progress = 0
            ValueAnimator.ofInt(0, 100).apply {
                duration = 1000
                addUpdateListener { animation ->
                    progress = animation.animatedValue as Int
                }
                start()
            }
        }
    }

    private fun hideProgressBar() {
        binding.progressbar.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

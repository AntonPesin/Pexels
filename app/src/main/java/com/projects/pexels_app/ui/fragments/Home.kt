package com.projects.pexels_app.ui.fragments


import android.animation.ValueAnimator
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.projects.pexels_app.R
import com.projects.pexels_app.data.network.models.CollectionModel
import com.projects.pexels_app.data.network.models.MediaModel
import com.projects.pexels_app.databinding.HomeBinding
import com.projects.pexels_app.ui.adapters.CuratedPhotoAdapter
import com.projects.pexels_app.ui.adapters.PhotoAdapter
import com.projects.pexels_app.ui.navigation.Navigation
import com.projects.pexels_app.ui.viewmodels.MainViewModel
import com.projects.pexels_app.utils.Keys
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


class Home : Fragment() {
    private var _binding: HomeBinding? = null
    private val binding get() = _binding!!
    private val mainViewModel: MainViewModel by viewModels({ requireActivity() })
    private lateinit var navigationHandler: Navigation
    private var selectedChip: Chip? = null
    private var searchJob: Job? = null
    private val curatedPhotoAdapter = CuratedPhotoAdapter()
    private val photoAdapter = PhotoAdapter()
    private var currentAdapter: RecyclerView.Adapter<*>? = null
    private val activeChips = mutableSetOf<Chip>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = HomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navigationHandler = Navigation(findNavController())

        setNavItemsIcons()
        checkInternetConnectivity()

        binding.navigationBar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bookmarks_menu -> {
                    binding.navigationBar.menu.findItem(R.id.home_menu).isChecked = false
                    navigationHandler.navigateHomeToBookmarks()
                }
            }
            true
        }

        binding.stubInternetFrame.setOnClickListener {
            checkInternetConnectivity()
        }

        binding.searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(keyword: String): Boolean {
                    lifecycleScope.launch {
                        performSearch(keyword)
                    }
                    return true
                }

                override fun onQueryTextChange(keyword: String): Boolean {
                    updateChipState(keyword)
                    searchJob?.cancel()
                    searchJob = lifecycleScope.launch {
                        delay(1000)
                        performSearch(keyword)
                    }
                    return true
                }
            }
        )

        curatedPhotoAdapter.setOnImageClickListener(
            object : CuratedPhotoAdapter.PhotoClickListener {
                override fun onPhotoClick(photo: MediaModel?) {
                    navigationHandler.navigateHomeToDetails(photo?.id, Keys.HOME.name)
                }
            })

        photoAdapter.setOnImageClickListener(
            object : PhotoAdapter.PhotoClickListener {
                override fun onPhotoClick(photo: MediaModel?) {
                    navigationHandler.navigateHomeToDetails(photo?.id, Keys.HOME.name)
                }
            })

        setupLoadStateListener()

    }

    private fun setNavItemsIcons() {
        binding.navigationBar.itemIconTintList = null
        binding.navigationBar.menu.findItem(R.id.home_menu)
            .setIcon(R.drawable.menu_home_icon_active)
        binding.navigationBar.menu.findItem(R.id.bookmarks_menu)
            .setIcon(R.drawable.menu_bookmark_inactive)
    }

    private fun setupLoadStateListener() {
        photoAdapter.addLoadStateListener { loadState ->
            if (_binding != null) {
                when (loadState.refresh) {
                    is LoadState.Loading -> showLoadingState()
                    is LoadState.NotLoading -> showContentState()
                    is LoadState.Error -> showErrorState(loadState.refresh as LoadState.Error)
                }
            }
        }
    }

    private fun showLoadingState() {
        showProgressBar()
        binding.stubInternetFrame.visibility = View.GONE
        binding.error.visibility = View.GONE
        binding.explore.visibility = View.GONE
        binding.homeRecyclerview.visibility = View.VISIBLE
    }

    private fun showContentState() {
        hideProgressBar()
        if (photoAdapter.itemCount == 0) {
            binding.homeRecyclerview.visibility = View.GONE
        } else {
            binding.error.visibility = View.GONE
            binding.homeRecyclerview.visibility = View.VISIBLE
        }
    }

    private fun showErrorState(errorState: LoadState.Error) {
        hideProgressBar()
        Toast.makeText(
            requireContext(),
            "${R.string.error_tag} ${errorState.error.message}",
            Toast.LENGTH_LONG
        ).show()
    }

    private suspend fun loadChips() {
        binding.chipGroup.removeAllViews()
        mainViewModel.collections.collect { collections ->
            updateChipGroup(collections)
        }
    }

    private fun updateChipGroup(collections: List<CollectionModel>) {
        collections.forEach { collection ->
            val chip = createCustomChip(collection)
            binding.chipGroup.addView(chip)
        }

        val params = binding.progressbar.layoutParams as ConstraintLayout.LayoutParams
        params.topToBottom = if (binding.chipGroup.childCount > 0) {
            binding.horizontalScrollView.id
        } else {
            binding.searchView.id
        }
        binding.progressbar.layoutParams = params
    }

    private fun createCustomChip(collection: CollectionModel): Chip {
        val chip = Chip(requireContext()).apply {
            text = collection.title
            isCheckable = true
            chipStrokeWidth = 0f
            chipCornerRadius = resources.getDimension(R.dimen.chip_corner_radius)
            checkedIcon = null
            setRippleColorResource(R.color.app_red)
            setChipBackgroundColorResource(R.color.chip_background_selector)
            setTextAppearanceResource(R.style.ChipTextAppearance)
            gravity = Gravity.CENTER
            textAlignment = View.TEXT_ALIGNMENT_CENTER
        }

        chip.setOnClickListener {
            binding.searchView.setQuery(chip.text, true)
        }

        chip.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectChip(chip)
                binding.homeRecyclerview.visibility = View.GONE
                binding.error.visibility = View.GONE
                binding.explore.visibility = View.GONE
                activeChips.add(chip)
                chip.isClickable = false
                chip.isFocusable = false
            } else {
                deselectChip(chip)
                activeChips.remove(chip)
                chip.isClickable = true
                chip.isFocusable = true
            }
        }

        return chip
    }

    private suspend fun performSearch(keyword: String) {
        currentAdapter = photoAdapter
        binding.homeRecyclerview.adapter = currentAdapter
        if (keyword.isNotEmpty()) {
            delay(1000)
            mainViewModel.keyword.value = keyword
            mainViewModel.pagedSearchPhotos.collectLatest { pagingData ->
                photoAdapter.submitData(pagingData)
            }
        } else {
            loadCuratedPhotos()
        }
    }

    private fun loadCuratedPhotos() {
        lifecycleScope.launch {
            delay(1000)
            val curatedPhotos = mainViewModel.getCuratedPhotos()
            curatedPhotos?.let {
                currentAdapter = curatedPhotoAdapter
                binding.homeRecyclerview.adapter = curatedPhotoAdapter
                curatedPhotoAdapter.setData(it)
                if (curatedPhotoAdapter.itemCount > 0) {
                    binding.homeRecyclerview.visibility = View.VISIBLE
                    binding.error.visibility = View.GONE
                    binding.explore.visibility = View.GONE
                } else {
                    Toast.makeText(requireContext(), "429", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showProgressBar() {
        _binding?.let {
            it.progressbar.visibility = View.VISIBLE
            it.progressbar.progress = 0
            val animator = ValueAnimator.ofInt(0, 100)
            animator.duration = 1500
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

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun checkInternetConnectivity() {
        if (!isInternetAvailable(requireContext())) {
            Toast.makeText(requireContext(), R.string.internet_off, Toast.LENGTH_LONG).show()
            binding.stubInternetFrame.visibility = View.VISIBLE
        } else {
            lifecycleScope.launch {
                mainViewModel.loadCollections()
                loadCuratedPhotos()
                loadChips()
            }
            _binding?.stubInternetFrame?.visibility = View.GONE
        }
    }

    private fun selectChip(chip: Chip) {
        selectedChip?.isChecked = false
        selectedChip = chip
        chip.isChecked = true
    }

    private fun deselectChip(chip: Chip) {
        if (selectedChip == chip) {
            selectedChip = null
        }
        chip.isChecked = false
    }

    private fun updateChipState(searchText: String) {
        val chipCount = binding.chipGroup.childCount
        for (i in 0 until chipCount) {
            val chip = binding.chipGroup.getChildAt(i) as Chip
            if (chip.text.toString().equals(searchText, ignoreCase = true)) {
                selectChip(chip)
                return
            }
        }
        selectedChip?.isChecked = false
        selectedChip = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


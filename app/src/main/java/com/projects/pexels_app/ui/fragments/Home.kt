package com.projects.pexels_app.ui.fragments


import android.animation.ValueAnimator
import android.os.Bundle
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
import com.projects.pexels_app.databinding.HomeBinding
import com.projects.pexels_app.domain.models.Collection
import com.projects.pexels_app.ui.adapter.PhotoAdapter
import com.projects.pexels_app.ui.viewmodels.MainViewModel
import com.projects.pexels_app.utils.Keys
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class Home : Fragment() {
    private var _binding: HomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels({ requireActivity() })
    private var selectedChip: Chip? = null
    private var searchJob: Job? = null
    private var loadCuratedPhotosJob: Job? = null
    private lateinit var adapter: PhotoAdapter
    private val activeChips = mutableSetOf<Chip>()
    private var isInternetAvailable = false

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


        lifecycleScope.launch {
            viewModel.loadCollections()
            loadCuratedPhotos()
            loadChips()
            setupViews()
            setupListeners()
            setNavItemsIcons()
        }
        lifecycleScope.launch {
            viewModel.isConnected.collect { isConnected ->
                if (isConnected) {
                    isInternetAvailable = true
                } else {
                    if (_binding != null) {
                        loadCuratedPhotosJob?.cancel()
                        searchJob?.cancel()
                        isInternetAvailable = false
                        binding.homeRecyclerview.visibility = View.GONE
                        binding.chipGroup.visibility = View.GONE
                        hideProgressBar()
                        binding.stubInternetFrame.visibility = View.VISIBLE
                    }
                }
            }
        }

    }

    private fun setupViews() {
        adapter = PhotoAdapter(Keys.HOME.name) { photo, sourceFragment ->
            val bundle = Bundle().apply {
                if (photo != null) {
                    putInt(Keys.ID.name, photo.id)
                    putString(Keys.FRAGMENT_NAME.name, sourceFragment)
                }
            }
            if (findNavController().currentDestination?.id != R.id.details_fragment) {
                findNavController().navigate(R.id.action_home_fragment_to_details_fragment, bundle)
            }
        }
        adapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        binding.homeRecyclerview.adapter = adapter
        setupExploreError()
        setupLoadStateListener()

    }

    private fun setupExploreError() {
        if (isInternetAvailable && adapter.itemCount > 0) {
            binding.explore.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE
            binding.error.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE
        }
    }

    private fun setupListeners() {
        binding.navigationBar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bookmarks_menu -> {
                    binding.navigationBar.menu.findItem(R.id.home_menu).isChecked = false
                    findNavController().navigate(R.id.action_home_fragment_to_bookmarks_fragment)
                }
            }
            true
        }

        binding.explore.setOnClickListener {
            loadCuratedPhotos()
        }

        binding.stubInternetFrame.setOnClickListener {
            lifecycleScope.launch {
                if (isInternetAvailable) {
                    if (binding.searchView.query.isNotEmpty()) {
                        searchForPhotos(binding.searchView.query.toString())
                    } else {
                        loadCuratedPhotos()
                    }
                    viewModel.loadCollections()
                    loadChips()
                    binding.homeRecyclerview.visibility = View.VISIBLE
                    binding.chipGroup.visibility = View.VISIBLE
                    binding.stubInternetFrame.visibility = View.GONE
                } else {
                    Toast.makeText(requireContext(), R.string.internet_off, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        binding.searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    if (query.isNotEmpty()) {
                        searchForPhotos(query)
                    }
                    return true
                }

                override fun onQueryTextChange(query: String): Boolean {
                    updateChipState(query)
                    if (query.isNotEmpty()) {
                        searchForPhotos(query)
                    } else {
                        loadCuratedPhotos()
                    }
                    return true
                }
            }
        )

        binding.searchView.setOnCloseListener {
            loadCuratedPhotos()
            false
        }
    }


    private fun setNavItemsIcons() {
        binding.navigationBar.itemIconTintList = null
        binding.navigationBar.menu.findItem(R.id.home_menu)
            .setIcon(R.drawable.menu_home_icon_active)
        binding.navigationBar.menu.findItem(R.id.bookmarks_menu)
            .setIcon(R.drawable.menu_bookmark_inactive)
    }

    private fun setupLoadStateListener() {
        adapter.addLoadStateListener { loadState ->
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
        _binding?.apply {
            showProgressBar()
            progressbar.visibility = View.VISIBLE
            stubInternetFrame.visibility = View.GONE
            error.visibility = View.GONE
            explore.visibility = View.GONE
            homeRecyclerview.visibility = View.VISIBLE
        }
    }

    private fun showContentState() {
        hideProgressBar()
        if (adapter.itemCount == 0) {
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
        lifecycleScope.launch {
            viewModel.collections.collect { collections ->
                if (_binding != null) {
                    updateChipGroup(collections)
                }
            }
        }
    }

    private fun updateChipGroup(collections: List<Collection>) {
        binding.chipGroup.removeAllViews()
        collections.forEach { collection ->
            val chip = createCustomChip(collection)
            binding.chipGroup.addView(chip)
        }
        setProgressBar()
    }

    private fun setProgressBar() {
        val params = binding.progressbar.layoutParams as ConstraintLayout.LayoutParams
        params.topToBottom = if (binding.chipGroup.childCount > 0) {
            binding.horizontalScrollView.id
        } else {
            binding.searchView.id
        }
        binding.progressbar.layoutParams = params
    }

    private fun createCustomChip(collection: Collection): Chip {
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

    private fun loadCuratedPhotos() {
        loadCuratedPhotosJob?.cancel()
        loadCuratedPhotosJob = viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getCuratedPhotos().collect {
                adapter.submitData(it)
            }
        }
    }

    private fun showProgressBar() {

        binding.progressbar.visibility = View.VISIBLE
        binding.progressbar.progress = 0
        val animator = ValueAnimator.ofInt(0, 100)
        animator.duration = 1000
        animator.addUpdateListener { animation ->
            _binding?.progressbar?.progress = animation.animatedValue as Int
        }
        animator.start()

    }

    private fun hideProgressBar() {
        binding.progressbar.visibility = View.GONE
        binding.progressbar.progress = 0

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

    private fun searchForPhotos(query: String) {
        searchJob?.cancel()
        searchJob = viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getSearchResults(query).collect {
                adapter.submitData(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchJob?.cancel()
        loadCuratedPhotosJob?.cancel()
        _binding = null
    }
}


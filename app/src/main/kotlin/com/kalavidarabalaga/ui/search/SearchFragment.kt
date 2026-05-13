package com.kalavidarabalaga.ui.search

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.kalavidarabalaga.adapters.TroupeAdapter
import com.kalavidarabalaga.databinding.FragmentSearchBinding
import com.kalavidarabalaga.ui.TroupeDetailActivity
import com.kalavidarabalaga.utils.Constants
import com.kalavidarabalaga.viewmodels.TroupeViewModel
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TroupeViewModel by activityViewModels()
    private lateinit var searchAdapter: TroupeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpinners()
        setupRecyclerView()
        setupButtons()
        observeViewModel()
    }

    private fun setupSpinners() {
        val districtAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            Constants.KARNATAKA_DISTRICTS
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        val artFormAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            Constants.FOLK_ART_FORMS
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.spinnerDistrict.adapter = districtAdapter
        binding.spinnerArtForm.adapter = artFormAdapter
    }

    private fun setupRecyclerView() {
        searchAdapter = TroupeAdapter { troupe ->
            val intent = Intent(requireContext(), TroupeDetailActivity::class.java).apply {
                putExtra(Constants.EXTRA_TROUPE_ID, troupe.id)
                putExtra(Constants.EXTRA_TROUPE_NAME, troupe.name)
            }
            startActivity(intent)
        }

        binding.searchResultsRecyclerView.apply {
            adapter = searchAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
            isNestedScrollingEnabled = false
        }
    }

    private fun setupButtons() {
        binding.btnSearch.setOnClickListener {
            val district = binding.spinnerDistrict.selectedItem as? String
            val artForm = binding.spinnerArtForm.selectedItem as? String
            viewModel.searchTroupes(district, artForm)
        }

        binding.btnClear.setOnClickListener {
            binding.spinnerDistrict.setSelection(0)
            binding.spinnerArtForm.setSelection(0)
            viewModel.clearSearch()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.isSearchLoading.collect { loading ->
                binding.searchProgressBar.visibility = if (loading) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.searchResults.collect { results ->
                searchAdapter.submitList(results)

                val hasSearched = viewModel.hasSearched.value
                when {
                    !hasSearched -> {
                        binding.searchResultsRecyclerView.visibility = View.GONE
                        binding.emptySearchState.visibility = View.GONE
                    }
                    results.isEmpty() -> {
                        binding.searchResultsRecyclerView.visibility = View.GONE
                        binding.emptySearchState.visibility = View.VISIBLE
                    }
                    else -> {
                        binding.searchResultsRecyclerView.visibility = View.VISIBLE
                        binding.emptySearchState.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

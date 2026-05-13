package com.kalavidarabalaga.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.kalavidarabalaga.adapters.TroupeAdapter
import com.kalavidarabalaga.databinding.FragmentHomeBinding
import com.kalavidarabalaga.ui.TroupeDetailActivity
import com.kalavidarabalaga.utils.Constants
import com.kalavidarabalaga.viewmodels.TroupeViewModel
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TroupeViewModel by activityViewModels()
    private lateinit var troupeAdapter: TroupeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeRefresh()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        troupeAdapter = TroupeAdapter { troupe ->
            val intent = Intent(requireContext(), TroupeDetailActivity::class.java).apply {
                putExtra(Constants.EXTRA_TROUPE_ID, troupe.id)
                putExtra(Constants.EXTRA_TROUPE_NAME, troupe.name)
            }
            startActivity(intent)
        }

        binding.troupeRecyclerView.apply {
            adapter = troupeAdapter
            layoutManager = GridLayoutManager(requireContext(), 2)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(
            com.kalavidarabalaga.R.color.kb_saffron,
            com.kalavidarabalaga.R.color.kb_deep_red
        )
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadAllTroupes()
        }

        binding.retryButton.setOnClickListener {
            viewModel.loadAllTroupes()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.isLoading.collect { loading ->
                if (!loading) {
                    binding.swipeRefresh.isRefreshing = false
                }
                if (loading && troupeAdapter.currentList.isEmpty()) {
                    binding.shimmerLayout.visibility = View.VISIBLE
                    binding.shimmerLayout.startShimmer()
                    binding.troupeRecyclerView.visibility = View.GONE
                    binding.emptyState.visibility = View.GONE
                    binding.errorState.visibility = View.GONE
                } else if (!loading) {
                    binding.shimmerLayout.stopShimmer()
                    binding.shimmerLayout.visibility = View.GONE
                }
            }
        }

        lifecycleScope.launch {
            viewModel.troupes.collect { troupes ->
                troupeAdapter.submitList(troupes)
                if (troupes.isEmpty() && viewModel.isLoading.value == false) {
                    binding.emptyState.visibility = View.VISIBLE
                    binding.troupeRecyclerView.visibility = View.GONE
                } else {
                    binding.emptyState.visibility = View.GONE
                    binding.troupeRecyclerView.visibility = View.VISIBLE
                }
            }
        }

        lifecycleScope.launch {
            viewModel.error.collect { error ->
                if (error != null && troupeAdapter.currentList.isEmpty()) {
                    binding.errorState.visibility = View.VISIBLE
                    binding.errorMessage.text = error
                    binding.troupeRecyclerView.visibility = View.GONE
                    binding.shimmerLayout.visibility = View.GONE
                } else {
                    binding.errorState.visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

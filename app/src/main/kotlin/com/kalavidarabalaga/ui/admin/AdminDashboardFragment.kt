package com.kalavidarabalaga.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.kalavidarabalaga.adapters.AdminTroupeAdapter
import com.kalavidarabalaga.databinding.FragmentAdminDashboardBinding
import com.kalavidarabalaga.models.Troupe
import com.kalavidarabalaga.repository.TroupeRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!
    private val troupeRepository = TroupeRepository()
    private lateinit var pendingAdapter: AdminTroupeAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pendingAdapter = AdminTroupeAdapter(
            onApprove = { troupe -> approveTroupe(troupe) },
            onDeactivate = {},
            onClick = { openDetail(it) }
        )
        binding.rvPendingTroupes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPendingTroupes.adapter = pendingAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            troupeRepository.getAllTroupesForAdmin().collectLatest { troupes ->
                val total = troupes.size
                val active = troupes.count { it.isActive }
                val pending = troupes.count { !it.isActive }

                binding.tvTotalCount.text = total.toString()
                binding.tvActiveCount.text = active.toString()
                binding.tvPendingCount.text = pending.toString()

                val pendingList = troupes.filter { !it.isActive }.take(5)
                pendingAdapter.submitList(pendingList)
                binding.tvNoPending.visibility = if (pendingList.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun approveTroupe(troupe: Troupe) {
        troupeRepository.approveTroupe(troupe.id) { success ->
            if (!success) Toast.makeText(requireContext(), "Failed to approve", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openDetail(troupe: Troupe) {
        val intent = android.content.Intent(requireContext(), com.kalavidarabalaga.ui.TroupeDetailActivity::class.java)
        intent.putExtra(com.kalavidarabalaga.utils.Constants.EXTRA_TROUPE_ID, troupe.id)
        intent.putExtra(com.kalavidarabalaga.utils.Constants.EXTRA_TROUPE_NAME, troupe.name)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

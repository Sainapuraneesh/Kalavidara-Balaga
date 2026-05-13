package com.kalavidarabalaga.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.kalavidarabalaga.adapters.AdminTroupeAdapter
import com.kalavidarabalaga.databinding.FragmentAdminTroupeListBinding
import com.kalavidarabalaga.models.Troupe
import com.kalavidarabalaga.repository.TroupeRepository
import com.kalavidarabalaga.ui.TroupeDetailActivity
import com.kalavidarabalaga.utils.Constants
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AdminTroupeListFragment : Fragment() {

    private var _binding: FragmentAdminTroupeListBinding? = null
    private val binding get() = _binding!!
    private val troupeRepository = TroupeRepository()
    private lateinit var adapter: AdminTroupeAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminTroupeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AdminTroupeAdapter(
            onApprove = { troupe -> confirmApprove(troupe) },
            onDeactivate = { troupe -> confirmDeactivate(troupe) },
            onClick = { troupe -> openDetail(troupe) }
        )
        binding.rvAdminTroupes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAdminTroupes.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            troupeRepository.getAllTroupesForAdmin().collectLatest { troupes ->
                adapter.submitList(troupes)
                binding.tvEmpty.visibility = if (troupes.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun confirmApprove(troupe: Troupe) {
        AlertDialog.Builder(requireContext())
            .setTitle("Approve Troupe")
            .setMessage("Approve \"${troupe.name}\"? It will become visible to all users.")
            .setPositiveButton("Approve") { _, _ ->
                troupeRepository.approveTroupe(troupe.id) { success ->
                    val msg = if (success) "Troupe approved" else "Failed to approve"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDeactivate(troupe: Troupe) {
        AlertDialog.Builder(requireContext())
            .setTitle("Deactivate Troupe")
            .setMessage("Deactivate \"${troupe.name}\"? It will be hidden from users.")
            .setPositiveButton("Deactivate") { _, _ ->
                troupeRepository.deactivateTroupe(troupe.id) { success ->
                    val msg = if (success) "Troupe deactivated" else "Failed to deactivate"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openDetail(troupe: Troupe) {
        val intent = Intent(requireContext(), TroupeDetailActivity::class.java)
        intent.putExtra(Constants.EXTRA_TROUPE_ID, troupe.id)
        intent.putExtra(Constants.EXTRA_TROUPE_NAME, troupe.name)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

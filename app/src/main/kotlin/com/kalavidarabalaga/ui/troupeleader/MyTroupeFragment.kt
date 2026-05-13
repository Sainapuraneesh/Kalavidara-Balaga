package com.kalavidarabalaga.ui.troupeleader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.kalavidarabalaga.R
import com.kalavidarabalaga.adapters.BookingAdapter
import com.kalavidarabalaga.databinding.FragmentMyTroupeBinding
import com.kalavidarabalaga.models.Booking
import com.kalavidarabalaga.repository.BookingRepository
import com.kalavidarabalaga.repository.TroupeRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MyTroupeFragment : Fragment() {

    private var _binding: FragmentMyTroupeBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val troupeRepository = TroupeRepository()
    private val bookingRepository = BookingRepository()
    private lateinit var bookingAdapter: BookingAdapter
    private var currentTroupeId: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMyTroupeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bookingAdapter = BookingAdapter(
            onAccept = { booking -> updateBookingStatus(booking, "accepted") },
            onReject = { booking -> updateBookingStatus(booking, "rejected") }
        )
        binding.rvBookings.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBookings.adapter = bookingAdapter

        val uid = auth.currentUser?.uid ?: return

        binding.progressBar.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            troupeRepository.getTroupeByOwner(uid).collectLatest { troupe ->
                binding.progressBar.visibility = View.GONE
                if (troupe == null) {
                    showEmptyState()
                } else {
                    currentTroupeId = troupe.id
                    showTroupeCard(troupe.name, troupe.artForm, troupe.district, troupe.isActive, troupe.id)
                    loadBookings(uid)
                }
            }
        }

        binding.btnAddTroupe.setOnClickListener {
            findNavController().navigate(R.id.action_myTroupe_to_addEditTroupe)
        }
    }

    private fun showEmptyState() {
        binding.layoutEmpty.visibility = View.VISIBLE
        binding.layoutTroupeCard.visibility = View.GONE
        binding.tvBookingsHeader.visibility = View.GONE
        binding.rvBookings.visibility = View.GONE
        binding.tvNoBookings.visibility = View.GONE
    }

    private fun showTroupeCard(name: String, artForm: String, district: String, isActive: Boolean, troupeId: String) {
        binding.layoutEmpty.visibility = View.GONE
        binding.layoutTroupeCard.visibility = View.VISIBLE
        binding.tvTroupeName.text = name
        binding.tvArtFormDistrict.text = "$artForm • $district"
        if (isActive) {
            binding.tvTroupeStatus.text = "Active"
            binding.tvTroupeStatus.setBackgroundResource(R.drawable.badge_active)
        } else {
            binding.tvTroupeStatus.text = "Pending Approval"
            binding.tvTroupeStatus.setBackgroundResource(R.drawable.badge_pending)
        }
        binding.btnEditTroupe.setOnClickListener {
            findNavController().navigate(
                R.id.action_myTroupe_to_addEditTroupe,
                bundleOf("troupeId" to troupeId)
            )
        }
        binding.tvBookingsHeader.visibility = View.VISIBLE
    }

    private fun loadBookings(uid: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            bookingRepository.getBookingsForLeader(uid).collectLatest { bookings ->
                bookingAdapter.submitList(bookings)
                binding.rvBookings.visibility = if (bookings.isEmpty()) View.GONE else View.VISIBLE
                binding.tvNoBookings.visibility = if (bookings.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun updateBookingStatus(booking: Booking, status: String) {
        bookingRepository.updateBookingStatus(booking.id, status) { success ->
            if (!success) Toast.makeText(requireContext(), "Failed to update", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

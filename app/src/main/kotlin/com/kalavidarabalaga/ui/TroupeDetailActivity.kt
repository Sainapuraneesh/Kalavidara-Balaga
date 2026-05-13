package com.kalavidarabalaga.ui

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.chrisbanes.photoview.PhotoView
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.kalavidarabalaga.R
import com.kalavidarabalaga.adapters.GalleryAdapter
import com.kalavidarabalaga.databinding.ActivityTroupeDetailBinding
import com.kalavidarabalaga.models.Booking
import com.kalavidarabalaga.models.Troupe
import com.kalavidarabalaga.repository.BookingRepository
import com.kalavidarabalaga.repository.UserRepository
import com.kalavidarabalaga.utils.Constants
import com.kalavidarabalaga.viewmodels.TroupeViewModel
import kotlinx.coroutines.launch

class TroupeDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTroupeDetailBinding
    private val viewModel: TroupeViewModel by viewModels()
    private lateinit var galleryAdapter: GalleryAdapter
    private var currentTroupe: Troupe? = null
    private val auth = FirebaseAuth.getInstance()
    private val bookingRepository = BookingRepository()
    private val userRepository = UserRepository()
    private var currentUserRole: String = Constants.ROLE_USER

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTroupeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupGalleryRecyclerView()

        val troupeId = intent.getStringExtra(Constants.EXTRA_TROUPE_ID)
        if (troupeId.isNullOrBlank()) {
            finish()
            return
        }

        auth.currentUser?.uid?.let { uid ->
            userRepository.getUserRole(uid) { role ->
                currentUserRole = role ?: Constants.ROLE_USER
            }
        }

        viewModel.loadTroupeDetail(troupeId)
        observeViewModel()

        binding.fabCall.setOnClickListener { showBookingOptions() }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupGalleryRecyclerView() {
        galleryAdapter = GalleryAdapter { imageUrl, _ -> showFullscreenImage(imageUrl) }

        binding.galleryRecyclerView.apply {
            adapter = galleryAdapter
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            isNestedScrollingEnabled = false
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.selectedTroupe.collect { troupe ->
                troupe?.let { bindTroupe(it) }
            }
        }
    }

    private fun bindTroupe(troupe: Troupe) {
        currentTroupe = troupe

        binding.collapsingToolbar.title = troupe.name

        Glide.with(this)
            .load(troupe.groupPhotoUrl.ifBlank { null })
            .placeholder(R.drawable.placeholder_troupe)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
            .into(binding.ivGroupPhoto)

        binding.tvTroupeName.text = troupe.name
        binding.chipArtForm.text = troupe.artForm
        binding.tvDistrict.text = troupe.district
        binding.tvContactName.text = troupe.leadContactName
        binding.tvServiceArea.text = troupe.serviceArea.joinToString(", ")
            .ifBlank { "Not specified" }

        if (troupe.description.isNotBlank()) {
            binding.tvDescription.text = troupe.description
            binding.tvDescription.visibility = View.VISIBLE
        }

        binding.equipmentChipGroup.removeAllViews()
        if (troupe.equipmentList.isNotEmpty()) {
            troupe.equipmentList.forEach { item ->
                val chip = Chip(this).apply {
                    text = item
                    isClickable = false
                    setChipBackgroundColorResource(R.color.kb_cream)
                    setTextColor(getColor(R.color.kb_dark_text))
                    chipStrokeWidth = 1f
                    setChipStrokeColorResource(R.color.kb_gold)
                }
                binding.equipmentChipGroup.addView(chip)
            }
        }

        galleryAdapter.submitList(troupe.portfolioImages)

        binding.videoLinksContainer.removeAllViews()
        troupe.portfolioVideoLinks.forEach { url ->
            val chip = Chip(this).apply {
                text = url.take(50) + if (url.length > 50) "…" else ""
                isClickable = true
                setChipBackgroundColorResource(R.color.kb_cream)
                setTextColor(getColor(R.color.kb_saffron))
                setOnClickListener { openUrl(url) }
            }
            binding.videoLinksContainer.addView(chip)
        }
    }

    private fun showBookingOptions() {
        val troupe = currentTroupe ?: return
        val options = if (currentUserRole == Constants.ROLE_USER) {
            arrayOf("Call Now", "Send Booking Request")
        } else {
            arrayOf("Call Now")
        }

        AlertDialog.Builder(this)
            .setTitle("Contact ${troupe.name}")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> makeCall()
                    1 -> showBookingRequestDialog(troupe)
                }
            }
            .show()
    }

    private fun makeCall() {
        val phone = currentTroupe?.leadContactPhone ?: return
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
        startActivity(intent)
    }

    private fun showBookingRequestDialog(troupe: Troupe) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_booking_request, null)
        val etEventDate = dialogView.findViewById<TextInputEditText>(R.id.etEventDate)
        val etMessage = dialogView.findViewById<TextInputEditText>(R.id.etMessage)

        AlertDialog.Builder(this)
            .setTitle("Booking Request")
            .setView(dialogView)
            .setPositiveButton("Send Request") { _, _ ->
                val eventDate = etEventDate.text.toString().trim()
                val message = etMessage.text.toString().trim()
                if (message.isBlank()) {
                    Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                sendBookingRequest(troupe, eventDate, message)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun sendBookingRequest(troupe: Troupe, eventDate: String, message: String) {
        val user = auth.currentUser ?: return
        val booking = Booking(
            userId = user.uid,
            userName = user.displayName ?: "",
            userEmail = user.email ?: "",
            troupeId = troupe.id,
            troupeName = troupe.name,
            troupeLeaderId = troupe.ownerId,
            eventDate = eventDate,
            message = message
        )
        bookingRepository.createBooking(booking) { success ->
            if (success) {
                Toast.makeText(this, "Booking request sent to ${troupe.name}!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Failed to send request. Try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun showFullscreenImage(imageUrl: String) {
        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val photoView = PhotoView(this)
        dialog.setContentView(photoView)

        Glide.with(this)
            .load(imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(photoView)

        photoView.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}

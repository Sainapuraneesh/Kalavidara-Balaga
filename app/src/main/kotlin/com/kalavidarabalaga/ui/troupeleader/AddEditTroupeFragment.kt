package com.kalavidarabalaga.ui.troupeleader

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.kalavidarabalaga.databinding.FragmentAddEditTroupeBinding
import com.kalavidarabalaga.models.Troupe
import com.kalavidarabalaga.repository.TroupeRepository
import com.kalavidarabalaga.utils.Constants
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AddEditTroupeFragment : Fragment() {

    private var _binding: FragmentAddEditTroupeBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val troupeRepository = TroupeRepository()
    private var editingTroupeId: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddEditTroupeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpinners()

        val troupeId = arguments?.getString("troupeId", "") ?: ""
        if (troupeId.isNotBlank()) {
            editingTroupeId = troupeId
            binding.tvFormTitle.text = "Edit Troupe"
            binding.btnSubmit.text = "Update Troupe"
            loadExistingTroupe(troupeId)
        }

        binding.btnSubmit.setOnClickListener { submitForm() }
    }

    private fun setupSpinners() {
        val artForms = Constants.FOLK_ART_FORMS.drop(1)
        val districts = Constants.KARNATAKA_DISTRICTS.drop(1)

        binding.spinnerArtForm.adapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_spinner_dropdown_item, artForms)
        binding.spinnerDistrict.adapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_spinner_dropdown_item, districts)
    }

    private fun loadExistingTroupe(troupeId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val troupe = troupeRepository.getTroupeById(troupeId).first() ?: return@launch
                populateForm(troupe)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to load troupe data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun populateForm(troupe: Troupe) {
        binding.etTroupeName.setText(troupe.name)
        binding.etLeadContactName.setText(troupe.leadContactName)
        binding.etLeadContactPhone.setText(troupe.leadContactPhone)
        binding.etDescription.setText(troupe.description)
        binding.etGroupPhotoUrl.setText(troupe.groupPhotoUrl)
        binding.etEquipment.setText(troupe.equipmentList.joinToString("\n"))
        binding.etServiceAreas.setText(troupe.serviceArea.joinToString("\n"))
        binding.etPortfolioImages.setText(troupe.portfolioImages.joinToString("\n"))
        binding.etVideoLinks.setText(troupe.portfolioVideoLinks.joinToString("\n"))

        val artForms = Constants.FOLK_ART_FORMS.drop(1)
        val artFormIdx = artForms.indexOf(troupe.artForm)
        if (artFormIdx >= 0) binding.spinnerArtForm.setSelection(artFormIdx)

        val districts = Constants.KARNATAKA_DISTRICTS.drop(1)
        val districtIdx = districts.indexOf(troupe.district)
        if (districtIdx >= 0) binding.spinnerDistrict.setSelection(districtIdx)
    }

    private fun submitForm() {
        val name = binding.etTroupeName.text.toString().trim()
        val artForm = binding.spinnerArtForm.selectedItem?.toString() ?: ""
        val district = binding.spinnerDistrict.selectedItem?.toString() ?: ""
        val contactName = binding.etLeadContactName.text.toString().trim()
        val contactPhone = binding.etLeadContactPhone.text.toString().trim()

        if (name.isBlank()) { binding.etTroupeName.error = "Required"; return }
        if (contactName.isBlank()) { binding.etLeadContactName.error = "Required"; return }
        if (contactPhone.isBlank()) { binding.etLeadContactPhone.error = "Required"; return }

        val equipment = binding.etEquipment.text.toString()
            .split("\n").map { it.trim() }.filter { it.isNotBlank() }
        val serviceAreas = binding.etServiceAreas.text.toString()
            .split("\n").map { it.trim() }.filter { it.isNotBlank() }
        val portfolioImages = binding.etPortfolioImages.text.toString()
            .split("\n").map { it.trim() }.filter { it.isNotBlank() }
        val videoLinks = binding.etVideoLinks.text.toString()
            .split("\n").map { it.trim() }.filter { it.isNotBlank() }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSubmit.isEnabled = false

        val uid = auth.currentUser?.uid ?: return

        if (editingTroupeId != null) {
            val updates = mapOf(
                "name" to name,
                "artForm" to artForm,
                "district" to district,
                "leadContactName" to contactName,
                "leadContactPhone" to contactPhone,
                "description" to binding.etDescription.text.toString().trim(),
                "groupPhotoUrl" to binding.etGroupPhotoUrl.text.toString().trim(),
                "equipmentList" to equipment,
                "serviceArea" to serviceAreas,
                "portfolioImages" to portfolioImages,
                "portfolioVideoLinks" to videoLinks
            )
            troupeRepository.updateTroupe(editingTroupeId!!, updates) { success ->
                binding.progressBar.visibility = View.GONE
                binding.btnSubmit.isEnabled = true
                if (success) {
                    Toast.makeText(requireContext(), "Troupe updated", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Failed to update. Try again.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            val troupe = Troupe(
                name = name,
                artForm = artForm,
                district = district,
                leadContactName = contactName,
                leadContactPhone = contactPhone,
                description = binding.etDescription.text.toString().trim(),
                groupPhotoUrl = binding.etGroupPhotoUrl.text.toString().trim(),
                equipmentList = equipment,
                serviceArea = serviceAreas,
                portfolioImages = portfolioImages,
                portfolioVideoLinks = videoLinks,
                ownerId = uid
            )
            troupeRepository.addTroupe(troupe) { success, _ ->
                binding.progressBar.visibility = View.GONE
                binding.btnSubmit.isEnabled = true
                if (success) {
                    Toast.makeText(requireContext(), "Troupe submitted for approval!", Toast.LENGTH_LONG).show()
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Failed to submit. Try again.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

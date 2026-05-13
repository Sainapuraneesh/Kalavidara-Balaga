package com.kalavidarabalaga.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.kalavidarabalaga.databinding.ActivityRoleSelectionBinding
import com.kalavidarabalaga.models.User
import com.kalavidarabalaga.repository.UserRepository
import com.kalavidarabalaga.ui.admin.AdminActivity
import com.kalavidarabalaga.utils.Constants

class RoleSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoleSelectionBinding
    private val userRepository = UserRepository()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoleSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSelectUser.setOnClickListener { selectRole(Constants.ROLE_USER) }
        binding.btnSelectTroupeLeader.setOnClickListener { selectRole(Constants.ROLE_TROUPE_LEADER) }
    }

    private fun selectRole(role: String) {
        val firebaseUser = auth.currentUser ?: return
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSelectUser.isEnabled = false
        binding.btnSelectTroupeLeader.isEnabled = false

        val user = User(
            uid = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            displayName = firebaseUser.displayName ?: "",
            role = role
        )

        userRepository.saveUser(user) { success ->
            binding.progressBar.visibility = View.GONE
            if (success) {
                navigateByRole(role)
            } else {
                binding.btnSelectUser.isEnabled = true
                binding.btnSelectTroupeLeader.isEnabled = true
                Toast.makeText(this, "Failed to save role. Try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateByRole(role: String) {
        val intent = when (role) {
            Constants.ROLE_ADMIN -> Intent(this, AdminActivity::class.java)
            else -> Intent(this, MainActivity::class.java).putExtra(Constants.EXTRA_USER_ROLE, role)
        }
        startActivity(intent)
        finish()
    }
}

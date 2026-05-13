package com.kalavidarabalaga.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.kalavidarabalaga.R
import com.kalavidarabalaga.databinding.ActivityLoginBinding
import com.kalavidarabalaga.repository.UserRepository
import com.kalavidarabalaga.ui.admin.AdminActivity
import com.kalavidarabalaga.utils.Constants

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val userRepository = UserRepository()

    private var isSignUpMode = false

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            checkRoleAndNavigate(auth.currentUser!!.uid)
            return
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.btnGoogleSignIn.setOnClickListener { startGoogleSignIn() }
        binding.btnEmailAuth.setOnClickListener { handleEmailAuth() }
        binding.tvToggleMode.setOnClickListener { toggleMode() }
    }

    private fun toggleMode() {
        isSignUpMode = !isSignUpMode
        if (isSignUpMode) {
            binding.btnEmailAuth.text = "Sign Up"
            binding.tvToggleMode.text = "Already have an account? Login"
        } else {
            binding.btnEmailAuth.text = "Login"
            binding.tvToggleMode.text = "Don't have an account? Sign Up"
        }
    }

    private fun handleEmailAuth() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            return
        }
        binding.tilEmail.error = null

        if (password.length < 6) {
            binding.tilPassword.error = "Password must be at least 6 characters"
            return
        }
        binding.tilPassword.error = null

        setLoading(true)

        if (isSignUpMode) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        checkRoleAndNavigate(auth.currentUser!!.uid)
                    } else {
                        setLoading(false)
                        Toast.makeText(this, task.exception?.message ?: "Sign up failed", Toast.LENGTH_LONG).show()
                    }
                }
        } else {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        checkRoleAndNavigate(auth.currentUser!!.uid)
                    } else {
                        setLoading(false)
                        Toast.makeText(this, task.exception?.message ?: "Login failed", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun startGoogleSignIn() {
        setLoading(true)
        startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
    }

    @Deprecated("Using onActivityResult for Google Sign-In compatibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                setLoading(false)
                Toast.makeText(this, getString(R.string.sign_in_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    checkRoleAndNavigate(auth.currentUser!!.uid)
                } else {
                    setLoading(false)
                    Toast.makeText(this, getString(R.string.sign_in_failed), Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkRoleAndNavigate(uid: String) {
        userRepository.getUserRole(uid) { role ->
            if (role.isNullOrBlank()) {
                startActivity(Intent(this, RoleSelectionActivity::class.java))
            } else {
                navigateByRole(role)
            }
            finish()
        }
    }

    private fun navigateByRole(role: String) {
        val intent = when (role) {
            Constants.ROLE_ADMIN -> Intent(this, AdminActivity::class.java)
            else -> Intent(this, MainActivity::class.java).putExtra(Constants.EXTRA_USER_ROLE, role)
        }
        startActivity(intent)
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnGoogleSignIn.isEnabled = !loading
        binding.btnEmailAuth.isEnabled = !loading
        binding.tvToggleMode.isEnabled = !loading
    }
}

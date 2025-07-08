package com.example.project180.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.project180.Model.AuthUser
import com.example.project180.R
import com.example.project180.ViewModel.LoginResult
import com.example.project180.ViewModel.LoginViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    private lateinit var viewModel: LoginViewModel
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        // Initialize views
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.submitButton)
        val registerTextView = findViewById<TextView>(R.id.registerTextView)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (!validateInputs(email, password)) {
                return@setOnClickListener
            }

            viewModel.login(AuthUser(email, password), this)
        }

        registerTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }


        viewModel.loginResult.observe(this) { result ->
            // Re-enable button regardless of result
            findViewById<Button>(R.id.submitButton).isEnabled = true

            when (result) {
                is LoginResult.Success -> {
                    navigateBasedOnRole(result.user.role)
                }
                is LoginResult.Error -> {
                    showError(result.message)
                    // Clear password field on error
                    passwordEditText.text?.clear()
                }
            }
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true

        when {
            email.isEmpty() -> {
                emailEditText.error = "Email is required"
                isValid = false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                emailEditText.error = "Invalid email format"
                isValid = false
            }
            else -> emailEditText.error = null
        }

        when {
            password.isEmpty() -> {
                passwordEditText.error = "Password is required"
                isValid = false
            }
            password.length < 6 -> {
                passwordEditText.error = "Password too short (min 6 characters)"
                isValid = false
            }
            else -> passwordEditText.error = null
        }



        return isValid
    }

    private fun navigateBasedOnRole(role: String) {
        when (role) {
            "Vendeur", "SUPERADMIN" -> {
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            }
            "Client" -> {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            else -> {
                showError("Unauthorized role")
            }
        }
    }

    private fun showError(message: String) {
        Snackbar.make(
            findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG
        ).show()
    }
}
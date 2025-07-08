package com.example.project180.Activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.project180.Model.UserRequest
import com.example.project180.R
import com.example.project180.Util.UserPreferences
import com.example.project180.ViewModel.UserViewModel
import com.example.project180.databinding.ActivityModifierProfilBinding
import com.google.android.material.snackbar.Snackbar
import java.io.File

class ModifierProfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityModifierProfilBinding
    private lateinit var userViewModel: UserViewModel
    private var selectedImageUri: Uri? = null
    private var currentUserId: Int = -1

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = uri
            binding.profileImage.setImageURI(uri)
            binding.imageErrorText.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityModifierProfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialiser le ViewModel
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        // Récupérer les infos de l'utilisateur connecté
        val userInfo = UserPreferences.getUserInfo(this)
        currentUserId = userInfo?.id ?: -1

        if (currentUserId != -1) {
            userViewModel.getUserById(currentUserId)
        }

        // Observer les données utilisateur
        userViewModel.selectedUser.observe(this, Observer { user ->
            user?.let {
                populateUserData(it)
            }
        })

        setupUI()
        setupListeners()
    }

    private fun populateUserData(user: UserRequest) {
        binding.apply {
            nomEditText.setText(user.nom)
            prenomEditText.setText(user.prenom)
            telephoneEditText.setText(user.telephone)
            emailEditText.setText(user.email)

            try {
                val base64String = user.imageDto?.imageBase64
                if (!base64String.isNullOrBlank()) {
                    val cleanBase64 = base64String.substringAfterLast(",")
                    val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    profileImage.setImageBitmap(bitmap)
                } else {
                    profileImage.setImageResource(R.drawable.user_image)
                }
            } catch (e: Exception) {
                Log.e("ImageError", "Erreur décodage image", e)
                profileImage.setImageResource(R.drawable.user_image)
            }
        }
    }

    private fun setupUI() {
        listOf(
            binding.nomEditText,
            binding.prenomEditText,
            binding.telephoneEditText,
            binding.emailEditText
        ).forEach { editText ->
            editText.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus && editText.text?.isNotEmpty() == true) {
                    editText.error = null
                }
            }
        }
    }

    private fun setupListeners() {
        binding.editProfileButton.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.submitButton.setOnClickListener {
            if (validateForm()) {
                updateUserProfile()
            }
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun validateForm(): Boolean {
        val nomText = binding.nomEditText.text.toString().trim()
        val prenomText = binding.prenomEditText.text.toString().trim()
        val telephoneText = binding.telephoneEditText.text.toString().trim()
        val emailText = binding.emailEditText.text.toString().trim()

        var isValid = true

        // Validation du nom
        if (nomText.isEmpty()) {
            binding.nomEditText.error = "Le nom est requis"
            isValid = false
        } else if (!nomText.matches(Regex("^[a-zA-ZéèàêâîïçÉÈÀÊÂÎÏÇ\\s]+\$"))) {
            binding.nomEditText.error = "Nom invalide"
            isValid = false
        }

        // Validation du prénom
        if (prenomText.isEmpty()) {
            binding.prenomEditText.error = "Le prénom est requis"
            isValid = false
        } else if (!prenomText.matches(Regex("^[a-zA-ZéèàêâîïçÉÈÀÊÂÎÏÇ\\s]+\$"))) {
            binding.prenomEditText.error = "Prénom invalide"
            isValid = false
        }

        // Validation du téléphone
        if (telephoneText.isEmpty()) {
            binding.telephoneEditText.error = "Le téléphone est requis"
            isValid = false
        } else if (!telephoneText.matches(Regex("^[0-9]{10}\$"))) {
            binding.telephoneEditText.error = "Téléphone invalide (10 chiffres)"
            isValid = false
        }

        // Validation de l'email
        if (emailText.isEmpty()) {
            binding.emailEditText.error = "L'email est requis"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            binding.emailEditText.error = "Email invalide"
            isValid = false
        }

        return isValid
    }

    private fun updateUserProfile() {
        val nomText = binding.nomEditText.text.toString().trim()
        val prenomText = binding.prenomEditText.text.toString().trim()
        val telephoneText = binding.telephoneEditText.text.toString().trim()
        val emailText = binding.emailEditText.text.toString().trim()
        val passwordText = binding.passwordEditText.text.toString().trim()

        val updatedUser = UserRequest(
            id = currentUserId,
            telephone = telephoneText,
            nom = nomText,
            prenom = prenomText,
            email = emailText,
            password = passwordText,
            role = "",
            imageDto = null
        )

        val imageFile = selectedImageUri?.let { uriToFile(it) }
        userViewModel.modifierUser(updatedUser, imageFile)
        userViewModel.modificationResult.observe(this) { success ->
            if (success) {
                showSuccess("Profil mis à jour avec succès")
                UserPreferences.clearUserInfo(this)
                val intent = Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            } else {
                showError("Échec de la mise à jour du profil")
            }
        }
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("selected_image", ".jpg", cacheDir)
        tempFile.outputStream().use { outputStream ->
            inputStream?.copyTo(outputStream)
        }
        return tempFile
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showSuccess(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}

package com.example.project180.Activity


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.project180.Model.ItemsModel
import com.example.project180.Model.UserRequest
import com.example.project180.R
import com.example.project180.ViewModel.RegisterViewModel
import com.google.android.material.snackbar.Snackbar

import com.google.android.material.textfield.TextInputEditText
import java.io.File

class RegisterActivity : AppCompatActivity() {

    private val viewModel: RegisterViewModel by viewModels()
    private var selectedImageUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = uri
            findViewById<ImageView>(R.id.profileImage).setImageURI(uri)
            findViewById<TextView>(R.id.imageErrorText).visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val nom = findViewById<TextInputEditText>(R.id.nomEditText)
        val prenom = findViewById<TextInputEditText>(R.id.prenomEditText)
        val telephone = findViewById<TextInputEditText>(R.id.telephoneEditText)
        val email = findViewById<TextInputEditText>(R.id.emailEditText)
        val password = findViewById<TextInputEditText>(R.id.passwordEditText)
        val roleDropdown = findViewById<AutoCompleteTextView>(R.id.roleDropdown)
        val submitButton = findViewById<Button>(R.id.submitButton)
        val loginTextView = findViewById<TextView>(R.id.recoveryTextView)
        val uploadImageBtn = findViewById<ImageButton>(R.id.editProfileButton)
        val imageErrorText = findViewById<TextView>(R.id.imageErrorText)

        val roles = listOf("Client", "Vendeur")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles)
        roleDropdown.setAdapter(adapter)

        // Écouteur pour masquer les erreurs quand le texte change
        nom.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus && nom.text?.isNotEmpty() == true) nom.error = null }
        prenom.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus && prenom.text?.isNotEmpty() == true) prenom.error = null }
        telephone.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus && telephone.text?.isNotEmpty() == true) telephone.error = null }
        email.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus && email.text?.isNotEmpty() == true) email.error = null }
        password.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus && password.text?.isNotEmpty() == true) password.error = null }

        // Écouteur pour le rôle
        roleDropdown.setOnItemClickListener { _, _, position, _ ->
            roleDropdown.error = null
            roleDropdown.setText(roles[position], false)
        }

        loginTextView.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        uploadImageBtn.setOnClickListener {
            pickImage.launch("image/*")
        }

        submitButton.setOnClickListener {
            val nomText = nom.text.toString().trim()
            val prenomText = prenom.text.toString().trim()
            val telephoneText = telephone.text.toString().trim()
            val emailText = email.text.toString().trim()
            val passwordText = password.text.toString().trim()
            val roleText = roleDropdown.text.toString().trim()

            var isValid = true

            // Validation du nom
            if (nomText.isEmpty()) {
                nom.error = "Le nom est requis"
                isValid = false
            } else if (!nomText.matches(Regex("^[a-zA-ZéèàêâîïçÉÈÀÊÂÎÏÇ\\s]+\$"))) {
                nom.error = "Nom invalide"
                isValid = false
            }

            // Validation du prénom
            if (prenomText.isEmpty()) {
                prenom.error = "Le prénom est requis"
                isValid = false
            } else if (!prenomText.matches(Regex("^[a-zA-ZéèàêâîïçÉÈÀÊÂÎÏÇ\\s]+\$"))) {
                prenom.error = "Prénom invalide"
                isValid = false
            }

            // Validation du téléphone
            if (telephoneText.isEmpty()) {
                telephone.error = "Le téléphone est requis"
                isValid = false
            } else if (!telephoneText.matches(Regex("^[0-9]{10}\$"))) {
                telephone.error = "Téléphone invalide (10 chiffres)"
                isValid = false
            }

            // Validation de l'email
            if (emailText.isEmpty()) {
                email.error = "L'email est requis"
                isValid = false
            } else if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                email.error = "Email invalide"
                isValid = false
            }

            // Validation du mot de passe
            if (passwordText.isEmpty()) {
                password.error = "Mot de passe requis"
                isValid = false
            } else if (passwordText.length < 6) {
                password.error = "Mot de passe trop court (min 6 caractères)"
                isValid = false
            }

            // Validation du rôle
            if (roleText.isEmpty()) {
                roleDropdown.error = "Le rôle est requis"
                isValid = false
            } else if (!roles.contains(roleText)) {
                roleDropdown.error = "Rôle invalide"
                isValid = false
            }

            // Validation de l'image
            if (selectedImageUri == null) {
                imageErrorText.text = "Veuillez sélectionner une image de profil"
                imageErrorText.visibility = View.VISIBLE
                isValid = false
            } else {
                imageErrorText.visibility = View.GONE
            }

            if (isValid) {
                val imageFile = selectedImageUri?.let { uriToFile(it) }

                imageFile?.let {
                    val user = UserRequest(
                        id = 0,
                        telephone = telephoneText,
                        nom = nomText,
                        prenom = prenomText,
                        email = emailText,
                        password = passwordText,
                        role = roleText,
                        imageDto = null
                    )

                    viewModel.registerUser(user, imageFile)
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    private fun showError(message: String) {

        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show()
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("selected_image", ".jpg", cacheDir)
        tempFile.outputStream().use { outputStream ->
            inputStream?.copyTo(outputStream)
        }
        return tempFile
    }
}












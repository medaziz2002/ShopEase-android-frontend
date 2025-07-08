package com.example.project180.Activity

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.project180.R
import com.example.project180.Util.UserPreferences
import com.example.project180.ViewModel.UserViewModel
import com.example.project180.databinding.ActivityScreenBinding
import com.google.android.material.snackbar.Snackbar  // Importation de Snackbar
import com.google.firebase.messaging.FirebaseMessaging

class ScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScreenBinding
    private lateinit var userViewModel: UserViewModel  // Déclarer le ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialiser le ViewModel
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        val userInfo = UserPreferences.getUserInfo(this)
        if (userInfo != null) {
            userViewModel.getUserById(userInfo.id)
        }

        // Observer les données sélectionnées
        userViewModel.selectedUser.observe(this, Observer { user ->
            user?.let {
                // Mettre à jour l'UI avec les données de l'utilisateur
                binding.nameTextView.text = "${it.nom} ${it.prenom}"
                binding.emailTextView.text = "${it.email}"
                try {
                    val base64String = it.imageDto?.imageBase64
                    if (!base64String.isNullOrBlank()) {
                        val cleanBase64 = base64String.substringAfterLast(",")
                        val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        binding.profileImageView.setImageBitmap(bitmap)
                    } else {
                        binding.profileImageView.setImageResource(com.example.project180.R.drawable.user_image)
                    }
                } catch (e: Exception) {
                    Log.e("ImageError", "Erreur décodage image", e)
                    binding.profileImageView.setImageResource(com.example.project180.R.drawable.user_image)
                }

            }
        })

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.editProfileButton.setOnClickListener {
            val intent = Intent(this, ModifierProfilActivity::class.java)
            startActivity(intent)
        }




        binding.logoutCard.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Déconnexion")
            .setMessage("Êtes-vous sûr de vouloir vous déconnecter ?")
            .setPositiveButton("Déconnecter") { _, _ -> performLogout() }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun performLogout() {
        FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener {
            if (it.isSuccessful) {
                System.out.println("je ssssssss")
                UserPreferences.clearUserInfo(this)
                val intent = Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)


            }
        }

    }
}

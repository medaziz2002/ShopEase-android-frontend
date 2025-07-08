package com.example.project180.Activity

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.project180.Helper.BottomNavigationHelper
import com.example.project180.R
import com.example.project180.Util.UserPreferences
import com.example.project180.ViewModel.CategoryViewModel
import com.example.project180.databinding.ActivityAddCategoryBinding
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileOutputStream

class AddCategoryActivity : AppCompatActivity() {




    private lateinit var binding: ActivityAddCategoryBinding
    val viewModel: CategoryViewModel by viewModels()
    private var selectedImageUri: Uri? = null
    var isEditMode = false
    private var editingCategoryId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val userInfo = UserPreferences.getUserInfo(this)
        val headerView = binding.root.findViewById<View>(R.id.header)
        val notificationBell = headerView.findViewById<FrameLayout>(R.id.notification_bell)
        if (userInfo != null) {
            val informations = headerView.findViewById<TextView>(R.id.textView5)
            val userName = "${userInfo.firstName} ${userInfo.lastName}"
            informations.text = userName
        }
        val bellIcon = notificationBell.getChildAt(0) as ImageView // This gets the ImageView inside the FrameLayout
        bellIcon.setOnClickListener {
            navigateToNotifications()
        }
        findViewById<View>(R.id.editTextText)?.visibility =View.GONE
        val bottomNavView = binding.bottomNavContainer.findViewById<View>(R.id.bottomNavigationView)
        BottomNavigationHelper.setupBottomNavigation(bottomNavView, this)


        checkIfEditMode()
        setupListeners()
        setupObservers()
    }

    private fun navigateToNotifications() {
        val intent = Intent(this, NotificationActivity::class.java)
        startActivity(intent)
    }
    private fun checkIfEditMode() {
        editingCategoryId = intent.getIntExtra("categoryId", -1).takeIf { it != -1 }
        val categoryTitle = intent.getStringExtra("categoryTitle")
        val categoryImageBase64 = intent.getStringExtra("categoryImageBase64")

        if (editingCategoryId != null) {
            isEditMode = true
            binding.startBtn.text = "Modifier la catégorie"
            binding.productTitleInput.setText(categoryTitle)

            categoryImageBase64?.let { base64 ->
                val decodedBytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

                // Créer un fichier temporaire pour l'image
                val tempFile = File.createTempFile("image", ".jpg", cacheDir)
                FileOutputStream(tempFile).use { out ->
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, out)
                }
                selectedImageUri = Uri.fromFile(tempFile)

                val imageView = ImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        resources.getDimensionPixelSize(R.dimen.image_preview_size),
                        resources.getDimensionPixelSize(R.dimen.image_preview_size)
                    ).apply { marginEnd = 16 }
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setImageBitmap(bitmap)
                }

                binding.imagesContainer.removeAllViews()
                binding.imagesContainer.addView(imageView)
            }
        }
    }

    private fun setupListeners() {
        binding.addImagesButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        binding.startBtn.setOnClickListener {
            val title = binding.productTitleInput.text.toString().trim()
            val titleInput = binding.productTitleInput
            val imageErrorText = binding.imageErrorText
            var isValid = true

            if (title.isEmpty()) {
                titleInput.error = "Le titre est requis"
                isValid = false
            } else if (!title.matches(Regex("^[a-zA-ZéèàêâîïçÉÈÀÊÂÎÏÇ\\s]+\$"))) {
                titleInput.error = "Titre invalide"
                isValid = false
            } else {
                titleInput.error = null
            }

            if (selectedImageUri == null) {
                imageErrorText.text = "Veuillez sélectionner une image"
                imageErrorText.visibility = View.VISIBLE
                isValid = false
            } else {
                imageErrorText.visibility = View.GONE
            }

            if (isValid) {
                val imageFile = uriToFile(selectedImageUri!!)

                if (isEditMode && editingCategoryId != null) {
                    viewModel.updateCategory(editingCategoryId!!, title, imageFile)
                } else {
                    viewModel.createCategory(title, imageFile)
                }
            }
        }
    }

    private fun setupObservers() {
        if (isEditMode) {
            viewModel.updateStatus.observe(this) { success ->
                if (success) {
                    Snackbar.make(binding.root, "Catégorie modifiée avec succès !", Snackbar.LENGTH_LONG).show()
                } else {
                    Snackbar.make(binding.root, "Erreur lors de la modification de la catégorie", Snackbar.LENGTH_LONG).show()

                }
            }
        } else {
            viewModel.uploadStatus.observe(this) { success ->
                if (success) {
                    Snackbar.make(binding.root, "Catégorie ajoutée avec succès !", Snackbar.LENGTH_LONG).show()

                    finish()
                } else {
                    Snackbar.make(binding.root, "Erreur lors de l'ajout de la catégorie", Snackbar.LENGTH_LONG).show()

                }
            }
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Snackbar.make(binding.root, "Erreur : $it", Snackbar.LENGTH_LONG).show()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                selectedImageUri = uri
                binding.imagesContainer.removeAllViews()
                val imageView = ImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        resources.getDimensionPixelSize(R.dimen.image_preview_size),
                        resources.getDimensionPixelSize(R.dimen.image_preview_size)
                    ).apply { marginEnd = 16 }
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    setImageURI(uri)
                }
                binding.imagesContainer.addView(imageView)
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

    companion object {
        private const val PICK_IMAGE_REQUEST = 1001
    }
}

package com.example.project180.Activity

import SizeListAdapter
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project180.Adapter.CategoryAdapter
import com.example.project180.Adapter.SelectedImagesAdapter

import com.example.project180.Helper.BottomNavigationHelper
import com.example.project180.Model.CategoryDto
import com.example.project180.Model.ItemsModel
import com.example.project180.Model.ProductDto
import com.example.project180.R
import com.example.project180.Util.UserPreferences
import com.example.project180.ViewModel.CategoryViewModel
import com.example.project180.ViewModel.ProductViewModel
import com.example.project180.databinding.ActivityAddProductBinding
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import java.io.File

class AddProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddProductBinding
    private val categoryViewModel: CategoryViewModel by viewModels()
    private val productViewModel: ProductViewModel by viewModels()
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var sizeAdapter: SizeListAdapter
    private lateinit var selectedImagesAdapter: SelectedImagesAdapter
    private var selectedCategory: CategoryDto? = null
    private val selectedImages = mutableListOf<Uri>()
    private var selectedImageFiles = mutableListOf<File>()
    private val sizeList = mutableListOf<String>()
    private val selectedSizes = mutableListOf<String>()
    private var isEditMode = false
    private var productToEdit: ItemsModel? = null
    private var existingImageUrls = mutableListOf<String>()
    private var positionToReplace: Int = -1
    private val imagesToDelete = mutableListOf<Int>()

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { intent ->
                if (intent.clipData != null) {
                    val count = intent.clipData?.itemCount ?: 0
                    for (i in 0 until count) {
                        intent.clipData?.getItemAt(i)?.uri?.let { uri ->
                            addSelectedImage(uri)
                        }
                    }
                } else {
                    intent.data?.let { uri ->
                        addSelectedImage(uri)
                    }
                }
            }
        }
    }

    private val replaceImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && positionToReplace != -1) {
            result.data?.data?.let { uri ->
                replaceSelectedImage(positionToReplace, uri)
                positionToReplace = -1
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        productToEdit = intent.getParcelableExtra("productToEdit")
        isEditMode = productToEdit != null

        val bottomNavView = binding.bottomNavContainer.findViewById<View>(R.id.bottomNavigationView)
        BottomNavigationHelper.setupBottomNavigation(bottomNavView, this)
        setupHeader()
        setupRecyclerView()
        setupSizeRecyclerView()
        setupSelectedImagesRecyclerView()
        setupObservers()
        setupClickListeners()
        showSizeSelection(false)
        binding.selectedImagesRecyclerView.visibility = View.GONE
        findViewById<View>(R.id.editTextText)?.visibility = View.GONE

        if (isEditMode) {
            loadProductData()
            binding.startBtn.text = "Mettre à jour le produit"
        } else {
            binding.startBtn.text = "Ajouter le produit"
        }

        val headerView = binding.root.findViewById<View>(R.id.header)
        val notificationBell = headerView.findViewById<FrameLayout>(R.id.notification_bell)
        val bellIcon = notificationBell.getChildAt(0) as ImageView
        bellIcon.setOnClickListener {
            navigateToNotifications()
        }
        categoryViewModel.fetchCategories()
    }

    private fun navigateToNotifications() {
        val intent = Intent(this, NotificationActivity::class.java)
        startActivity(intent)
    }

    private fun loadProductData() {
        productToEdit?.let { product ->
            Log.d("ProductDebug", "Product loaded: ${Gson().toJson(product)}")

            binding.productTitleInput.setText(product.title)
            binding.productDescriptionInput.setText(product.description)
            binding.productPriceInput.setText(product.price.toString())

            val discount = if (product.discountedPrice > 0 && product.discountedPrice < product.price) {
                ((product.price - product.discountedPrice) / product.price * 100).toInt()
            } else {
                0
            }
            binding.productDiscountInput.setText(discount.toString())
            binding.productStockInput.setText(product.stock.toString())

            product.image.forEach { imageDto ->
                val imageBase64 = imageDto.imageBase64

                // Vérifier si l'image est une URL ou une chaîne Base64
                val uri = if (imageBase64?.startsWith("http") == true) {
                    Uri.parse(imageBase64)  // Si c'est une URL, on parse directement l'URL
                } else {
                    Uri.parse("data:image/jpeg;base64,$imageBase64")  // Si c'est une image Base64, on ajoute le préfixe
                }

                selectedImages.add(uri)  // Ajouter l'uri à la liste des images sélectionnées
                existingImageUrls.add(imageBase64 ?: "")  // Ajouter l'image de type Base64 ou l'URL dans la liste existante
            }



            binding.selectedImagesRecyclerView.visibility = View.VISIBLE
            selectedImagesAdapter.notifyDataSetChanged()

            product.categoryId?.let { categoryId ->
                categoryViewModel.categories.observe(this, Observer { categories ->
                    categories?.let {
                        val category = it.find { cat -> cat.id == categoryId }
                        category?.let { selectedCat ->
                            selectedCategory = selectedCat
                            val position = it.indexOfFirst { cat -> cat.id == categoryId }
                            if (position != -1) {
                                categoryAdapter.selectedPosition = position
                                categoryAdapter.notifyItemChanged(position)
                                handleCategorySelection(selectedCat)

                                when (selectedCat.titre?.lowercase()) {
                                    "nourriture", "repas" -> {
                                        Log.d("WeightDebug", "Fetching weights for food category")
                                        productViewModel.fetchWeights()
                                        productViewModel.weights.observe(this@AddProductActivity) { weightDto ->
                                            weightDto?.weights?.let { apiWeightList ->
                                                this.sizeList.clear()
                                                this.sizeList.addAll(apiWeightList)
                                                sizeAdapter.notifyDataSetChanged()

                                                product.weight?.forEach { weight ->
                                                    val normalizedWeight = weight.trim().uppercase()
                                                    val foundWeight = apiWeightList.find {
                                                        it.trim().uppercase() == normalizedWeight
                                                    }

                                                    if (foundWeight != null) {
                                                        if (!selectedSizes.contains(foundWeight)) {
                                                            selectedSizes.add(foundWeight)
                                                        }
                                                    }
                                                }

                                                sizeAdapter.setSelectedItems(selectedSizes)
                                            }
                                        }
                                    }
                                    "vêtements", "vetement", "habillement" -> {
                                        productViewModel.fetchSizes()
                                        productViewModel.sizes.observe(this@AddProductActivity) { sizeDto ->
                                            sizeDto?.sizes?.let { apiSizeList ->
                                                this.sizeList.clear()
                                                this.sizeList.addAll(apiSizeList)
                                                sizeAdapter.notifyDataSetChanged()

                                                product.size?.forEach { size ->
                                                    if (apiSizeList.contains(size) && !selectedSizes.contains(size)) {
                                                        selectedSizes.add(size)
                                                    }
                                                }
                                                sizeAdapter.setSelectedItems(selectedSizes)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                })
            }
        } ?: run {
            Log.e("ProductDebug", "No product to edit found")
        }
    }

    private fun setupHeader() {
        UserPreferences.getUserInfo(this)?.let { userInfo ->
            binding.header.textView5.text = "${userInfo.firstName} ${userInfo.lastName}"
        }
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(mutableListOf()).apply {
            onItemClick = { category ->
                selectedCategory = category
                handleCategorySelection(category)
            }
        }

        binding.categoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(
                this@AddProductActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = categoryAdapter
        }
    }

    private fun setupSizeRecyclerView() {
        sizeAdapter = SizeListAdapter(sizeList,allowMultipleSelection = true).apply {
            onItemClick = { size ->
                if (selectedSizes.contains(size)) {
                    selectedSizes.remove(size)
                } else {
                    selectedSizes.add(size)
                }
                notifyDataSetChanged()
            }
        }

        binding.sizeList.apply {
            layoutManager = LinearLayoutManager(
                this@AddProductActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = sizeAdapter
        }
    }

    private fun setupSelectedImagesRecyclerView() {
        Log.d("ImageDebug", "Initialisation du RecyclerView des images sélectionnées")

        selectedImagesAdapter = SelectedImagesAdapter(
            selectedImages,
            onDeleteClick = { position ->
                Log.d("ImageDebug", "Clic sur suppression de l'image à la position $position")

                if (position in selectedImages.indices) {
                    val existingImagesCount = productToEdit?.image?.size ?: 0
                    Log.d("ImageDebug", "Nombre d'images existantes: $existingImagesCount")

                    if (position < existingImagesCount) {
                        Log.d("ImageDebug", "Tentative de suppression d'une image existante")

                        productToEdit?.image?.get(position)?.id?.let { imageId ->
                            Log.d("ImageDebug", "ID de l'image à supprimer: $imageId")

                            if (!imagesToDelete.contains(imageId.toInt())) {
                                imagesToDelete.add(imageId.toInt())
                                Log.d("ImageDebug", "ID $imageId ajouté à la liste de suppression. Liste actuelle: $imagesToDelete")
                            } else {
                                Log.d("ImageDebug", "ID $imageId déjà présent dans la liste de suppression")
                            }
                        } ?: run {
                            Log.e("ImageDebug", "Aucun ID trouvé pour l'image à la position $position")
                        }
                    } else {
                        val filePosition = position - existingImagesCount
                        Log.d("ImageDebug", "Tentative de suppression d'une nouvelle image (position fichier: $filePosition)")

                        if (filePosition in selectedImageFiles.indices) {
                            selectedImageFiles.removeAt(filePosition)
                            Log.d("ImageDebug", "Fichier temporaire supprimé à la position $filePosition")
                        } else {
                            Log.e("ImageDebug", "Position de fichier invalide: $filePosition")
                        }
                    }

                    selectedImages.removeAt(position)
                    Log.d("ImageDebug", "Image retirée de la liste d'affichage. Nouvelle taille: ${selectedImages.size}")

                    selectedImagesAdapter.notifyItemRemoved(position)
                    Log.d("ImageDebug", "Notification d'élément supprimé envoyée à l'adapter")

                    if (selectedImages.isEmpty()) {
                        binding.selectedImagesRecyclerView.visibility = View.GONE
                        Log.d("ImageDebug", "Aucune image restante - masquage du RecyclerView")
                    }
                } else {
                    Log.e("ImageDebug", "Position invalide pour suppression: $position")
                }
            },
            onReplaceClick = { position ->
                Log.d("ImageDebug", "Clic sur remplacement de l'image à la position $position")

                if (position < existingImageUrls.size) {
                    Log.d("ImageDebug", "Tentative de remplacement d'une image existante - non autorisé")
                    Snackbar.make(binding.root,
                        "Vous ne pouvez pas remplacer cette image. Ajoutez une nouvelle image et supprimez celle-ci si nécessaire.",
                        Snackbar.LENGTH_LONG).show()
                } else {
                    Log.d("ImageDebug", "Lancement du sélecteur d'image pour remplacement")
                    launchImageReplacer(position)
                }
            }
        )

        binding.selectedImagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(
                this@AddProductActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = selectedImagesAdapter
            Log.d("ImageDebug", "RecyclerView configuré avec ${selectedImages.size} images")
        }

        Log.d("ImageDebug", "Configuration terminée. Visibilité: ${binding.selectedImagesRecyclerView.visibility}")
    }


    private fun handleCategorySelection(category: CategoryDto) {
        Snackbar.make(binding.root, "Catégorie: ${category.titre}", Snackbar.LENGTH_SHORT).show()
        selectedCategory = category

        when (category.titre?.lowercase()) {
            "nourriture", "repas" -> {
                showSizeSelection(true)
                productViewModel.fetchWeights()
                binding.textViewSizesTitle.text = "Poids disponibles"
            }
            "vêtements", "vetement", "habillement" -> {
                showSizeSelection(true)
                productViewModel.fetchSizes()
                binding.textViewSizesTitle.text = "Tailles disponibles"
            }
            else -> {
                showSizeSelection(false)
                selectedSizes.clear()
            }
        }
    }

    private fun showSizeSelection(show: Boolean) {
        binding.sizeList.visibility = if (show) View.VISIBLE else View.GONE
        binding.textViewSizesTitle.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun setupObservers() {
        categoryViewModel.categories.observe(this, Observer { categories ->
            categories?.let {
                categoryAdapter.items.clear()
                categoryAdapter.items.addAll(it)
                categoryAdapter.notifyDataSetChanged()

                if (isEditMode) {
                    productToEdit?.let { product ->
                        val category = it.find { cat -> cat.id == product.categoryId }
                        category?.let {
                            selectedCategory = it
                            handleCategorySelection(it)
                        }
                    }
                }
            }
        })

        productViewModel.weights.observe(this, Observer { weightDto ->
            weightDto?.weights?.let { weights ->
                sizeList.clear()
                sizeList.addAll(weights)
                sizeAdapter.notifyDataSetChanged()
                binding.textViewSizesTitle.text = "Poids disponibles"

                if (isEditMode) {
                    productToEdit?.weight?.forEach { weight ->
                        val index = sizeList.indexOf(weight)
                        if (index != -1) {
                            if (!selectedSizes.contains(weight)) {
                                selectedSizes.add(weight)
                            }
                        }
                    }
                    sizeAdapter.notifyDataSetChanged()
                }
            }
        })

        productViewModel.sizes.observe(this, Observer { sizeDto ->
            sizeDto?.sizes?.let { sizes ->
                sizeList.clear()
                sizeList.addAll(sizes)
                sizeAdapter.notifyDataSetChanged()
                binding.textViewSizesTitle.text = "Tailles disponibles"

                if (isEditMode) {
                    productToEdit?.size?.forEach { size ->
                        val index = sizeList.indexOf(size)
                        if (index != -1) {
                            if (!selectedSizes.contains(size)) {
                                selectedSizes.add(size)
                            }
                        }
                    }
                    sizeAdapter.notifyDataSetChanged()
                }
            }
        })

        productViewModel.uploadStatus.observe(this, Observer { success ->
            if (success) {
                val message = if (isEditMode) "Produit mis à jour!" else "Produit ajouté!"
                Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
                    .addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(snackbar: Snackbar?, event: Int) {
                            super.onDismissed(snackbar, event)
                            val intent = Intent(this@AddProductActivity, ListProductActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    })
                    .show()
            }
        })

        productViewModel.updateStatus.observe(this, Observer { success ->
            if (success) {
                val message = "Produit mis à jour!"
                Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
                    .addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(snackbar: Snackbar?, event: Int) {
                            super.onDismissed(snackbar, event)
                            val intent = Intent(this@AddProductActivity, ListProductActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    })
                    .show()
            }
        })

        productViewModel.error.observe(this, Observer { error ->
            error?.let { Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show() }
        })
    }

    private fun setupClickListeners() {
        binding.addImagesButton.setOnClickListener {
            openImagePicker()
        }

        binding.startBtn.setOnClickListener {
            if (isEditMode) {
                validateAndUpdateProduct()
            } else {
                validateAndSubmitProduct()
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            type = "image/*"
        }
        imagePickerLauncher.launch(intent)
    }

    private fun launchImageReplacer(position: Int) {
        positionToReplace = position
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
        }
        replaceImageLauncher.launch(intent)
    }

    private fun addSelectedImage(uri: Uri) {
        uri.toFile()?.let { file ->
            selectedImages.add(uri)
            selectedImageFiles.add(file)
            selectedImagesAdapter.notifyItemInserted(selectedImages.size - 1)
            if (selectedImages.isNotEmpty()) {
                binding.selectedImagesRecyclerView.visibility = View.VISIBLE
            }
        }
    }

    private fun replaceSelectedImage(position: Int, newUri: Uri) {
        if (position in selectedImages.indices) {
            if (position >= existingImageUrls.size) {
                newUri.toFile()?.let { newFile ->
                    selectedImages[position] = newUri
                    val filePosition = position - existingImageUrls.size
                    if (filePosition in selectedImageFiles.indices) {
                        selectedImageFiles[filePosition] = newFile
                    }
                    selectedImagesAdapter.notifyItemChanged(position)
                }
            }
        }
    }

    private fun validateAndSubmitProduct() {
        val title = binding.productTitleInput.text.toString().trim()
        val description = binding.productDescriptionInput.text.toString().trim()
        val priceText = binding.productPriceInput.text.toString().trim()
        val discountText = binding.productDiscountInput.text.toString().trim()
        val stockText = binding.productStockInput.text.toString().trim()

        var isValid = true

        if (title.isEmpty()) {
            binding.productTitleInput.error = "Titre obligatoire"
            isValid = false
        }

        if (description.isEmpty()) {
            binding.productDescriptionInput.error = "Description obligatoire"
            isValid = false
        }

        val price = try {
            priceText.toDouble().also {
                if (it <= 0) {
                    binding.productPriceInput.error = "Prix doit être > 0"
                    isValid = false
                }
            }
        } catch (e: Exception) {
            binding.productPriceInput.error = "Prix invalide"
            isValid = false
            0.0
        }

        val discount = try {
            discountText.takeIf { it.isNotEmpty() }?.toDouble() ?: 0.0
        } catch (e: Exception) {
            binding.productDiscountInput.error = "Remise invalide"
            isValid = false
            0.0
        }

        val stock = try {
            stockText.toInt().also {
                if (it < 0) {
                    binding.productStockInput.error = "Stock invalide"
                    isValid = false
                }
            }
        } catch (e: Exception) {
            binding.productStockInput.error = "Stock doit être un nombre"
            isValid = false
            0
        }

        if (!isValid) {
            return
        }

        if (selectedCategory == null) {
            Snackbar.make(binding.root, "Sélectionnez une catégorie", Snackbar.LENGTH_LONG).show()
            return
        }

        if (selectedImages.size < 2) {
            Snackbar.make(binding.root, "Minimum 2 images requises", Snackbar.LENGTH_LONG).show()
            return
        }

        val needsSizeSelection = selectedCategory?.titre?.lowercase() in
                listOf("nourriture", "repas", "vêtements", "vetement", "habillement")

        if (needsSizeSelection && selectedSizes.isEmpty()) {
            Snackbar.make(binding.root, "Sélectionnez au moins une taille/poids", Snackbar.LENGTH_LONG).show()
            return
        }

        UserPreferences.getUserInfo(this)?.let { userInfo ->
            val productDto = when {
                selectedCategory?.titre?.lowercase() in listOf("vêtements", "vetement", "habillement") -> {
                    ProductDto(
                        title = title,
                        description = description,
                        price = price,
                        sellerId = userInfo.id,
                        discountPercentage = discount,
                        stock = stock,
                        categoryId = selectedCategory?.id,
                        size = selectedSizes,
                        weight = null
                    )
                }
                selectedCategory?.titre?.lowercase() in listOf("nourriture", "repas") -> {
                    ProductDto(
                        title = title,
                        description = description,
                        price = price,
                        sellerId = userInfo.id,
                        discountPercentage = discount,
                        stock = stock,
                        categoryId = selectedCategory?.id,
                        weight = selectedSizes,
                        size = null
                    )
                }
                else -> {
                    ProductDto(
                        title = title,
                        description = description,
                        price = price,
                        sellerId = userInfo.id,
                        discountPercentage = discount,
                        stock = stock,
                        categoryId = selectedCategory?.id,
                        size = null,
                        weight = null
                    )
                }
            }

            val validFiles = selectedImageFiles.filter { file ->
                val isValidFile = file.exists() && file.length() > 0
                if (!isValidFile) {
                    Log.e("FileValidation", "Fichier invalide ou vide: ${file.absolutePath}")
                }
                isValidFile
            }

            if (validFiles.size < 2) {
                Snackbar.make(binding.root, "Certains fichiers sont invalides", Snackbar.LENGTH_LONG).show()
                return@let
            }

            Log.d("FileUpload", "Envoi de ${validFiles.size} fichiers valides")
            productViewModel.createProduct(productDto, validFiles)

        } ?: run {
            Snackbar.make(binding.root, "Erreur utilisateur", Snackbar.LENGTH_LONG).show()
        }
    }


    private fun validateAndUpdateProduct() {
        productToEdit?.let { product ->
            // Vérification de la sélection des images
            if (selectedImages.size < 2) {
                Snackbar.make(binding.root, "Minimum 2 images requises", Snackbar.LENGTH_LONG).show()
                return
            }

            // Vérification de la sélection d'une catégorie
            if (selectedCategory == null) {
                Snackbar.make(binding.root, "Sélectionnez une catégorie", Snackbar.LENGTH_LONG).show()
                return
            }

            // Vérification de la nécessité de sélectionner une taille/poids selon la catégorie
            val needsSizeSelection = selectedCategory?.titre?.lowercase() in
                    listOf("nourriture", "repas", "vêtements", "vetement", "habillement")

            if (needsSizeSelection && selectedSizes.isEmpty()) {
                Snackbar.make(binding.root, "Sélectionnez au moins une taille/poids", Snackbar.LENGTH_LONG).show()
                return
            }

            // Validation des champs du formulaire
            var isValid = true

            val title = binding.productTitleInput.text.toString().trim()
            val description = binding.productDescriptionInput.text.toString().trim()
            val priceText = binding.productPriceInput.text.toString().trim()
            val discountText = binding.productDiscountInput.text.toString().trim()
            val stockText = binding.productStockInput.text.toString().trim()

            if (title.isEmpty()) {
                binding.productTitleInput.error = "Titre obligatoire"
                isValid = false
            }

            if (description.isEmpty()) {
                binding.productDescriptionInput.error = "Description obligatoire"
                isValid = false
            }

            if (priceText.isEmpty()) {
                binding.productPriceInput.error = "Prix obligatoire"
                isValid = false
            }

            if (stockText.isEmpty()) {
                binding.productStockInput.error = "Stock obligatoire"
                isValid = false
            }

            // Si un champ est invalide, on arrête l'exécution
            if (!isValid) return

            // Récupération des informations utilisateur
            UserPreferences.getUserInfo(this)?.let { userInfo ->
                val productDto = when {
                    selectedCategory?.titre?.lowercase() in listOf("vêtements", "vetement", "habillement") -> {
                        ProductDto(
                            title = title,
                            description = description,
                            price = priceText.toDoubleOrNull() ?: 0.0,
                            sellerId = userInfo.id,
                            discountPercentage = discountText.toDoubleOrNull() ?: 0.0,
                            stock = stockText.toIntOrNull() ?: 0,
                            categoryId = selectedCategory?.id,
                            size = selectedSizes,
                            weight = null
                        )
                    }
                    selectedCategory?.titre?.lowercase() in listOf("nourriture", "repas") -> {
                        ProductDto(
                            title = title,
                            description = description,
                            price = priceText.toDoubleOrNull() ?: 0.0,
                            sellerId = userInfo.id,
                            discountPercentage = discountText.toDoubleOrNull() ?: 0.0,
                            stock = stockText.toIntOrNull() ?: 0,
                            categoryId = selectedCategory?.id,
                            weight = selectedSizes,
                            size = null
                        )
                    }
                    else -> {
                        ProductDto(
                            title = title,
                            description = description,
                            price = priceText.toDoubleOrNull() ?: 0.0,
                            sellerId = userInfo.id,
                            discountPercentage = discountText.toDoubleOrNull() ?: 0.0,
                            stock = stockText.toIntOrNull() ?: 0,
                            categoryId = selectedCategory?.id,
                            size = null,
                            weight = null
                        )
                    }
                }

                // Validation des fichiers d'images
                val validFiles = selectedImageFiles.filter { file ->
                    val isValid = file.exists() && file.length() > 0
                    if (!isValid) {
                        Log.e("FileValidation", "Fichier invalide ou vide: ${file.absolutePath}")
                    }
                    isValid
                }


                Log.d("FileUpload", "Envoi de ${validFiles.size} fichiers valides")
                // Appel de la méthode pour mettre à jour le produit
                productViewModel.updateProduct(
                    product.id,
                    productDto,
                    validFiles.toList(), // Liste des nouveaux fichiers
                    imagesToDelete.toList() // Liste des IDs à supprimer
                )
            } ?: run {
                Snackbar.make(binding.root, "Erreur utilisateur", Snackbar.LENGTH_LONG).show()
            }
        } ?: run {
            Snackbar.make(binding.root, "Erreur: produit non trouvé", Snackbar.LENGTH_LONG).show()
        }
    }


    private fun Uri.toFile(): File? {
        return try {
            val inputStream = contentResolver.openInputStream(this) ?: return null
            val fileExtension = contentResolver.getType(this)?.split("/")?.last() ?: "jpg"
            val tempFile = File.createTempFile("img_", ".$fileExtension", cacheDir).apply {
                deleteOnExit()
            }

            inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            if (tempFile.exists() && tempFile.length() > 0) {
                tempFile
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("FileConversion", "Erreur conversion URI en fichier", e)
            null
        }
    }
}
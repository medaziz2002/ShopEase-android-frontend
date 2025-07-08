package com.example.project180.Activity

import SizeListAdapter
import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.example.project180.Adapter.PicListAdapter
import com.example.project180.Adapter.ReviewAdapter

import com.example.project180.Model.ItemsModel
import com.example.project180.Model.ReviewDto
import com.example.project180.Util.UserPreferences
import com.example.project180.ViewModel.CartViewModel
import com.example.project180.ViewModel.ProductViewModel
import com.example.project180.ViewModel.ReviewViewModel
import com.example.project180.databinding.ActivityDetailBinding
import com.google.android.material.snackbar.Snackbar

class DetailActivity : BaseActivity() {
    private lateinit var binding: ActivityDetailBinding
    private lateinit var item: ItemsModel
    private var role: String = "Client"
    private var selectedRating = 0f
    private val productViewModel: ProductViewModel by viewModels()
    private val reviewViewModel: ReviewViewModel by viewModels()
    private val cartViewModel: CartViewModel by viewModels()

    private var existingReview: ReviewDto? = null
    private var userId: Int? = null
    private var isInitialLoad = true
    private var isLoadingUserReview = false
    private var hasCheckedReview = false

    private lateinit var sizeAdapter: SizeListAdapter
    private lateinit var reviewAdapter: ReviewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userInfo = UserPreferences.getUserInfo(this)
        userId = userInfo?.id
        role = userInfo?.role ?: "Client"

        getBundle()
        initViews()
        setupObservers()

        if (userInfo?.id != null && !hasCheckedReview) {
            hasCheckedReview = true
            reviewViewModel.hasUserReviewedProduct(item.id, userInfo.id)
            reviewViewModel.fetchReviewsByProductId(item.id, userInfo.id)
        }

        reviewAdapter = ReviewAdapter(emptyList())
        binding.reviewRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.reviewRecyclerView.adapter = reviewAdapter
    }

    private fun setupObservers() {
        reviewViewModel.reviews.observe(this) { reviews ->
            reviewAdapter.updateReviews(reviews ?: emptyList())
            Log.d("ReviewDebug", "Reviews received: ${reviews?.size}")
        }

        reviewViewModel.userHasReviewed.observe(this) { hasReviewed ->
            if (hasReviewed == true && existingReview == null) {
                isLoadingUserReview = true
                reviewViewModel.getUserReviewForProduct(userId!!, item.id)
            } else if (hasReviewed == false) {
                resetReviewFields()
            }
        }

        reviewViewModel.userReview.observe(this) { review ->
            if (isLoadingUserReview) {
                review?.let {
                    loadExistingReview(it)
                }
                isLoadingUserReview = false
            }
        }

        productViewModel.deleteStatus.observe(this) { success ->
            if (success) {
                showMessage("Produit supprimé avec succès")
                finish()
            }
        }


        productViewModel.product.observe(this) { productDto ->
            productDto?.let {
                binding.ratingTxt.text = "%.1f".format(it.rating ?: 0.0)
                item = item.copy(rating = it.rating ?: 0.0)

                Log.d("ProductUpdate", "Product rating updated to: ${it.rating}")
            }
        }
    }

    // Et dans refreshData(), assurez-vous d'appeler getProductById:
    private fun refreshData() {
        userId?.let { userId ->
            reviewViewModel.fetchReviewsByProductId(item.id, userId)

            // Ceci va déclencher l'observer product ci-dessus
            productViewModel.getProductById(item.id)

            reviewViewModel.hasUserReviewedProduct(item.id, userId)
        }
    }

    private fun updateProductInfo(updatedProduct: ItemsModel) {
        // Mettre à jour le rating affiché
        binding.ratingTxt.text = "%.1f".format(updatedProduct.rating)

        // Mettre à jour l'objet item local
        item = updatedProduct

        Log.d("ProductUpdate", "Product rating updated to: ${updatedProduct.rating}")
    }

    private fun resetReviewFields() {
        isInitialLoad = true
        existingReview = null

        binding.ratingBar.rating = 0f
        binding.ratingBar.setIsIndicator(false)
        binding.etMessage.setText("")

        binding.etMessage.apply {
            isFocusable = true
            isFocusableInTouchMode = true
            isEnabled = true
            clearFocus()
        }

        isInitialLoad = false
    }

    private fun loadExistingReview(review: ReviewDto) {
        isInitialLoad = true
        existingReview = review

        if (!binding.etMessage.hasFocus()) {
            binding.etMessage.setText(review.comment ?: "")
        }

        binding.ratingBar.rating = review.rating ?: 0f
        binding.ratingBar.setIsIndicator(false)

        binding.etMessage.apply {
            isFocusable = true
            isFocusableInTouchMode = true
            isEnabled = true
        }

        isInitialLoad = false
    }

    private fun getBundle() {
        item = intent.getParcelableExtra("product") ?: return
        binding.titleTxt.text = item.title
        binding.descriptionTxt.text = item.description
        binding.txtCategory.text = item.categoryName
        binding.txtOriginalPrice.text = "${"%.2f".format(item.price)}€"

        if (item.discountedPrice > 0 && item.discountedPrice < item.price) {
            binding.txtOriginalPrice.apply {
                text = "%.2f€".format(item.price)
                visibility = View.VISIBLE
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }
            binding.txtDiscountedPrice.text = "${"%.2f".format(item.discountedPrice)}€"
        } else {
            binding.txtOriginalPrice.visibility = View.GONE
            binding.txtDiscountedPrice.text = "${"%.2f".format(item.price)}€"
        }

        binding.ratingTxt.text = "%.1f".format(item.rating)
        binding.SellerNameTxt.text = item.sellerName

        if (item.sellerPic.isNotEmpty()) {
            Glide.with(this)
                .load(item.sellerPic)
                .apply(RequestOptions().transform(CenterCrop()))
                .into(binding.picSeller)
        }

        binding.backBtn.setOnClickListener { finish() }
    }

    private fun initViews() {
        if (role == "Vendeur") {
            binding.adminActionsLayout.visibility = View.VISIBLE

            binding.btnEdit.setOnClickListener {
                val intent = Intent(this, AddProductActivity::class.java).apply {
                    putExtra("productToEdit", item)
                }
                startActivity(intent)
            }

            binding.btnDelete.setOnClickListener {
                showDeleteConfirmation()
            }
        }

        val imageBase64List = item.image.mapNotNull { it.imageBase64 }
        if (imageBase64List.isNotEmpty()) {
            Glide.with(this)
                .load("data:image/jpeg;base64,${imageBase64List[0]}")
                .into(binding.picMain)

            binding.picList.adapter = PicListAdapter(imageBase64List, binding.picMain)
            binding.picList.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        }

        val isClient = role == "Client"
        binding.FavIcon.visibility = if (isClient) View.VISIBLE else View.GONE
        binding.AddToCartBtn.visibility = if (isClient) View.VISIBLE else View.GONE
        binding.reviewLayout.visibility = if (isClient) View.VISIBLE else View.GONE
        binding.CartBtn.visibility = if (isClient) View.VISIBLE else View.GONE

        binding.msgToSellerBtn.setOnClickListener {
            val sendIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("sms:${item.sellerTell}")
                putExtra("sms_body", "Bonjour, je suis intéressé par ${item.title}")
            }
            startActivity(sendIntent)
        }

        binding.calToSellerBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${item.sellerTell}")
            }
            startActivity(intent)
        }

        if (isClient) {
            binding.FavIcon.setOnClickListener { /* Gestion favoris */ }

            binding.ratingBar.setOnRatingBarChangeListener { _, newRating, fromUser ->
                if (fromUser && !isInitialLoad && existingReview != null && newRating != existingReview?.rating) {
                    existingReview = existingReview?.copy(rating = newRating)
                    reviewViewModel.updateReviewRating(existingReview!!.id!!, newRating)
                    showMessage("Note mise à jour")
                }
            }
            binding.AddToCartBtn.setOnClickListener {
                val userInfo = UserPreferences.getUserInfo(this)
                if (userInfo?.id == null) {
                    showMessage("Vous devez être connecté pour ajouter au panier")
                    return@setOnClickListener
                }

                val userId = userInfo.id
                Log.d("je suis ","bloqued ici1")
                // Vérification de la sélection pour les vêtements
                when (item.categoryName?.lowercase()) {
                    in listOf("vêtements", "vetement", "habillement") -> {
                        if (sizeAdapter.selectedItems.isEmpty()) {
                            showMessage("Veuillez sélectionner une taille")
                            return@setOnClickListener
                        }
                    }
                    in listOf("nourriture", "repas") -> {
                        if (sizeAdapter.selectedItems.isEmpty()) {
                            showMessage("Veuillez sélectionner un poids")
                            return@setOnClickListener
                        }
                    }
                }
                Log.d("je suis ","bloqued ici2")
                cartViewModel.checkProductInCart(item.id, userId)
                cartViewModel.productInCart.observe(this@DetailActivity) { inCart ->
                    if (inCart == true) {
                        Log.d("je suis ","bloqued if")
                        showMessage("Ce produit est déjà dans votre panier")
                        return@observe
                    }else
                    {
                        Log.d("je suis ","bloqued else")
                        val quantity = sizeAdapter.selectedItems.size.takeIf { it > 0 } ?: 1
                        Log.d("je suis 4",quantity.toString())
                        val sizes = when {
                            item.categoryName?.lowercase() in listOf("vêtements", "vetement", "habillement") -> {
                                sizeAdapter.selectedItems.filter { it.isNotEmpty() }
                            }
                            else -> null
                        }

                        val weights = when {
                            item.categoryName?.lowercase() in listOf("nourriture", "repas") -> {
                                sizeAdapter.selectedItems.filter { it.isNotEmpty() }
                            }
                            else -> null
                        }

                        Log.d("je suis ","hhdh")
                        cartViewModel.addToCart(
                            productId = item.id,
                            userId = userId,
                            quantity = quantity,
                            sizes = sizes?.takeIf { it.isNotEmpty() },
                            weights = weights?.takeIf { it.isNotEmpty() }
                        )
                        showMessage("Produit ajouté au panier")
                    }
                }
            }

            binding.CartBtn.setOnClickListener {
                startActivity(Intent(this, CartActivity::class.java))
            }
            binding.etMessage.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus && existingReview != null && !isInitialLoad) {
                    val currentText = binding.etMessage.text?.toString()?.trim() ?: ""
                    if (currentText != existingReview?.comment?.trim()) {
                        existingReview = existingReview?.copy(comment = currentText)
                        reviewViewModel.updateReview(
                            existingReview!!.id!!,
                            ReviewDto(
                                productId = item.id,
                                userId = userId!!,
                                comment = currentText,
                                rating = binding.ratingBar.rating
                            )
                        )
                        showMessage("Commentaire sauvegardé")
                    }
                }
            }

            binding.btnSend.setOnClickListener {
                val userInfo = UserPreferences.getUserInfo(this)
                if (userInfo?.id == null) {
                    showMessage("Vous devez être connecté pour laisser un avis")
                    return@setOnClickListener
                }

                val comment = binding.etMessage.text?.toString()?.trim() ?: ""
                val rating = binding.ratingBar.rating

                if (rating.toInt() == 0) {
                    showMessage("Veuillez donner une note")
                    return@setOnClickListener
                }

                val reviewDto = ReviewDto(
                    productId = item.id,
                    userId = userInfo.id,
                    comment = comment,
                    rating = rating
                )

                if (existingReview != null) {
                    reviewViewModel.updateReview(existingReview!!.id!!, reviewDto)
                    showMessage("Avis mis à jour")
                } else {
                    reviewViewModel.createReview(reviewDto)
                    showMessage("Avis créé")
                    existingReview = reviewDto.copy(id = -1) // ID temporaire
                }

                binding.etMessage.clearFocus()
            }

            setupSizeAndWeightViews()
        }
    }

    private fun setupSizeAndWeightViews() {
        when {
            item.categoryName?.lowercase() in listOf("vêtements", "vetement", "habillement") && !item.size.isNullOrEmpty() -> {
                sizeAdapter = SizeListAdapter(item.size.toList(), allowMultipleSelection = true)
                binding.sizeList.adapter = sizeAdapter
                binding.sizeList.layoutManager = LinearLayoutManager(
                    this,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                binding.sizeList.visibility = View.VISIBLE
                binding.textViewSizesTitle.visibility = View.VISIBLE
                binding.textViewSizesTitle.text = "Tailles disponibles"
            }
            item.categoryName?.lowercase() in listOf("nourriture", "repas") && !item.weight.isNullOrEmpty() -> {
                sizeAdapter = SizeListAdapter(item.weight.toList(), allowMultipleSelection = true)
                binding.sizeList.adapter = sizeAdapter
                binding.sizeList.layoutManager = LinearLayoutManager(
                    this,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                binding.sizeList.visibility = View.VISIBLE
                binding.textViewSizesTitle.visibility = View.VISIBLE
                binding.textViewSizesTitle.text = "Poids disponibles"
            }
            else -> {
                binding.sizeList.visibility = View.GONE
                binding.textViewSizesTitle.visibility = View.GONE
            }
        }
    }

    private fun showDeleteConfirmation() {
        Snackbar.make(binding.root, "Êtes-vous sûr de vouloir supprimer ce produit?", Snackbar.LENGTH_LONG)
            .setAction("OUI") {
                productViewModel.deleteProduct(item.id)
            }
            .show()
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        // Recharger les données quand on revient sur l'activité
        userId?.let {
            refreshData()
        }
    }
}
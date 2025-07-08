package com.example.project180.Activity

import SizeListAdapter
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project180.ViewModel.CartViewModel
import com.example.project180.ViewModel.ProductViewModel
import com.example.project180.databinding.ActivityManipulateCartBinding
import com.google.android.material.snackbar.Snackbar

class ManipulateCartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManipulateCartBinding
    private val cartViewModel: CartViewModel by viewModels()
    private val productViewModel: ProductViewModel by viewModels()
    private var selectedOption: String? = null
    private var currentAdapter: SizeListAdapter? = null
    private var cartId: Int? = null // Assuming the cart item ID is passed in the intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManipulateCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val categoryTitle = intent.getStringExtra("category_titre") ?: ""
        cartId = intent.getIntExtra("cart_id", 0)
        setupViews(categoryTitle)
        observeProductData(categoryTitle)
    }

    private fun setupViews(categoryTitle: String) {
        binding.textViewSizesTitle.text = when {
            isClothingCategory(categoryTitle) -> "Sélectionnez une taille"
            isFoodCategory(categoryTitle) -> "Sélectionnez un poids"
            else -> "Options disponibles"
        }

        binding.closeButton.setOnClickListener {
            // Ensure category-based logic is applied for closing the activity and updating the cart
            when {
                isClothingCategory(categoryTitle) -> {
                    // Check if an option was selected before updating the cart
                    selectedOption?.let { selected ->
                        if (cartId != null) {
                            cartViewModel.updateCartItemOptions(cartId!!, sizes = listOf(selected), weights = null)
                        }
                    }
                }
                isFoodCategory(categoryTitle) -> {
                    // In case of food category, check if a weight was selected and update the cart
                    selectedOption?.let { selected ->
                        if (cartId != null) {
                            cartViewModel.updateCartItemOptions(cartId!!, sizes = null, weights = listOf(selected))
                        }
                    }
                }
                else -> {
                    // Handle default case if needed (e.g., for other categories or generic behavior)
                }
            }

            finish() // Close the activity after the updates
        }
    }

    private fun observeProductData(categoryTitle: String) {
        when {
            isClothingCategory(categoryTitle) -> {
                productViewModel.sizes.observe(this) { sizeDto ->
                    sizeDto?.let {
                        val sizesList = it.sizes
                        setupOptionsRecyclerView(sizesList, allowMultipleSelection = false)
                    }
                }
                productViewModel.fetchSizes()
            }
            isFoodCategory(categoryTitle) -> {
                productViewModel.weights.observe(this) { weightDto ->
                    weightDto?.let {
                        val weightsList = it.weights
                        setupOptionsRecyclerView(weightsList, allowMultipleSelection = false)
                    }
                }
                productViewModel.fetchWeights()
            }
        }
    }

    private fun setupOptionsRecyclerView(options: List<String>, allowMultipleSelection: Boolean) {
        binding.sizeList.apply {
            layoutManager = LinearLayoutManager(
                this@ManipulateCartActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = SizeListAdapter(options, allowMultipleSelection).apply {
                setOnSizeSelectedListener { selectedOption ->
                    this@ManipulateCartActivity.selectedOption = selectedOption
                }
            }
        }
    }

    private fun isClothingCategory(category: String): Boolean {
        return category.lowercase() in listOf("vêtements", "vetement", "habillement")
    }

    private fun isFoodCategory(category: String): Boolean {
        return category.lowercase() in listOf("nourriture", "repas")
    }
}

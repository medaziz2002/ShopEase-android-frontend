package com.example.project180.Activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.example.project180.Adapter.BookPagerAdapter
import com.example.project180.Helper.BottomNavigationHelper
import com.example.project180.Model.ItemsModel
import com.example.project180.Model.ProductDto
import com.example.project180.R
import com.example.project180.Util.BookPageTransformer
import com.example.project180.Util.UserPreferences
import com.example.project180.ViewModel.ProductViewModel
import com.example.project180.databinding.ActivityBookStyleBinding

class ListProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookStyleBinding
    private val productViewModel: ProductViewModel by viewModels()
    private lateinit var bookPagerAdapter: BookPagerAdapter
    private var currentPage = 0
    private var allProducts = listOf<ProductDto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookStyleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val bottomNavView = binding.bottomNavContainer.findViewById<View>(R.id.bottomNavigationView)
        BottomNavigationHelper.setupBottomNavigation(bottomNavView, this)
        setupUserInfo()
        setupViewPager()
        setupSearch()
        setupObservers()
        setupListeners()
        val userInfo = UserPreferences.getUserInfo(this)
        val headerView = binding.root.findViewById<View>(R.id.header)
        val notificationBell = headerView.findViewById<FrameLayout>(R.id.notification_bell)
        productViewModel.fetchProducts()
        val bellIcon = notificationBell.getChildAt(0) as ImageView // This gets the ImageView inside the FrameLayout
        bellIcon.setOnClickListener {
            navigateToNotifications()
        }
    }
    private fun navigateToNotifications() {
        val intent = Intent(this, NotificationActivity::class.java)
        startActivity(intent)
    }

    private fun setupSearch() {
        val headerView = binding.root.findViewById<View>(R.id.header)
        val searchEditText = headerView.findViewById<EditText>(R.id.editTextText)

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterProducts(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }
    private fun filterProducts(query: String) {
        val lowerQuery = query.lowercase()

        val filteredList = if (query.isEmpty()) {
            allProducts
        } else {
            allProducts.filter { product ->
                listOfNotNull(
                    product.title,
                    product.description,
                    product.categoryDto?.titre,
                    product.sellerName,
                    product.sellerTelephone,
                    product.sellerPic,
                    product.price?.toString(),
                    product.size?.joinToString(", "),
                    product.rating?.toString(),
                    product.stock?.toString(),
                    product.discountPercentage?.toString()
                ).any { field ->
                    field.contains(lowerQuery, ignoreCase = true)
                }
            }
        }
        bookPagerAdapter.updateProducts(filteredList)
    }




    private fun setupUserInfo() {
        UserPreferences.getUserInfo(this)?.let { userInfo ->
            binding.header.textView5.text = "${userInfo.firstName} ${userInfo.lastName}"
            binding.addImagesButton.visibility = if (userInfo.role == "Vendeur") View.VISIBLE else View.GONE
        }
    }

    private fun setupViewPager() {
        bookPagerAdapter = BookPagerAdapter(this, emptyList()) { product ->
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra("product", product.toItemsModel())
            }
            startActivity(intent)
        }

        binding.productViewPager.apply {
            adapter = bookPagerAdapter
            offscreenPageLimit = 3
            setPageTransformer(BookPageTransformer())

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    if (position != currentPage) {
                        bookPagerAdapter.playPageTurnSound()
                        currentPage = position
                    }
                }
            })
        }
    }

    private fun setupObservers() {
        productViewModel.products.observe(this, Observer { products ->
            allProducts = products // Sauvegarde de tous les produits
            bookPagerAdapter.updateProducts(products)
        })

        productViewModel.error.observe(this, Observer { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        })
    }

    private fun setupListeners() {
        binding.addImagesButton.setOnClickListener {
            startActivity(Intent(this, AddProductActivity::class.java))
        }
    }

    fun ProductDto.toItemsModel(): ItemsModel {
        return ItemsModel(
            id = this.id ?: 0,
            categoryId =this.categoryId ?:0,
            categoryName = this.categoryDto?.titre ?:"il n'y a pas",
            title = this.title ?: "Nom inconnu",
            description = this.description ?: "Description non disponible",
            image = this.images ?: emptyList(),
            size = this.size?.let { ArrayList(it) } ?: ArrayList(),
            price = this.price ?: 0.0,
            rating = this.rating ?: 0.0,
            stock = this.stock ?: 0,
            numberInCart = 0,
            weight = this.weight?.let { ArrayList(it) } ?: ArrayList(),
            discountedPrice = if ((this.discountPercentage ?: 0.0) > 0) {
                (this.price ?: 0.0) * (1 - (this.discountPercentage ?: 0.0) / 100)
            } else {
                this.price ?: 0.0
            },
            sellerName = this.sellerName ?: "Vendeur inconnu",
            sellerTell = this.sellerTelephone ?: "",
            sellerPic = this.sellerPic ?: ""
        )
    }
}





/*
picUrl = this.images?.mapNotNull {
    if (it.imageBase64?.startsWith("/9j/") == true || it.imageBase64?.startsWith("iVBORw0") == true) {
        "data:image/jpeg;base64,${it.imageBase64}"
    } else {
        it.imageBase64
    }
}?.let { ArrayList(it) } ?: ArrayList(),
*/
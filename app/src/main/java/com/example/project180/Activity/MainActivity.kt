package com.example.project180.Activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.example.project180.Adapter.BestSellerAdapter
import com.example.project180.Adapter.CategoryAdapter
import com.example.project180.Adapter.SliderAdapter
import com.example.project180.Helper.BottomNavigationHelper
import com.example.project180.Model.CategoryDto
import com.example.project180.Model.ItemsModel
import com.example.project180.Model.SliderModel
import com.example.project180.R
import com.example.project180.Util.UserPreferences
import com.example.project180.ViewModel.CategoryViewModel
import com.example.project180.ViewModel.MainViewModel
import com.example.project180.ViewModel.ProductViewModel
import com.example.project180.databinding.ActivityMainBinding


class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var productViewModel: ProductViewModel
    private lateinit var bestSellerAdapter: BestSellerAdapter
    private lateinit var categoryAdapter: CategoryAdapter

    // Variables pour stocker les listes complètes
    private var allProducts: MutableList<ItemsModel> = mutableListOf()
    private var allCategories: MutableList<CategoryDto> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        categoryViewModel = ViewModelProvider(this).get(CategoryViewModel::class.java)
        productViewModel = ViewModelProvider(this).get(ProductViewModel::class.java)

        setupUI()
        setupClickListeners()
        setupSearch()
        initBanners()
        initCategories()
        initBestSeller()
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
                product.title?.lowercase()?.contains(lowerQuery) == true ||
                        product.description?.lowercase()?.contains(lowerQuery) == true ||
                        product.price?.toString()?.contains(lowerQuery) == true
            }
        }
        bestSellerAdapter.updateItems(filteredList.toMutableList())
    }


    private fun setupUI() {
        // Configuration de la navigation
        val bottomNavView = binding.bottomNavContainer.findViewById<View>(R.id.bottomNavigationView)
        BottomNavigationHelper.setupBottomNavigation(bottomNavView, this)
        val headerView = binding.root.findViewById<View>(R.id.header)
        val notificationBell = headerView.findViewById<FrameLayout>(R.id.notification_bell)
        val bellIcon = notificationBell.getChildAt(0) as ImageView // This gets the ImageView inside the FrameLayout
        bellIcon.setOnClickListener {
            navigateToNotifications()
        }
        // Affichage des infos utilisateur
        val userInfo = UserPreferences.getUserInfo(this)
        if (userInfo != null) {
            val informations = headerView.findViewById<TextView>(R.id.textView5)
            val userName = "${userInfo.firstName} ${userInfo.lastName}"
            informations.text = userName
        }

        // Initialisation des adapters
        bestSellerAdapter = BestSellerAdapter(mutableListOf()) { item ->
            navigateToDetail(item)
        }
        binding.viewBestSeller.layoutManager = GridLayoutManager(this, 2)
        binding.viewBestSeller.adapter = bestSellerAdapter

        categoryAdapter = CategoryAdapter(mutableListOf())
        binding.viewCategory.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.viewCategory.adapter = categoryAdapter

        val fabChat = binding.root.findViewById<View>(R.id.fab_chat)
        fabChat.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }
    }

    private fun navigateToDetail(item: ItemsModel) {
        val intent = Intent(this, DetailActivity::class.java).apply {
            putExtra("product", item)
        }
        startActivity(intent)
    }
    private fun navigateToNotifications() {
        val intent = Intent(this, NotificationActivity::class.java)
        startActivity(intent)
    }


    private fun setupClickListeners() {
        binding.SeeAllCategory.setOnClickListener {
            startActivity(Intent(this, ListCategoryActivity::class.java))
        }
        binding.SeeAllProduct.setOnClickListener {
            startActivity(Intent(this, ListProductActivity::class.java))
        }
    }

    private fun initBestSeller() {
        binding.progressBarBestSeller.visibility = View.VISIBLE
        productViewModel._topRatedProducts.observe(this, Observer { products ->
            products?.let {
                allProducts = it.map { product ->
                    ItemsModel(
                        id = product.id ?: 0,
                        categoryName = product.categoryDto?.titre ?:"il n'y a pas",
                        title = product.title ?: "Nom inconnu",
                        picUrl = product.images?.mapNotNull {
                            if (it.imageBase64?.startsWith("/9j/") == true ||
                                it.imageBase64?.startsWith("iVBORw0") == true) {
                                "data:image/jpeg;base64,${it.imageBase64}"
                            } else {
                                it.imageBase64
                            }
                        }?.let { ArrayList(it) } ?: ArrayList(),
                        price = product.price ?: 0.0,
                        rating = product.rating ?: 0.0,
                        discountedPrice = if ((product.discountPercentage ?: 0.0) > 0) {
                            (product.price ?: 0.0) * (1 - (product.discountPercentage ?: 0.0) / 100)
                        } else {
                            product.price ?: 0.0
                        },
                        description = product.description ?: "",
                        sellerName = product.sellerName ?: "",
                        sellerTell = product.sellerTelephone ?: "",
                        sellerPic = product.sellerPic ?: "",
                        size = product.size?.let { ArrayList(it) } ?: ArrayList(),
                        stock = product.stock ?: 0
                    )
                }.toMutableList()

                bestSellerAdapter.updateItems(allProducts)
                binding.progressBarBestSeller.visibility = View.GONE
            }
        })
        productViewModel.fetchTopRatedProducts(5)
    }

    private fun initCategories() {
        binding.progressBarCategory.visibility = View.VISIBLE
        categoryViewModel.categories.observe(this, Observer { categories ->
            categories?.let {
                allCategories.clear()
                allCategories.addAll(it)
                categoryAdapter.items = allCategories.toMutableList()
                categoryAdapter.notifyDataSetChanged()
                binding.progressBarCategory.visibility = View.GONE
            }
        })
        categoryViewModel.fetchCategories()
    }

    private fun initBanners() {
        binding.progressBarBanner.visibility = View.VISIBLE
        mainViewModel.banners.observe(this, Observer {
            banners(it)
            binding.progressBarBanner.visibility = View.GONE
        })
        mainViewModel.loadBanners()
    }

    private fun banners(images: List<SliderModel>) {
        binding.viewPagerSlider.adapter = SliderAdapter(images, binding.viewPagerSlider)
        binding.viewPagerSlider.clipToPadding = false
        binding.viewPagerSlider.clipChildren = false
        binding.viewPagerSlider.offscreenPageLimit = 3
        binding.viewPagerSlider.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        val compositePageTransformer = CompositePageTransformer().apply {
            addTransformer(MarginPageTransformer(40))
        }
        binding.viewPagerSlider.setPageTransformer(compositePageTransformer)
        if (images.size > 1) {
            binding.dotIndicator.visibility = View.VISIBLE
            binding.dotIndicator.attachTo(binding.viewPagerSlider)
        }
    }
}
package com.example.project180.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project180.Adapter.CategoryAdapterList
import com.example.project180.R
import com.example.project180.ViewModel.CategoryViewModel
import com.example.project180.databinding.ActivityListCategoryBinding
import com.google.android.material.snackbar.Snackbar
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.example.project180.Helper.BottomNavigationHelper
import com.example.project180.Model.CategoryDto
import com.example.project180.Util.UserPreferences


class ListCategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListCategoryBinding
    private lateinit var viewModel: CategoryViewModel
    private lateinit var adapter: CategoryAdapterList
    private var role: String = "USER" // Par défaut à USER
    private var allCategories = listOf<CategoryDto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val userInfo = UserPreferences.getUserInfo(this)
        val headerView = binding.root.findViewById<View>(R.id.header)
        val notificationBell = headerView.findViewById<FrameLayout>(R.id.notification_bell)
        if (userInfo != null) {

            val informations = headerView.findViewById<TextView>(R.id.textView5)
            val userName = "${userInfo.firstName} ${userInfo.lastName}"
            informations.text = userName
            role = userInfo.role
        }
        val bellIcon = notificationBell.getChildAt(0) as ImageView // This gets the ImageView inside the FrameLayout
        bellIcon.setOnClickListener {
            navigateToNotifications()
        }

        binding.addImagesButton.visibility = if (role == "SUPERADMIN") View.VISIBLE else View.GONE

        val bottomNavView = binding.bottomNavContainer.findViewById<View>(R.id.bottomNavigationView)
        BottomNavigationHelper.setupBottomNavigation(bottomNavView, this)

        binding.addImagesButton.setOnClickListener {
            startActivity(Intent(this@ListCategoryActivity, AddCategoryActivity::class.java))
            finish()
        }
        setupSearch()
        viewModel = ViewModelProvider(this)[CategoryViewModel::class.java]

        setupRecyclerView()
        setupObservers()
        viewModel.fetchCategories()
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
                filterCategories(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterCategories(query: String) {
        val filteredList = if (query.isEmpty()) {
            allCategories // Retourne la liste complète quand la recherche est vide
        } else {
            allCategories.filter {
                it.titre?.contains(query, ignoreCase = true) == true
            }
        }
        adapter.submitList(filteredList)
    }

    private fun setupObservers() {
        viewModel.categories.observe(this, Observer { categories ->
            categories?.let {
                allCategories = it // Mettre à jour allCategories avec la nouvelle liste
                adapter = CategoryAdapterList(role == "SUPERADMIN") { category ->
                    Toast.makeText(this, "Clicked: ${category.titre}", Toast.LENGTH_SHORT).show()
                }
                binding.recyclerView.adapter = adapter
                adapter.submitList(it) // Utilisez submitList au lieu de submitOriginalList
            }
        })
    }

    private val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
        0,
        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            if (role != "SUPERADMIN") {
                adapter.notifyItemChanged(viewHolder.adapterPosition)
                Snackbar.make(binding.root, "Action non autorisée", Snackbar.LENGTH_LONG).show()
                return
            }

            val position = viewHolder.adapterPosition
            val category = adapter.currentList[position]

            when (direction) {
                ItemTouchHelper.LEFT -> {
                    val intent = Intent(this@ListCategoryActivity, AddCategoryActivity::class.java).apply {
                        putExtra("categoryId", category.id)
                        putExtra("categoryTitle", category.titre)
                        putExtra("categoryImageBase64", category.imageDto?.imageBase64)
                    }
                    startActivity(intent)
                }

                ItemTouchHelper.RIGHT -> {
                    val dialogBuilder = android.app.AlertDialog.Builder(this@ListCategoryActivity)
                    dialogBuilder.setMessage("Êtes-vous sûr de vouloir supprimer la catégorie : ${category.titre} ?")
                        .setCancelable(false)
                        .setPositiveButton("Oui") { dialog, _ ->
                            viewModel.deleteCategory(category.id!!)
                            Snackbar.make(binding.root, "Catégorie supprimée avec succès !", Snackbar.LENGTH_LONG).show()
                            dialog.dismiss()
                        }
                        .setNegativeButton("Non") { dialog, _ ->
                            adapter.notifyItemChanged(position)
                            dialog.dismiss()
                        }

                    val alert = dialogBuilder.create()
                    alert.show()
                }
            }
        }

        override fun getSwipeDirs(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            return if (viewHolder is CategoryAdapterList.CategoryViewHolder && role == "SUPERADMIN") {
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            } else {
                0
            }
        }
    })

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }



}
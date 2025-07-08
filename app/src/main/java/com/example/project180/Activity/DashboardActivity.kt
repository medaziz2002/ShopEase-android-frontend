package com.example.project180.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.project180.Helper.BottomNavigationHelper
import com.example.project180.R
import com.example.project180.Util.UserPreferences
import com.example.project180.ViewModel.DashboardViewModel
import com.example.project180.databinding.ActivityDashboardBinding
import com.google.android.material.snackbar.Snackbar

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var viewModel: DashboardViewModel
    private lateinit var role: String
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userInfo = UserPreferences.getUserInfo(this)
        role = userInfo?.role ?: "Vendeur"
        userId = userInfo?.id ?: -1

        val bottomNavView = binding.bottomNavContainer.findViewById<View>(R.id.bottomNavigationView)
        BottomNavigationHelper.setupBottomNavigation(bottomNavView, this)
        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
        findViewById<View>(R.id.editTextText)?.visibility =View.GONE
        setupUI()
        setupObservers()
        fetchDataBasedOnRole()
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

    }

    private fun navigateToNotifications() {
        val intent = Intent(this, NotificationActivity::class.java)
        startActivity(intent)
    }

    private fun fetchDataBasedOnRole() {
        if (role == "SUPERADMIN") {
            viewModel.fetchDashboardStats()
        } else {
            if (userId != -1) {
                viewModel.fetchDashboardStatsVendeur(userId)
            } else {
                Snackbar.make(binding.root, "Impossible de récupérer l'ID du vendeur", Snackbar.LENGTH_LONG).show()
                Log.e("DashboardActivity", "User ID is not available for seller")
            }
        }
    }

    private fun setupUI() {
        val adminGrid = binding.root.findViewById<GridLayout>(R.id.gridLayoutAdmin)
        val sellerGrid = binding.root.findViewById<GridLayout>(R.id.gridLayoutSeller)

        if (role == "SUPERADMIN") {
            adminGrid.visibility = View.VISIBLE
            sellerGrid.visibility = View.GONE
        } else {
            adminGrid.visibility = View.GONE
            sellerGrid.visibility = View.VISIBLE
        }
    }

    private fun setupObservers() {
        // Observer pour les stats admin
        viewModel.stats.observe(this, Observer { stats ->
            stats?.let {
                binding.tvTotalUsers.text = "${it.totalUsers}"
                binding.tvClients.text = "${it.totalClients}"
                binding.tvSellers.text = "${it.totalSellers}"
                binding.tvProducts.text = "${it.totalProducts}"
                binding.tvCategories.text = "${it.totalCategories}"
            }
        })

        // Observer pour les stats vendeur
        viewModel.statsSeller.observe(this, Observer { stats ->
            stats?.let {
                binding.tvProductsSeller.text = "${it.totalProducts}"
                binding.tvCategoriesSeller.text = "${it.totalCategories}"
            }
        })

        viewModel.error.observe(this, Observer { errorMessage ->
            errorMessage?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
            }
        })
    }
}
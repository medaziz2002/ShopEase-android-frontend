package com.example.project180.Activity

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project180.Adapter.OrdersAdapter
import com.example.project180.R
import com.example.project180.Util.UserPreferences
import com.example.project180.ViewModel.OrderViewModel
import com.example.project180.databinding.ActivityOrdersBinding
import com.google.android.material.snackbar.Snackbar

class OrdersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrdersBinding
    private val orderViewModel: OrderViewModel by viewModels()
    private lateinit var ordersAdapter: OrdersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupObservers()
        loadOrders()

        binding.backBtn.setOnClickListener {
            finish()
        }

    }

    private fun setupRecyclerView() {
        ordersAdapter = OrdersAdapter().apply {
            onStatusUpdateListener = { orderId, newStatus ->
                // Vérifier à nouveau le rôle par sécurité
                UserPreferences.getUserInfo(this@OrdersActivity)?.takeIf { it.role == "Vendeur" }?.let { user ->
                    orderViewModel.updateOrderStatus(orderId, newStatus)
                    Snackbar.make(binding.root, "Statut de la commande mis à jour", Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        binding.ordersRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@OrdersActivity)
            adapter = ordersAdapter
        }
    }

    private fun setupObservers() {
        orderViewModel.orders.observe(this) { orders ->
            ordersAdapter.submitList(orders)
            binding.emptyTxt.visibility = if (orders.isEmpty()) View.VISIBLE else View.GONE
        }

        orderViewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        orderViewModel.error.observe(this) { error ->
            error?.let {
                binding.emptyTxt.text = it
                binding.emptyTxt.visibility = View.VISIBLE
            }
        }
    }

    private fun loadOrders() {
        UserPreferences.getUserInfo(this)?.let { user ->
            if (user.role == "Vendeur") {
                // Charger toutes les commandes pour le vendeur
                orderViewModel.loadVendeurOrders(user.id)
            } else {
                orderViewModel.loadUserOrders(user.id)
            }
        } ?: run {
            binding.emptyTxt.text = "Utilisateur non connecté"
            binding.emptyTxt.visibility = View.VISIBLE
        }
    }
}
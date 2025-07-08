package com.example.project180.Activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project180.Adapter.NotificationAdapter
import com.example.project180.R
import com.example.project180.Util.UserPreferences
import com.example.project180.ViewModel.NotificationViewModel
import com.example.project180.databinding.ActivityNotificationBinding

class NotificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationBinding
    private lateinit var viewModel: NotificationViewModel
    private lateinit var adapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(NotificationViewModel::class.java)

        setupRecyclerView()
        setupObservers()

        val userInfo = UserPreferences.getUserInfo(this)
        if (userInfo != null) {
            viewModel.fetchNotifications(userInfo.id)  // Pass the user ID to the ViewModel
        } else {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
        }

        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun setupObservers() {
        viewModel.notifications.observe(this, Observer { notifications ->
            notifications?.let {
                adapter = NotificationAdapter(it)
                binding.recyclerView.adapter = adapter
            }
        })
    }
}

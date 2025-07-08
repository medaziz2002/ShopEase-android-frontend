package com.example.project180.Activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project180.Adapter.UserAdapter
import com.example.project180.Helper.BottomNavigationHelper
import com.example.project180.Model.UserRequest
import com.example.project180.R
import com.example.project180.Util.UserPreferences
import com.example.project180.ViewModel.UserViewModel
import com.example.project180.databinding.ActivityListUserBinding

class ListUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListUserBinding
    private lateinit var viewModel: UserViewModel
    private lateinit var adapter: UserAdapter
    private var allUsers = listOf<UserRequest>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize user info in header
        setupUserHeader()

        // Setup bottom navigation
        setupBottomNavigation()

        // Initialize RecyclerView and Adapter
        setupRecyclerView()

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[UserViewModel::class.java]

        // Setup search functionality
        setupSearch()

        // Setup observers for LiveData
        setupObservers()

        // Fetch users data
        viewModel.fetchUsers()
    }

    private fun setupUserHeader() {
        val userInfo = UserPreferences.getUserInfo(this)
        val headerView = binding.root.findViewById<View>(R.id.header)
        val notificationBell = headerView.findViewById<FrameLayout>(R.id.notification_bell)
        val bellIcon = notificationBell.getChildAt(0) as ImageView

        if (userInfo != null) {
            val informations = headerView.findViewById<TextView>(R.id.textView5)
            val userName = "${userInfo.firstName} ${userInfo.lastName}"
            informations.text = userName
        }

        bellIcon.setOnClickListener {
            navigateToNotifications()
        }
    }

    private fun setupBottomNavigation() {
        val bottomNavView = binding.bottomNavContainer.findViewById<View>(R.id.bottomNavigationView)
        BottomNavigationHelper.setupBottomNavigation(bottomNavView, this)
    }

    private fun setupRecyclerView() {
        adapter = UserAdapter().apply {
            setOnItemClickListener(object : UserAdapter.OnItemClickListener {
                override fun onMessageClick(user: UserRequest) {
                    user.telephone?.let { phoneNumber ->
                        val sendIntent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("sms:$phoneNumber")
                            putExtra("sms_body", "Bonjour ${user.nom ?: ""}")
                        }
                        startActivity(sendIntent)
                    } ?: run {
                        Toast.makeText(
                            this@ListUserActivity,
                            "Aucun numéro de téléphone disponible",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onCallClick(user: UserRequest) {
                    user.telephone?.let { phoneNumber ->
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:$phoneNumber")
                        }
                        startActivity(intent)
                    } ?: run {
                        Toast.makeText(
                            this@ListUserActivity,
                            "Aucun numéro de téléphone disponible",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
        }

        binding.recyclerViewUser.apply {
            layoutManager = LinearLayoutManager(this@ListUserActivity)
            adapter = this@ListUserActivity.adapter
        }
    }

    private fun setupSearch() {
        val headerView = binding.root.findViewById<View>(R.id.header)
        val searchEditText = headerView.findViewById<EditText>(R.id.editTextText)

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterUsers(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun navigateToNotifications() {
        val intent = Intent(this, NotificationActivity::class.java)
        startActivity(intent)
    }

    private fun filterUsers(query: String) {
        val filteredList = if (query.isEmpty()) {
            allUsers
        } else {
            allUsers.filter { user ->
                user.nom?.contains(query, ignoreCase = true) == true ||
                        user.prenom?.contains(query, ignoreCase = true) == true ||
                        user.email?.contains(query, ignoreCase = true) == true ||
                        user.role?.contains(query, ignoreCase = true) == true
            }
        }
        adapter.setUsers(filteredList)
    }

    private fun setupObservers() {
        viewModel.users.observe(this, Observer { users ->
            allUsers = users
            adapter.setUsers(users)
        })

        viewModel.error.observe(this, Observer { error ->
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
        })
    }
}
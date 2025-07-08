package com.example.project180.Helper

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.project180.Activity.CartActivity
import com.example.project180.Activity.DashboardActivity
import com.example.project180.Activity.ListCategoryActivity
import com.example.project180.Activity.ListProductActivity
import com.example.project180.Activity.ListUserActivity
import com.example.project180.Activity.MainActivity
import com.example.project180.Activity.OrdersActivity
import com.example.project180.Activity.ScreenActivity
import com.example.project180.R
import com.example.project180.Util.UserPreferences

object BottomNavigationHelper {

    @SuppressLint("SuspiciousIndentation")
    fun setupBottomNavigation(view: View, context: Context) {
        // Récupérer les informations de l'utilisateur
        val userInfo = UserPreferences.getUserInfo(context)
        val role = userInfo?.role ?: "Client" // Valeur par défaut si null

        // Configurer les écouteurs de clic
        view.findViewById<View>(R.id.dashboard).setOnClickListener {
            if (context !is DashboardActivity) {
                val intent = Intent(context, DashboardActivity::class.java)
                context.startActivity(intent)
                (context as? AppCompatActivity)?.overridePendingTransition(0, 0)
            }
        }

        view.findViewById<View>(R.id.list_product).setOnClickListener {
            val intent = Intent(context, ListProductActivity::class.java)
            context.startActivity(intent)
            (context as? AppCompatActivity)?.overridePendingTransition(0, 0)
        }

        view.findViewById<View>(R.id.list_category).setOnClickListener {
            val intent = Intent(context, ListCategoryActivity::class.java)
            context.startActivity(intent)
            (context as? AppCompatActivity)?.overridePendingTransition(0, 0)
        }

        view.findViewById<View>(R.id.list_user).setOnClickListener {
            if (role == "SUPERADMIN") {
                val intent = Intent(context, ListUserActivity::class.java)
                context.startActivity(intent)
                (context as? AppCompatActivity)?.overridePendingTransition(0, 0)
            }
        }

        view.findViewById<View>(R.id.cart_layout).setOnClickListener {
            if (role == "Client") {
                val intent = Intent(context, CartActivity::class.java)
                context.startActivity(intent)
                (context as? AppCompatActivity)?.overridePendingTransition(0, 0)
            }
        }

        view.findViewById<View>(R.id.cart_commande).setOnClickListener {
            if (role == "Client" || role == "Vendeur") {
                val intent = Intent(context, OrdersActivity::class.java)
                context.startActivity(intent)
                (context as? AppCompatActivity)?.overridePendingTransition(0, 0)
            }
        }

        view.findViewById<View>(R.id.home_layout).setOnClickListener {
            if (role == "Client") {
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
                (context as? AppCompatActivity)?.overridePendingTransition(0, 0)
            }
        }

        view.findViewById<View>(R.id.profile_layout).setOnClickListener {

                val intent = Intent(context, ScreenActivity::class.java)
                context.startActivity(intent)
                (context as? AppCompatActivity)?.overridePendingTransition(0, 0)

        }

        // Configurer la visibilité des éléments en fonction du rôle
        view.findViewById<View>(R.id.list_user).visibility =
            if (role == "SUPERADMIN") View.VISIBLE else View.GONE

        view.findViewById<View>(R.id.dashboard).visibility =
            if (role == "SUPERADMIN" || role == "Vendeur") View.VISIBLE else View.GONE

        view.findViewById<View>(R.id.list_category).visibility =
            if (role == "SUPERADMIN" || role == "Vendeur") View.VISIBLE else View.GONE

        view.findViewById<View>(R.id.home_layout).visibility =
            if (role == "Client") View.VISIBLE else View.GONE

        view.findViewById<View>(R.id.cart_layout).visibility =
            if (role == "Client") View.VISIBLE else View.GONE

        view.findViewById<View>(R.id.cart_commande).visibility =
            if (role == "Client" || role == "Vendeur") View.VISIBLE else View.GONE
    }
}
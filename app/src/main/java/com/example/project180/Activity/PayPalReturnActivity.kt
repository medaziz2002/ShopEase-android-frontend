package com.example.project180.Activity


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class PayPalReturnActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        // Traitez le retour de PayPal ici si nécessaire
        finish()  // Retournez à CartActivity
    }
}
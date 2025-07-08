package com.example.project180.Activity

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.google.firebase.FirebaseApp
import com.paypal.checkout.PayPalCheckout
import com.paypal.checkout.config.CheckoutConfig
import com.paypal.checkout.config.Environment
import com.paypal.checkout.config.SettingsConfig
import com.paypal.checkout.createorder.CurrencyCode
import com.paypal.checkout.createorder.UserAction
import com.paypal.pyplcheckout.BuildConfig

class App : Application() {

    var clientID = "AajYdzavK4IrS6zp2WNv3wKwYI-lt0yIoqGE5TeO8T4ExPJ8ypC-YKVDQcV8nx6So7z2OSM7j_qLBPpZ"
    var returnUrl = "com.example.project180://paypalpay"

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        val config = CheckoutConfig(
            application = this,
            clientId = clientID,
            environment = Environment.SANDBOX,
            returnUrl = returnUrl,
            currencyCode = CurrencyCode.EUR,
            userAction = UserAction.PAY_NOW,
            settingsConfig = SettingsConfig(
                loggingEnabled = true,
                showWebCheckout = true  // Active le mode web si nécessaire
            )
        )
        PayPalCheckout.setConfig(config)
        createNotificationChannel()
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "commande_updates",  // ID du canal (doit correspondre à celui utilisé dans showNotification)
                "Commandes",         // Nom lisible par l'utilisateur
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications pour les mises à jour des commandes"
            }

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }













}
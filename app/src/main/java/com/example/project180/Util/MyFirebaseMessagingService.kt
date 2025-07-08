package com.example.project180.services

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.project180.Activity.NotificationActivity
import com.example.project180.Helper.RetrofitInstance
import com.example.project180.R
import com.example.project180.Util.UserPreferences
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Nouveau token généré: $token")

        UserPreferences.saveFcmToken(applicationContext, token)
        val userInfo = UserPreferences.getUserInfo(this)

        if (userInfo != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    RetrofitInstance.apiWithAuth.updateFcmToken(userInfo.id, token)
                    Log.d("FCM", "Token envoyé au serveur avec succès")
                } catch (e: Exception) {
                    Log.e("FCM", "Échec d'envoi du token", e)
                }
            }
        } else {
            Log.d("FCM", "Utilisateur non connecté : token sauvegardé en local uniquement")
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "Message reçu")

        // Vérifier si l'utilisateur est connecté
        if (!UserPreferences.isUserLoggedIn(this)) {
            Log.d("FCM", "Utilisateur non connecté - Notification ignorée")
            return
        }

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"]
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"]

        if (!title.isNullOrEmpty() && !body.isNullOrEmpty()) {
            showNotification(title, body)
        }
    }

    private fun showNotification(title: String, message: String) {
        Log.d("FCM", "Notification: $title - $message")
        val channelId = "commande_updates"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Commandes",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.bell)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}

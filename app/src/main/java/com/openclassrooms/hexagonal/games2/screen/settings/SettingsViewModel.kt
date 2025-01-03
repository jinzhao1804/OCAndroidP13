package com.openclassrooms.hexagonal.games2.screen.settings

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * ViewModel responsible for managing user settings, specifically notification preferences.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
  @ApplicationContext private val context: Context
) : ViewModel() {

  // Define the notification channel ID
  private val channelId = "com.openclassrooms.hexagonal.notifications"

  /**
   * Enables notifications by creating or modifying the notification channel.
   * This sets the notification channel's importance to high, which allows notifications to be shown.
   */
  fun enableNotifications() {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Check if we are on Android Oreo (API 26) or later
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      // Delete the existing channel if it exists to update the importance
      notificationManager.deleteNotificationChannel(channelId)

      println( "$channelId deleted")
      // Create a new notification channel with high importance
      val channel = NotificationChannel(
        channelId,
        "App Notifications", // Channel name
        NotificationManager.IMPORTANCE_HIGH // Set importance level to high so notifications are shown
      )
      notificationManager.createNotificationChannel(channel)

      notificationManager.deleteNotificationChannel(channelId)

      println("Notifications enabled by creating/updating the notification channel.")
    } else {
      // For devices below Android Oreo, notifications are handled without channels
      println("Notifications enabled. No channel needed for versions below Oreo.")
    }
  }

  /**
   * Disables notifications by either muting the notification channel or deleting it.
   * This sets the channel's importance to low or deletes it, preventing notifications.
   */
  fun disableNotifications() {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Check if we are on Android Oreo (API 26) or later
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      // Delete the existing channel if it exists to update the importance
      notificationManager.deleteNotificationChannel(channelId)
      println( "$channelId deleted")

      // Create a new notification channel with no importance (effectively disabling notifications)
      val channel = NotificationChannel(
        channelId,
        "App Notifications", // Channel name
        NotificationManager.IMPORTANCE_NONE // Set importance level to none, so notifications are not shown
      )
      notificationManager.createNotificationChannel(channel)
      println("Notifications disabled by setting the channel importance to low.")
    } else {
      // For devices below Android Oreo, notifications are handled without channels
      println("Notifications disabled. No channel needed for versions below Oreo.")
    }
  }
}

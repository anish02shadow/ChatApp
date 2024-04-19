package com.example69.chatapp.ui.theme.ViewModels

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class MessageLimitManager @Inject constructor(
    @ApplicationContext public val context: Context){

    private val sharedPreferences = context.getSharedPreferences("message_limit", Context.MODE_PRIVATE)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun incrementMessageCount() {
        val today = dateFormat.format(Date())
        val currentCount = sharedPreferences.getInt(today, 0)
        sharedPreferences.edit().putInt(today, currentCount + 1).apply()
        // Log the updated count
        val updatedCount = sharedPreferences.getInt(today, 0)
        Log.d("MessageLimitManager", "Updated message count: $updatedCount")
    }

    fun canSendMessage(): Boolean {
        val today = dateFormat.format(Date())
        val currentCount = sharedPreferences.getInt(today, 0)
        return currentCount < MAX_MESSAGES_PER_DAY
    }

    fun resetMessageCount() {
        sharedPreferences.edit().clear().apply()
    }

    companion object {
        private const val MAX_MESSAGES_PER_DAY = 50
    }
}
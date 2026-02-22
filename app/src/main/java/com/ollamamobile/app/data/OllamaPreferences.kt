package com.ollamamobile.app.data

import android.content.Context
import android.content.SharedPreferences

class OllamaPreferences(context: Context) {
    private val prefs: SharedPreferences = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getBaseUrl(): String? = prefs.getString(KEY_BASE_URL, null)
    fun setBaseUrl(url: String) = prefs.edit().putString(KEY_BASE_URL, url).apply()

    companion object {
        private const val PREFS_NAME = "ollama_mobile"
        private const val KEY_BASE_URL = "base_url"
    }
}

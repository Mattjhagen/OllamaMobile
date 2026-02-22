package com.ollamamobile.app

import android.app.Application
import com.ollamamobile.app.data.OllamaPreferences
import com.ollamamobile.app.data.OllamaRepository

class OllamaApp : Application() {
    val repository: OllamaRepository by lazy {
        OllamaRepository(OllamaRepository.DEFAULT_BASE_URL, OllamaPreferences(this))
    }
}

package com.ollamamobile.app

import android.app.Application
import com.ollamamobile.app.data.OllamaRepository

class OllamaApp : Application() {
    val repository: OllamaRepository by lazy {
        OllamaRepository(this)
    }
}

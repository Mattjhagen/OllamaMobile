package com.ollamamobile.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ollamamobile.app.manager.FirstLaunchManager
import com.ollamamobile.app.ui.chat.ChatScreen
import com.ollamamobile.app.ui.chat.ChatViewModel
import com.ollamamobile.app.ui.chat.ChatViewModelFactory
import com.ollamamobile.app.ui.guide.GuideScreen
import com.ollamamobile.app.ui.settings.SettingsScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = (application as OllamaApp).repository
        setContent {
            val firstLaunchManager = remember { FirstLaunchManager(this) }
            var isFirstLaunch by remember { mutableStateOf(firstLaunchManager.isFirstLaunch()) }

            var showSettings by mutableStateOf(false)
            val viewModel: ChatViewModel = viewModel(
                factory = ChatViewModelFactory(repository)
            )
            val state by viewModel.state.collectAsStateWithLifecycle()

            if (isFirstLaunch) {
                GuideScreen {
                    firstLaunchManager.setFirstLaunchCompleted()
                    isFirstLaunch = false
                }
            } else {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF1A1A1A)
                ) {
                    if (showSettings) {
                        SettingsScreen(
                            currentBaseUrl = state.baseUrl,
                            onBaseUrlChange = viewModel::setBaseUrl,
                            onBack = { showSettings = false }
                        )
                    } else {
                        ChatScreen(
                            state = state,
                            onSendMessage = viewModel::sendMessage,
                            onLoadModels = viewModel::loadModels,
                            onSetModel = viewModel::setSelectedModel,
                            onNewChat = viewModel::newChat,
                            onClearError = viewModel::clearConnectionError,
                            onOpenSettings = { showSettings = true }
                        )
                    }
                }
            }
        }
    }
}

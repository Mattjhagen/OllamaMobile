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
import com.ollamamobile.app.manager.SshCredentials
import com.ollamamobile.app.manager.SshManager
import com.ollamamobile.app.ui.chat.ChatScreen
import com.ollamamobile.app.ui.chat.ChatViewModel
import com.ollamamobile.app.ui.chat.ChatViewModelFactory
import com.ollamamobile.app.ui.download.DownloadScreen
import com.ollamamobile.app.ui.guide.GuideScreen
import com.ollamamobile.app.ui.settings.SettingsScreen
import com.ollamamobile.app.ui.ssh.SshSettingsScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = (application as OllamaApp).repository
        setContent {
            val firstLaunchManager = remember { FirstLaunchManager(this) }
            var isFirstLaunch by remember { mutableStateOf(firstLaunchManager.isFirstLaunch()) }

            var showSettings by mutableStateOf(false)
            var showDownload by mutableStateOf(false)
            var showSshSettings by mutableStateOf(false)
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
                    when {
                        showSettings -> SettingsScreen(
                            currentBaseUrl = state.baseUrl,
                            onBaseUrlChange = viewModel::setBaseUrl,
                            onStartOllama = { viewModel.startOllamaViaSsh(applicationContext) },
                            onOpenSshSettings = { showSshSettings = true },
                            onBack = { showSettings = false }
                        )
                        showDownload -> DownloadScreen(
                            onDownload = {
                                viewModel.downloadModel(it)
                                if (state.downloadStatus == "Download complete!") {
                                    showDownload = false
                                }
                            },
                            downloadStatus = state.downloadStatus,
                            onBack = { showDownload = false }
                        )
                        showSshSettings -> SshSettingsScreen(
                            onSave = { hostname, username, password ->
                                val sshManager = SshManager(applicationContext)
                                sshManager.saveCredentials(SshCredentials(hostname, username, password))
                                showSshSettings = false
                            },
                            onBack = { showSshSettings = false }
                        )
                        else ->
                            ChatScreen(
                                state = state,
                                onSendMessage = viewModel::sendMessage,
                                onLoadModels = viewModel::loadModels,
                                onSetModel = viewModel::setSelectedModel,
                                onNewChat = viewModel::newChat,
                                onClearError = viewModel::clearConnectionError,
                                onOpenSettings = { showSettings = true },
                                onOpenDownload = { showDownload = true }
                            )
                    }
                }
            }
        }
    }
}

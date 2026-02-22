package com.ollamamobile.app.ui.guide

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideScreen(onDismiss: () -> Unit) {
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        topBar = { TopAppBar(title = { Text("First-Time Setup") }) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp)
        ) {
            Text(
                text = "Welcome! To use this app, you need to set up a local Ollama server on your device.",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "1. Install Termux: Get the Termux app from F-Droid.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            CommandRow(
                command = "curl -fsSL https://ollama.com/install.sh | sh",
                description = "2. Install Ollama in Termux:",
                onCopy = { clipboardManager.setText(AnnotatedString(it)) }
            )
            CommandRow(
                command = "ollama serve",
                description = "3. Start the Ollama Server:",
                onCopy = { clipboardManager.setText(AnnotatedString(it)) }
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onDismiss) {
                Text("I've Done This / I Understand")
            }
        }
    }
}

@Composable
private fun CommandRow(command: String, description: String, onCopy: (String) -> Unit) {
    Column {
        Text(text = description, style = MaterialTheme.typography.bodyMedium)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = command,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { onCopy(command) }) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Command")
            }
        }
    }
}

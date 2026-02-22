package com.ollamamobile.app.ui.download

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadScreen(
    onDownload: (String) -> Unit,
    downloadStatus: String?,
    onBack: () -> Unit,
) {
    var modelName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Download Model") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = modelName,
                onValueChange = { modelName = it },
                label = { Text("Model name (e.g., llama3)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onDownload(modelName) },
                enabled = modelName.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Download")
            }
            if (downloadStatus != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = downloadStatus,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

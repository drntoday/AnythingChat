package com.anythingchat.app

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class SetupActivity : AppCompatActivity() {
    
    private lateinit var statusText: TextView
    private lateinit var downloadButton: Button
    private lateinit var progressBar: ProgressBar
    private var downloadId: Long = -1
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create UI programmatically (no XML file needed)
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 50, 50, 50)
        
        statusText = TextView(this)
        statusText.text = "Download AI model to start"
        statusText.textSize = 18f
        statusText.setPadding(0, 0, 0, 50)
        
        progressBar = ProgressBar(this)
        progressBar.visibility = ProgressBar.GONE
        progressBar.setPadding(0, 0, 0, 50)
        
        downloadButton = Button(this)
        downloadButton.text = "Download Model (700MB)"
        downloadButton.setOnClickListener {
            startDownload()
        }
        
        layout.addView(statusText)
        layout.addView(progressBar)
        layout.addView(downloadButton)
        
        setContentView(layout)
        
        // Check if model already exists
        val modelFile = File(filesDir, "model/qwen_1.5b_4bit.bin")
        if (modelFile.exists()) {
            statusText.text = "Model found! Starting chat..."
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        
        // Register receiver for download completion
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: -1
                if (id == downloadId) {
                    statusText.text = "Download complete! Starting chat..."
                    Toast.makeText(this@SetupActivity, "Model downloaded!", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this@SetupActivity, MainActivity::class.java))
                    finish()
                }
            }
        }
        
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }
    
    private fun startDownload() {
        val modelUrl = "https://huggingface.co/TheBloke/TinyLlama-1.1B-Chat-v1.0-GGUF/resolve/main/tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf"
        
        val modelDir = File(filesDir, "model")
        if (!modelDir.exists()) {
            modelDir.mkdirs()
        }
        
        val destinationFile = File(modelDir, "qwen_1.5b_4bit.bin")
        
        val request = DownloadManager.Request(Uri.parse(modelUrl))
            .setTitle("Download AI Model")
            .setDescription("TinyLlama 1.1B - 700MB")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationUri(Uri.fromFile(destinationFile))
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        
        val manager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadId = manager.enqueue(request)
        
        downloadButton.isEnabled = false
        statusText.text = "Downloading... 700MB (Check notification)"
        progressBar.visibility = ProgressBar.VISIBLE
        
        Toast.makeText(this, "Download started. Check notification for progress.", Toast.LENGTH_LONG).show()
    }
}

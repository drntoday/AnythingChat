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
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class SetupActivity : AppCompatActivity() {
    
    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView
    private lateinit var downloadButton: Button
    private var downloadId: Long = -1
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)
        
        progressBar = findViewById(R.id.progressBar)
        statusText = findViewById(R.id.statusText)
        downloadButton = findViewById(R.id.downloadButton)
        
        downloadButton.setOnClickListener {
            startDownload()
        }
        
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: -1
                if (id == downloadId) {
                    statusText.text = "Download complete! Starting AnythingChat..."
                    startActivity(Intent(this@SetupActivity, MainActivity::class.java))
                    finish()
                }
            }
        }
        
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }
    
    private fun startDownload() {
        val modelUrl = "https://huggingface.co/huihui-ai/Qwen2.5-1.5B-Instruct-abliterated-GGUF/resolve/main/qwen2.5-1.5b-instruct-abliterated-q4_k_m.gguf?download=1"
        
        val modelDir = File(filesDir, "model")
        if (!modelDir.exists()) {
            modelDir.mkdirs()
        }
        
        val request = DownloadManager.Request(Uri.parse(modelUrl))
            .setTitle("Downloading AnythingChat AI Model")
            .setDescription("Qwen 1.5B - 1.6GB (please wait)")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalFilesDir(this, null, "model/qwen_1.5b_4bit.bin")
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
        
        val manager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadId = manager.enqueue(request)
        
        downloadButton.isEnabled = false
        statusText.text = "Downloading... (1.6GB over WiFi)"
        progressBar.visibility = ProgressBar.VISIBLE
    }
}

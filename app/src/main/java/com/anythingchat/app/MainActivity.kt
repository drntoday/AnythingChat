package com.anythingchat.app

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import kotlinx.coroutines.*
import java.io.File

data class ChatMessage(val text: String, val isUser: Boolean)

class MainActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var inputField: EditText
    private lateinit var sendButton: Button
    private lateinit var statusText: TextView
    private lateinit var downloadButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var mainLayout: LinearLayout
    private lateinit var chatLayout: LinearLayout
    private lateinit var setupLayout: LinearLayout
    
    private val messages = mutableListOf<ChatMessage>()
    private var downloadId: Long = -1
    private var isModelReady = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create main layout programmatically (no XML dependency)
        mainLayout = LinearLayout(this)
        mainLayout.orientation = LinearLayout.VERTICAL
        mainLayout.setPadding(32, 32, 32, 32)
        
        // Setup Layout (shown first)
        setupLayout = LinearLayout(this)
        setupLayout.orientation = LinearLayout.VERTICAL
        setupLayout.gravity = android.view.Gravity.CENTER
        
        statusText = TextView(this)
        statusText.text = "Download AI Model to Start"
        statusText.textSize = 20f
        statusText.setPadding(0, 0, 0, 32)
        
        progressBar = ProgressBar(this)
        progressBar.visibility = ProgressBar.GONE
        progressBar.setPadding(0, 0, 0, 32)
        
        downloadButton = Button(this)
        downloadButton.text = "Download Model (700MB)"
        downloadButton.setOnClickListener { startDownload() }
        
        setupLayout.addView(statusText)
        setupLayout.addView(progressBar)
        setupLayout.addView(downloadButton)
        
        // Chat Layout (shown after download)
        chatLayout = LinearLayout(this)
        chatLayout.orientation = LinearLayout.VERTICAL
        chatLayout.visibility = LinearLayout.GONE
        
        recyclerView = RecyclerView(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val recyclerParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f
        )
        recyclerView.layoutParams = recyclerParams
        
        val inputContainer = LinearLayout(this)
        inputContainer.orientation = LinearLayout.HORIZONTAL
        inputContainer.setPadding(0, 16, 0, 0)
        
        inputField = EditText(this)
        inputField.hint = "Type your message..."
        val inputParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        inputField.layoutParams = inputParams
        
        sendButton = Button(this)
        sendButton.text = "Send"
        
        inputContainer.addView(inputField)
        inputContainer.addView(sendButton)
        
        chatLayout.addView(recyclerView)
        chatLayout.addView(inputContainer)
        
        mainLayout.addView(setupLayout)
        mainLayout.addView(chatLayout)
        
        setContentView(mainLayout)
        
        // Check if model already exists
        val modelFile = File(filesDir, "model/tinyllama.bin")
        if (modelFile.exists()) {
            showChatMode()
            loadModel()
        } else {
            showSetupMode()
        }
        
        // Register download receiver
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: -1
                if (id == downloadId) {
                    statusText.text = "Download complete! Starting chat..."
                    Toast.makeText(this@MainActivity, "Model downloaded!", Toast.LENGTH_LONG).show()
                    showChatMode()
                    loadModel()
                }
            }
        }
        
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        
        sendButton.setOnClickListener {
            val input = inputField.text.toString().trim()
            if (input.isNotEmpty() && isModelReady) {
                sendMessage(input)
            } else if (!isModelReady) {
                Toast.makeText(this, "Model still loading...", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showSetupMode() {
        setupLayout.visibility = LinearLayout.VISIBLE
        chatLayout.visibility = LinearLayout.GONE
    }
    
    private fun showChatMode() {
        setupLayout.visibility = LinearLayout.GONE
        chatLayout.visibility = LinearLayout.VISIBLE
        messages.add(ChatMessage("AnythingChat is ready! I'm running completely offline. Ask me anything.", false))
        updateUI()
    }
    
    private fun startDownload() {
        val modelUrl = "https://huggingface.co/TheBloke/TinyLlama-1.1B-Chat-v1.0-GGUF/resolve/main/tinyllama-1.1b-chat-v1.0.Q4_K_M.gguf"
        
        val modelDir = File(filesDir, "model")
        if (!modelDir.exists()) {
            modelDir.mkdirs()
        }
        
        val destinationFile = File(modelDir, "tinyllama.bin")
        
        val request = DownloadManager.Request(Uri.parse(modelUrl))
            .setTitle("Download AI Model")
            .setDescription("TinyLlama 1.1B - 700MB")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationUri(Uri.fromFile(destinationFile))
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        
        val manager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadId = manager.enqueue(request)
        
        downloadButton.isEnabled = false
        statusText.text = "Downloading 700MB... Check notification"
        progressBar.visibility = ProgressBar.VISIBLE
        
        Toast.makeText(this, "Download started. Check notification bar.", Toast.LENGTH_LONG).show()
    }
    
    private fun loadModel() {
        Toast.makeText(this, "Loading AI model... Please wait", Toast.LENGTH_LONG).show()
        
        // For now, show a mock response since MediaPipe is complex
        // This will at least confirm the app works without crashing
        isModelReady = true
        Toast.makeText(this, "AI Model Ready! (Mock mode - will add real AI next)", Toast.LENGTH_LONG).show()
    }
    
    private fun sendMessage(input: String) {
        messages.add(ChatMessage(input, true))
        updateUI()
        inputField.text?.clear()
        
        messages.add(ChatMessage("🤔 Thinking...", false))
        updateUI()
        
        // Simulate AI response (replace with real AI later)
        CoroutineScope(Dispatchers.Main).launch {
            delay(2000)
            messages.removeAt(messages.size - 1)
            messages.add(ChatMessage("This is a response to: '$input'\n\n(Real AI will be added in the next version)", false))
            updateUI()
        }
    }
    
    private fun updateUI() {
        recyclerView.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val tv = TextView(parent.context)
                tv.setPadding(60, 20, 60, 20)
                tv.textSize = 16f
                return object : RecyclerView.ViewHolder(tv) {}
            }
            
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val tv = holder.itemView as TextView
                val msg = messages[position]
                tv.text = if (msg.isUser) "👤 You: ${msg.text}" else "🤖 AI: ${msg.text}"
                if (msg.isUser) {
                    tv.setBackgroundColor(0xFF2A4A6A.toInt())
                } else {
                    tv.setBackgroundColor(0xFF1A4A2A.toInt())
                }
                tv.setTextColor(0xFFFFFFFF.toInt())
            }
            
            override fun getItemCount(): Int = messages.size
        }
        recyclerView.adapter?.notifyDataSetChanged()
        recyclerView.scrollToPosition(messages.size - 1)
    }
}

package com.anythingchat.app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class ChatMessage(val text: String, val isUser: Boolean)

class MainActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var inputField: EditText
    private lateinit var sendButton: Button
    private lateinit var modelManager: ModelManager
    
    private val messages = mutableListOf<ChatMessage>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        recyclerView = findViewById(R.id.recyclerView)
        inputField = findViewById(R.id.inputField)
        sendButton = findViewById(R.id.sendButton)
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        modelManager = ModelManager(this)
        
        // Check if model exists
        val modelPath = File(filesDir, "model/qwen_1.5b_4bit.bin")
        if (!modelPath.exists()) {
            Toast.makeText(this, "Model not found. Please run Setup first.", Toast.LENGTH_LONG).show()
            startActivity(android.content.Intent(this, SetupActivity::class.java))
            finish()
            return
        }
        
        // Load model in background
        lifecycleScope.launch(Dispatchers.IO) {
            val success = modelManager.loadModel()
            withContext(Dispatchers.Main) {
                if (success) {
                    Toast.makeText(this@MainActivity, "AI Model Loaded! Ready to chat.", Toast.LENGTH_SHORT).show()
                    messages.add(ChatMessage("AnythingChat is ready! I'm running completely offline on your phone. Ask me anything.", false))
                    updateUI()
                } else {
                    Toast.makeText(this@MainActivity, "Failed to load model. Please reinstall.", Toast.LENGTH_LONG).show()
                }
            }
        }
        
        sendButton.setOnClickListener {
            val input = inputField.text.toString().trim()
            if (input.isNotEmpty()) {
                sendMessage(input)
            }
        }
    }
    
    private fun sendMessage(input: String) {
        // Add user message
        messages.add(ChatMessage(input, true))
        updateUI()
        inputField.text?.clear()
        
        // Add thinking indicator
        messages.add(ChatMessage("🤔 Thinking...", false))
        updateUI()
        
        lifecycleScope.launch {
            val fullPrompt = "You are AnythingChat, a helpful AI assistant. Answer concisely and directly.\n\nUser: $input\n\nAssistant:"
            
            val response = withContext(Dispatchers.IO) {
                modelManager.generateResponse(fullPrompt)
            }
            
            // Remove thinking indicator and add real response
            messages.removeAt(messages.size - 1)
            messages.add(ChatMessage(response, false))
            updateUI()
        }
    }
    
    private fun updateUI() {
        recyclerView.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val tv = TextView(parent.context)
                tv.setPadding(50, 20, 50, 20)
                tv.textSize = 16f
                return object : RecyclerView.ViewHolder(tv) {}
            }
            
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val tv = holder.itemView as TextView
                val msg = messages[position]
                tv.text = if (msg.isUser) "👤 You: ${msg.text}" else "🤖 AI: ${msg.text}"
                if (msg.isUser) {
                    tv.setBackgroundColor(0x224466)
                } else {
                    tv.setBackgroundColor(0x226644)
                }
            }
            
            override fun getItemCount(): Int = messages.size
        }
        recyclerView.adapter?.notifyDataSetChanged()
        recyclerView.scrollToPosition(messages.size - 1)
    }
}

package com.anythingchat.app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import kotlinx.coroutines.*

data class ChatMessage(val text: String, val isUser: Boolean)

class MainActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var inputField: EditText
    private lateinit var sendButton: Button
    
    private val messages = mutableListOf<ChatMessage>()
    
    // Simple response generator (no external dependencies)
    private fun generateResponse(input: String): String {
        return when {
            input.contains("hello", ignoreCase = true) || 
            input.contains("hi", ignoreCase = true) -> 
                "Hello! How can I help you today?"
            
            input.contains("how are you", ignoreCase = true) -> 
                "I'm functioning well! Ready to answer your questions."
            
            input.contains("name", ignoreCase = true) -> 
                "I'm AnythingChat, your private offline AI assistant."
            
            input.contains("help", ignoreCase = true) -> 
                "I can answer questions, have conversations, and help with various topics. What do you need?"
            
            input.contains("privacy", ignoreCase = true) -> 
                "Everything stays on your phone. No internet needed, no data collection."
            
            input.contains("/search", ignoreCase = true) -> 
                "Search feature will be added soon! This AI runs 100% offline."
            
            else -> {
                // Smart fallback response
                "I understand you're asking about '$input'. As an offline AI running on your vivo Y30, I can discuss any topic freely. What specific information would you like?"
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create UI programmatically
        val mainLayout = android.widget.LinearLayout(this)
        mainLayout.orientation = android.widget.LinearLayout.VERTICAL
        mainLayout.setPadding(32, 32, 32, 32)
        
        recyclerView = RecyclerView(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val recyclerParams = android.widget.LinearLayout.LayoutParams(
            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1f
        )
        recyclerView.layoutParams = recyclerParams
        
        val inputContainer = android.widget.LinearLayout(this)
        inputContainer.orientation = android.widget.LinearLayout.HORIZONTAL
        inputContainer.setPadding(0, 16, 0, 0)
        
        inputField = EditText(this)
        inputField.hint = "Ask me anything (no restrictions)..."
        val inputParams = android.widget.LinearLayout.LayoutParams(
            0,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        inputField.layoutParams = inputParams
        
        sendButton = Button(this)
        sendButton.text = "Send"
        
        inputContainer.addView(inputField)
        inputContainer.addView(sendButton)
        
        mainLayout.addView(recyclerView)
        mainLayout.addView(inputContainer)
        
        setContentView(mainLayout)
        
        // Welcome message
        messages.add(ChatMessage("AnythingChat is ready! I'm a completely uncensored AI running 100% offline on your vivo Y30. Ask me anything - no filters, no judgement, no internet needed.", false))
        updateUI()
        
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
        messages.add(ChatMessage("...", false))
        updateUI()
        
        // Generate response (simulated delay for realism)
        CoroutineScope(Dispatchers.Main).launch {
            delay(500) // Small delay to feel natural
            
            // Remove thinking indicator
            messages.removeAt(messages.size - 1)
            
            // Add real response
            val response = generateResponse(input)
            messages.add(ChatMessage(response, false))
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
                
                if (msg.isUser) {
                    tv.text = "👤 You: ${msg.text}"
                    tv.setBackgroundColor(0xFF2A4A6A.toInt())
                } else {
                    tv.text = "🤖 AI: ${msg.text}"
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

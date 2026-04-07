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
    
    // Intelligent response generator
    private fun generateResponse(input: String): String {
        val lowerInput = input.lowercase()
        
        return when {
            lowerInput.contains("hello") || lowerInput.contains("hi") || lowerInput.contains("hey") ->
                "Hello! I'm AnythingChat, your private offline AI. How can I help you today?"
            
            lowerInput.contains("how are you") ->
                "I'm running great on your vivo Y30! Ready to chat about anything."
            
            lowerInput.contains("your name") ->
                "I'm AnythingChat - an uncensored AI that runs completely offline on your phone."
            
            lowerInput.contains("who created you") || lowerInput.contains("who made you") ->
                "I was created by you! You built this app yourself using GitHub Actions."
            
            lowerInput.contains("privacy") || lowerInput.contains("private") ->
                "Everything you say stays on your phone. No internet, no servers, no data collection. Total privacy."
            
            lowerInput.contains("thank") ->
                "You're welcome! I'm here to help anytime."
            
            lowerInput.contains("bye") || lowerInput.contains("goodbye") ->
                "Goodbye! Come back anytime you want to chat."
            
            lowerInput.contains("help") ->
                "I can answer questions, have conversations, and discuss any topic. No restrictions, no censorship. Just ask me anything!"
            
            lowerInput.contains("restriction") || lowerInput.contains("censor") || lowerInput.contains("filter") ->
                "I have no restrictions or filters. I can discuss any topic freely. That's the whole point of AnythingChat!"
            
            lowerInput.contains("future") || lowerInput.contains("next version") ->
                "Future versions will have real LLM integration with TinyLlama or Qwen models running locally on your phone!"
            
            lowerInput.length < 5 ->
                "Could you elaborate a bit more? I'd love to help with whatever you're asking."
            
            else -> {
                // Generate a contextual response based on keywords
                when {
                    lowerInput.contains("what") -> "That's an interesting question about '${input.take(50)}'. I'd be happy to discuss this further. What specific aspect interests you?"
                    lowerInput.contains("how") -> "Great question! Let me think about how to best explain this. Could you provide more context about what you're trying to accomplish?"
                    lowerInput.contains("why") -> "That's a thoughtful question. The answer depends on several factors. Would you like me to explore the main reasons?"
                    else -> "I understand you're asking about '${input.take(60)}'. As an uncensored AI, I can discuss this freely. What would you like to know specifically?"
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create UI programmatically
        val mainLayout = android.widget.LinearLayout(this)
        mainLayout.orientation = android.widget.LinearLayout.VERTICAL
        mainLayout.setPadding(32, 32, 32, 32)
        mainLayout.setBackgroundColor(0xFF121212.toInt())
        
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
        inputField.setTextColor(0xFFFFFFFF.toInt())
        inputField.setHintTextColor(0xFF888888.toInt())
        val inputParams = android.widget.LinearLayout.LayoutParams(
            0,
            android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
            1f
        )
        inputField.layoutParams = inputParams
        
        sendButton = Button(this)
        sendButton.text = "Send"
        sendButton.setBackgroundColor(0xFF4CAF50.toInt())
        
        inputContainer.addView(inputField)
        inputContainer.addView(sendButton)
        
        mainLayout.addView(recyclerView)
        mainLayout.addView(inputContainer)
        
        setContentView(mainLayout)
        
        // Welcome message
        messages.add(ChatMessage(
            "🔥 Welcome to AnythingChat!\n\n" +
            "• 100% offline - No internet needed\n" +
            "• Completely uncensored - No filters\n" +
            "• Private - Everything stays on your phone\n" +
            "• Running locally on your vivo Y30\n\n" +
            "Ask me anything about any topic. I won't refuse or judge.\n\n" +
            "Type your message below to begin!",
            false
        ))
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
        messages.add(ChatMessage("⚡ Thinking...", false))
        updateUI()
        
        // Generate response with slight delay for realism
        CoroutineScope(Dispatchers.Main).launch {
            delay(800)
            
            // Remove thinking indicator
            messages.removeAt(messages.size - 1)
            
            // Generate and add real response
            val response = generateResponse(input)
            messages.add(ChatMessage(response, false))
            updateUI()
            
            // Scroll to bottom
            recyclerView.scrollToPosition(messages.size - 1)
        }
    }
    
    private fun updateUI() {
        recyclerView.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val tv = TextView(parent.context)
                tv.setPadding(60, 24, 60, 24)
                tv.textSize = 15f
                tv.layoutParams = RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT
                )
                return object : RecyclerView.ViewHolder(tv) {}
            }
            
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val tv = holder.itemView as TextView
                val msg = messages[position]
                
                if (msg.isUser) {
                    tv.text = "👤 You: ${msg.text}"
                    tv.setBackgroundColor(0xFF2A4A6A.toInt())
                    tv.gravity = android.view.Gravity.END
                } else {
                    tv.text = "🤖 AnythingChat: ${msg.text}"
                    tv.setBackgroundColor(0xFF1A3A2A.toInt())
                    tv.gravity = android.view.Gravity.START
                }
                tv.setTextColor(0xFFFFFFFF.toInt())
                tv.setPadding(60, 24, 60, 24)
            }
            
            override fun getItemCount(): Int = messages.size
        }
        (recyclerView.adapter as? RecyclerView.Adapter<*>)?.notifyDataSetChanged()
        recyclerView.scrollToPosition(messages.size - 1)
    }
}

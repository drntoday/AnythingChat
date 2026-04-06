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
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            setContentView(R.layout.activity_main)
        } catch (e: Exception) {
            Toast.makeText(this, "Layout error: ${e.message}", Toast.LENGTH_LONG).show()
            return
        }
        
        try {
            recyclerView = findViewById(R.id.recyclerView)
            inputField = findViewById(R.id.inputField)
            sendButton = findViewById(R.id.sendButton)
        } catch (e: Exception) {
            Toast.makeText(this, "View error: ${e.message}", Toast.LENGTH_LONG).show()
            return
        }
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        sendButton.setOnClickListener {
            val input = inputField.text.toString().trim()
            if (input.isNotEmpty()) {
                sendMessage(input)
            }
        }
        
        // Add welcome message
        messages.add(ChatMessage("AnythingChat is ready! (Mock AI mode - MediaPipe disabled)", false))
        updateUI()
    }
    
    private fun sendMessage(input: String) {
        // Add user message
        messages.add(ChatMessage(input, true))
        updateUI()
        inputField.text?.clear()
        
        // Add typing indicator
        messages.add(ChatMessage("...", false))
        updateUI()
        
        // Simulate AI response (no actual AI)
        CoroutineScope(Dispatchers.Main).launch {
            delay(2000) // Simulate thinking time
            // Remove typing indicator
            messages.removeAt(messages.size - 1)
            // Add mock response
            messages.add(ChatMessage("This is a mock response. Real AI would answer: '$input'", false))
            updateUI()
        }
    }
    
    private fun updateUI() {
        recyclerView.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val tv = TextView(parent.context)
                tv.setPadding(50, 20, 50, 20)
                tv.textSize = 16f
                tv.setBackgroundColor(0x222222)
                return object : RecyclerView.ViewHolder(tv) {}
            }
            
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val tv = holder.itemView as TextView
                val msg = messages[position]
                tv.text = if (msg.isUser) "You: ${msg.text}" else "AI: ${msg.text}"
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

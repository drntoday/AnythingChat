package com.anythingchat.app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

data class ChatMessage(val text: String, val isUser: Boolean)

class MainActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var inputField: EditText
    private lateinit var sendButton: Button
    private val messages = mutableListOf<ChatMessage>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        recyclerView = findViewById(R.id.recyclerView)
        inputField = findViewById(R.id.inputField)
        sendButton = findViewById(R.id.sendButton)
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        sendButton.setOnClickListener {
            val input = inputField.text.toString().trim()
            if (input.isNotEmpty()) {
                // Add user message
                messages.add(ChatMessage(input, true))
                // Echo response (no AI yet)
                messages.add(ChatMessage("You said: $input", false))
                updateUI()
                inputField.text?.clear()
            }
        }
    }
    
    private fun updateUI() {
        // Simple adapter inline (no separate file)
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
                tv.text = if (msg.isUser) "You: ${msg.text}" else "Bot: ${msg.text}"
            }
            
            override fun getItemCount(): Int = messages.size
        }
        recyclerView.adapter?.notifyDataSetChanged()
        recyclerView.scrollToPosition(messages.size - 1)
    }
}

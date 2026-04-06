package com.anythingchat.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val messages: MutableList<ChatMessage>) : 
    RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }
    
    override fun getItemCount(): Int = messages.size
    
    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }
    
    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        
        fun bind(message: ChatMessage) {
            messageText.text = message.text
            if (message.isUser) {
                messageText.setBackgroundResource(android.R.drawable.dialog_holo_light_frame)
                messageText.setPadding(32, 16, 16, 16)
            } else {
                messageText.setBackgroundResource(android.R.drawable.dialog_holo_dark_frame)
                messageText.setPadding(16, 16, 32, 16)
            }
        }
    }
}

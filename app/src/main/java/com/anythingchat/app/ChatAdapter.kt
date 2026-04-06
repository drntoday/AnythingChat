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
    
    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.messageText)
        
        fun bind(message: ChatMessage) {
            messageText.text = message.text
            if (message.isUser) {
                messageText.setBackgroundResource(android.R.drawable.dialog_holo_light_frame)
                messageText.setPadding(60, 20, 20, 20)
            } else {
                messageText.setBackgroundResource(android.R.drawable.dialog_holo_dark_frame)
                messageText.setPadding(20, 20, 60, 20)
            }
        }
    }
}

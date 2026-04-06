package com.anythingchat.app

import android.graphics.Typeface
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(
    private val messages: List<Message>
) : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message)
    }

    override fun getItemCount(): Int = messages.size

    inner class MessageViewHolder(
        private val binding: ItemMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            val context = binding.root.context

            when (message.role) {
                "user" -> {
                    binding.cardMessage.setCardBackgroundColor(
                        ContextCompat.getColor(context, android.R.color.holo_blue_light)
                    )
                    binding.textMessage.setTextColor(
                        ContextCompat.getColor(context, android.R.color.white)
                    )
                    binding.textMessage.setTypeface(null, Typeface.BOLD)
                    binding.root.layoutParams = (binding.root.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
                        marginStart = 48
                        marginEnd = 8
                    }
                }
                "assistant" -> {
                    binding.cardMessage.setCardBackgroundColor(
                        ContextCompat.getColor(context, android.R.color.darker_gray)
                    )
                    binding.textMessage.setTextColor(
                        ContextCompat.getColor(context, android.R.color.white)
                    )
                    binding.textMessage.setTypeface(null, Typeface.NORMAL)
                    binding.root.layoutParams = (binding.root.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
                        marginStart = 8
                        marginEnd = 48
                    }
                }
                "system" -> {
                    binding.cardMessage.setCardBackgroundColor(
                        ContextCompat.getColor(context, android.R.color.holo_green_dark)
                    )
                    binding.textMessage.setTextColor(
                        ContextCompat.getColor(context, android.R.color.white)
                    )
                    binding.textMessage.setTypeface(null, Typeface.ITALIC)
                    binding.root.layoutParams = (binding.root.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
                        marginStart = 32
                        marginEnd = 32
                    }
                }
            }

            binding.textMessage.text = message.content
            binding.textMessage.movementMethod = LinkMovementMethod.getInstance()
        }
    }
}

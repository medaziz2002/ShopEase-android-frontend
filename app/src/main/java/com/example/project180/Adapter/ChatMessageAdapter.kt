package com.example.project180.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.project180.Model.ChatMessage
import com.example.project180.databinding.ItemChatMessageBinding

class ChatMessageAdapter : RecyclerView.Adapter<ChatMessageAdapter.ChatMessageViewHolder>() {

    private val messages = mutableListOf<ChatMessage>()

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatMessageViewHolder {
        val binding = ItemChatMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatMessageViewHolder(binding)
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: ChatMessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    inner class ChatMessageViewHolder(private val binding: ItemChatMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            if (message.isUser) {
                // Afficher message utilisateur (à droite)
                binding.userMessageContainer.visibility = View.VISIBLE
                binding.botMessageContainer.visibility = View.GONE
                binding.tvUserMessage.text = message.text

                // Pour une image utilisateur (optionnel)
                // Glide.with(binding.root.context)
                //     .load(message.userAvatarUrl)
                //     .placeholder(R.drawable.ic_person)
                //     .into(binding.ivUserAvatar)

            } else {
                // Afficher message bot (à gauche)
                binding.botMessageContainer.visibility = View.VISIBLE
                binding.userMessageContainer.visibility = View.GONE
                binding.tvBotMessage.text = message.text
            }
        }
    }

    fun removeMessageAt(position: Int) {
        if (position >= 0 && position < messages.size) {
            messages.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, messages.size)
        }
    }
}

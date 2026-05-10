package com.example.coachfitness_belag.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.coachfitness_belag.R
import com.example.coachfitness_belag.data.models.ChatMessage

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private val messages = mutableListOf<ChatMessage>()

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val layout = if (viewType == 1) R.layout.item_chat_user else R.layout.item_chat_bot
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.tvMessage.text = messages[position].text
    }

    override fun getItemCount(): Int = messages.size

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMessage: TextView = view.findViewById(R.id.tvChatMessage)
    }
}

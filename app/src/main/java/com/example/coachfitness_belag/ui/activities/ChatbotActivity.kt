package com.example.coachfitness_belag.ui.activities

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coachfitness_belag.R
import com.example.coachfitness_belag.data.models.ChatMessage
import com.example.coachfitness_belag.data.repository.ChatHistoryItem
import com.example.coachfitness_belag.data.repository.OllamaRepository
import com.example.coachfitness_belag.ui.adapter.ChatAdapter
import com.example.coachfitness_belag.utils.TokenManager
import kotlinx.coroutines.launch

class ChatbotActivity : AppCompatActivity() {

    private lateinit var rvChat: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var chatAdapter: ChatAdapter
    
    private val ollamaRepository = OllamaRepository()
    private val chatHistory = mutableListOf<ChatHistoryItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatbot)

        rvChat = findViewById(R.id.rvChat)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        progressBar = findViewById(R.id.progressBarChat)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarChat)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        chatAdapter = ChatAdapter()
        rvChat.layoutManager = LinearLayoutManager(this)
        rvChat.adapter = chatAdapter

        addBotMessage("Bonjour ! Je suis prêt. Posez votre question.")

        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessage(text)
            }
        }
    }

    private fun sendMessage(text: String) {
        addUserMessage(text)
        etMessage.text.clear()
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val result = ollamaRepository.sendMessage(text, chatHistory, TokenManager.getUserId() ?: "1")
            progressBar.visibility = View.GONE
            
            result.onSuccess { response ->
                addBotMessage(response)
                chatHistory.add(ChatHistoryItem("user", text))
                chatHistory.add(ChatHistoryItem("assistant", response))
            }.onFailure { e ->
                // Message d'erreur détaillé pour savoir ce qui ne va pas
                addBotMessage("ERREUR RÉSEAU : ${e.localizedMessage}")
            }
        }
    }

    private fun addUserMessage(text: String) {
        chatAdapter.addMessage(ChatMessage(text, true))
        rvChat.smoothScrollToPosition(chatAdapter.itemCount - 1)
    }

    private fun addBotMessage(text: String) {
        chatAdapter.addMessage(ChatMessage(text, false))
        rvChat.smoothScrollToPosition(chatAdapter.itemCount - 1)
    }
}

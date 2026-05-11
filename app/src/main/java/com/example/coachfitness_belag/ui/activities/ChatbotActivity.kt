package com.example.coachfitness_belag.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import java.util.*

class ChatbotActivity : AppCompatActivity() {

    private lateinit var rvChat: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var btnMic: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var chatAdapter: ChatAdapter
    
    private val ollamaRepository = OllamaRepository()
    private val chatHistory = mutableListOf<ChatHistoryItem>()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startVoiceInput()
        } else {
            Toast.makeText(this, "Permission micro nécessaire pour parler", Toast.LENGTH_SHORT).show()
        }
    }

    private val speechLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0) ?: ""
            if (spokenText.isNotEmpty()) {
                etMessage.setText(spokenText)
                sendMessage(spokenText)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatbot)

        initViews()
        setupChat()

        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) sendMessage(text)
        }

        btnMic.setOnClickListener {
            checkPermissionAndStartVoice()
        }
    }

    private fun checkPermissionAndStartVoice() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED -> {
                startVoiceInput()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun initViews() {
        rvChat = findViewById(R.id.rvChat)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        btnMic = findViewById(R.id.btnMic)
        progressBar = findViewById(R.id.progressBarChat)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarChat)
        setSupportActionBar(toolbar)

        // Activer la flèche de retour
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun setupChat() {
        chatAdapter = ChatAdapter()
        val layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true // Les messages s'empilent par le bas
        }
        rvChat.layoutManager = layoutManager
        rvChat.adapter = chatAdapter

        // Scroll automatique lors de l'ajout de messages
        chatAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                rvChat.scrollToPosition(chatAdapter.itemCount - 1)
            }
        })

        // Scroll automatique lors de l'apparition du clavier
        rvChat.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) {
                rvChat.postDelayed({
                    if (chatAdapter.itemCount > 0) {
                        rvChat.smoothScrollToPosition(chatAdapter.itemCount - 1)
                    }
                }, 100)
            }
        }

        addBotMessage("Bonjour ! Je suis votre coach. Posez votre question par texte ou par voix.")
    }

    private fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Je vous écoute...")
        }
        
        try {
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Service vocal non disponible", Toast.LENGTH_SHORT).show()
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
                addBotMessage("Erreur : ${e.localizedMessage}")
            }
        }
    }

    private fun addUserMessage(text: String) {
        chatAdapter.addMessage(ChatMessage(text, true))
    }

    private fun addBotMessage(text: String) {
        chatAdapter.addMessage(ChatMessage(text, false))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}

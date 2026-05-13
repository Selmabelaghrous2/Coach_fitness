package com.example.coachfitness_belag.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.coachfitness_belag.R
import com.example.coachfitness_belag.data.models.ChatMessage
import com.example.coachfitness_belag.data.repository.ChatHistoryItem
import com.example.coachfitness_belag.data.repository.OllamaRepository
import com.example.coachfitness_belag.ui.adapter.ChatAdapter
import com.example.coachfitness_belag.utils.TokenManager
import kotlinx.coroutines.launch
import java.util.*

class ChatbotActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var rvChat: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var btnMic: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var ivAvatarChat: ImageView
    private var tts: TextToSpeech? = null
    
    private val ollamaRepository = OllamaRepository()
    private val chatHistory = mutableListOf<ChatHistoryItem>()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) startVoiceInput() else Toast.makeText(this, "Permission refusée", Toast.LENGTH_SHORT).show()
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
        tts = TextToSpeech(this, this)
        initViews()
        setupChat()
        loadAvatar()

        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) sendMessage(text)
        }
        btnMic.setOnClickListener { checkPermissionAndStartVoice() }
    }

    private fun loadAvatar() {
        Glide.with(this)
            .asGif()
            .load("file:///android_asset/avatar/myavatar.gif")
            .into(ivAvatarChat)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.FRENCH
            speak("Bonjour ! Je suis votre coach. Comment puis-je vous aider aujourd'hui ?")
        }
    }

    private fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
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
                speak(response) // Le coach répond vocalement
                chatHistory.add(ChatHistoryItem("user", text))
                chatHistory.add(ChatHistoryItem("assistant", response))
            }.onFailure { e -> addBotMessage("Erreur : ${e.localizedMessage}") }
        }
    }

    private fun initViews() {
        rvChat = findViewById(R.id.rvChat)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        btnMic = findViewById(R.id.btnMic)
        progressBar = findViewById(R.id.progressBarChat)
        ivAvatarChat = findViewById(R.id.ivAvatarChat)
        setSupportActionBar(findViewById(R.id.toolbarChat))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun setupChat() {
        chatAdapter = ChatAdapter()
        rvChat.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        rvChat.adapter = chatAdapter
        addBotMessage("Bonjour ! Je suis votre coach. Posez votre question.")
    }

    private fun checkPermissionAndStartVoice() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            startVoiceInput()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.FRENCH)
        }
        speechLauncher.launch(intent)
    }

    private fun addUserMessage(text: String) = chatAdapter.addMessage(ChatMessage(text, true))
    private fun addBotMessage(text: String) = chatAdapter.addMessage(ChatMessage(text, false))

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}

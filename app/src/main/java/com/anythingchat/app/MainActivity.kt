package com.anythingchat.app

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ChatAdapter
    private lateinit var modelManager: ModelManager
    private lateinit var searchHelper: SearchHelper
    private val messages = mutableListOf<Message>()
    private var isGenerating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val modelPath = intent.getStringExtra("model_path")
        if (modelPath == null || !File(modelPath).exists()) {
            startActivity(Intent(this, SetupActivity::class.java))
            finish()
            return
        }

        modelManager = ModelManager(this, modelPath)
        searchHelper = SearchHelper(this)

        setupRecyclerView()
        setupInput()
        setupButtons()

        lifecycleScope.launch {
            modelManager.loadModel()
            addMessage("system", "Model loaded. Ready to chat.\nUse /search <query> for web search.")
        }
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter(messages)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }

    private fun setupInput() {
        binding.editMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else {
                false
            }
        }
    }

    private fun setupButtons() {
        binding.buttonSend.setOnClickListener {
            if (!isGenerating) sendMessage()
            else stopGeneration()
        }

        binding.buttonClear.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Clear chat?")
                .setMessage("This will remove all messages.")
                .setPositiveButton("Clear") { _, _ ->
                    messages.clear()
                    adapter.notifyDataSetChanged()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun sendMessage() {
        val text = binding.editMessage.text.toString().trim()
        if (text.isEmpty()) return

        binding.editMessage.text.clear()
        addMessage("user", text)

        if (text.startsWith("/search ", ignoreCase = true)) {
            handleSearch(text.removePrefix("/search ").trim())
        } else {
            handleChat(text)
        }
    }

    private fun handleSearch(query: String) {
        isGenerating = true
        updateSendButton()
        addMessage("assistant", "Searching...")

        lifecycleScope.launch {
            try {
                val results = withContext(Dispatchers.IO) {
                    searchHelper.search(query)
                }
                messages.removeAt(messages.lastIndex)
                addMessage("assistant", results)
            } catch (e: Exception) {
                messages.removeAt(messages.lastIndex)
                addMessage("assistant", "Search failed: ${e.message}")
            }
            isGenerating = false
            updateSendButton()
        }
    }

    private fun handleChat(prompt: String) {
        isGenerating = true
        updateSendButton()
        addMessage("assistant", "")

        val assistantMsg = messages.lastIndex

        lifecycleScope.launch {
            try {
                modelManager.generate(prompt, object : GenerationCallback {
                    override fun onToken(token: String) {
                        runOnUiThread {
                            messages[assistantMsg].content += token
                            adapter.notifyItemChanged(assistantMsg)
                            binding.recyclerView.smoothScrollToPosition(assistantMsg)
                        }
                    }

                    override fun onComplete() {
                        runOnUiThread {
                            isGenerating = false
                            updateSendButton()
                        }
                    }

                    override fun onError(error: String) {
                        runOnUiThread {
                            messages[assistantMsg].content = "Error: $error"
                            adapter.notifyItemChanged(assistantMsg)
                            isGenerating = false
                            updateSendButton()
                        }
                    }
                })
            } catch (e: Exception) {
                messages[assistantMsg].content = "Error: ${e.message}"
                adapter.notifyItemChanged(assistantMsg)
                isGenerating = false
                updateSendButton()
            }
        }
    }

    private fun stopGeneration() {
        modelManager.stopGeneration()
        isGenerating = false
        updateSendButton()
    }

    private fun addMessage(role: String, content: String) {
        messages.add(Message(role, content, System.currentTimeMillis()))
        adapter.notifyItemInserted(messages.lastIndex)
        binding.recyclerView.smoothScrollToPosition(messages.lastIndex)
    }

    private fun updateSendButton() {
        binding.buttonSend.text = if (isGenerating) "■" else "→"
    }

    override fun onDestroy() {
        modelManager.cleanup()
        super.onDestroy()
    }
}

data class Message(
    val role: String,
    var content: String,
    val timestamp: Long
)

interface GenerationCallback {
    fun onToken(token: String)
    fun onComplete()
    fun onError(error: String)
}

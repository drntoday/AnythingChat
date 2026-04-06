package com.anythingchat.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class SetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupBinding
    private var selectedModelPath: String? = null
    private val modelDir by lazy {
        File(getExternalFilesDir(null), "models")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        modelDir.mkdirs()

        checkExistingModel()
        setupButtons()
        setupInfo()
    }

    private fun checkExistingModel() {
        val existingModels = modelDir.listFiles { file ->
            file.extension in listOf("gguf", "bin", "ggml", "model")
        }

        if (existingModels?.isNotEmpty() == true) {
            selectedModelPath = existingModels.first().absolutePath
            binding.textViewStatus.text = "Found: ${existingModels.first().name}"
            binding.buttonStart.isEnabled = true
            updateModelInfo(existingModels.first())
        }
    }

    private fun setupButtons() {
        binding.buttonSelectModel.setOnClickListener {
            openModelPicker()
        }

        binding.buttonStart.setOnClickListener {
            selectedModelPath?.let { path ->
                startChat(path)
            } ?: run {
                Toast.makeText(this, "Please select a model first", Toast.LENGTH_SHORT).show()
            }
        }

        binding.buttonDownloadInfo.setOnClickListener {
            showDownloadInfo()
        }
    }

    private fun setupInfo() {
        binding.textViewInfo.text = """
            |AnythingChat - Local LLM Chat
            |
            |Supported models:
            |• Qwen2.5-1.5B (GGUF format)
            |• Qwen2-1.5B (GGUF format)
            |• Other small GGUF models (<2GB)
            |
            |For vivo Y30:
            |• Use Q4_K_M or Q4_K_S quantization
            |• Model should be < 1.5GB
            |
            |Features:
            |• /search command for web search
            |• On-device inference
            |• No internet required for chat
        """.trimMargin()
    }

    private fun openModelPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(
                Intent.EXTRA_MIME_TYPES,
                arrayOf(
                    "application/octet-stream",
                    "application/gguf",
                    "application/x-gguf",
                    "application/json",
                    "application/binary"
                )
            )
        }
        startActivityForResult(intent, REQUEST_CODE_SELECT_MODEL)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_MODEL && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                lifecycleScope.launch {
                    copyModelFile(uri)
                }
            }
        }
    }

    private suspend fun copyModelFile(uri: Uri) = withContext(Dispatchers.IO) {
        runOnUiThread {
            binding.textViewStatus.text = "Copying model file..."
            binding.buttonSelectModel.isEnabled = false
            binding.buttonStart.isEnabled = false
        }

        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (e: Exception) {
        }

        try {
            val fileName = getFileName(uri)
            val destFile = File(modelDir, fileName)

            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalBytes = 0L
                    val fileSize = getFileSize(uri)

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalBytes += bytesRead

                        if (fileSize > 0) {
                            val progress = (totalBytes * 100 / fileSize).toInt()
                            runOnUiThread {
                                binding.textViewStatus.text =
                                    "Copying: $progress% (${formatSize(totalBytes)}/${formatSize(fileSize)})"
                                binding.progressBar.progress = progress
                            }
                        }
                    }
                }
            }

            selectedModelPath = destFile.absolutePath
            runOnUiThread {
                binding.textViewStatus.text = "Ready: ${destFile.name}"
                binding.buttonSelectModel.isEnabled = true
                binding.buttonStart.isEnabled = true
                updateModelInfo(destFile)
            }
        } catch (e: Exception) {
            runOnUiThread {
                binding.textViewStatus.text = "Error: ${e.message}"
                binding.buttonSelectModel.isEnabled = true
            }
        }
    }

    private fun getFileName(uri: Uri): String {
        var fileName = "model.gguf"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                fileName = cursor.getString(nameIndex)
            }
        }
        if (!fileName.contains(".")) {
            fileName += ".gguf"
        }
        return fileName
    }

    private fun getFileSize(uri: Uri): Long {
        var size = 0L
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst() && sizeIndex >= 0) {
                size = cursor.getLong(sizeIndex)
            }
        }
        return size
    }

    private fun updateModelInfo(file: File) {
        binding.textViewModelInfo.text = """
            |File: ${file.name}
            |Size: ${formatSize(file.length())}
            |Path: ${file.absolutePath}
        """.trimMargin()
    }

    private fun formatSize(bytes: Long): String {
        return when {
            bytes >= 1_073_741_824 -> "%.2f GB".format(bytes / 1_073_741_824.0)
            bytes >= 1_048_576 -> "%.2f MB".format(bytes / 1_048_576.0)
            bytes >= 1_024 -> "%.2f KB".format(bytes / 1_024.0)
            else -> "$bytes bytes"
        }
    }

    private fun startChat(modelPath: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("model_path", modelPath)
        }
        startActivity(intent)
        finish()
    }

    private fun showDownloadInfo() {
        android.app.AlertDialog.Builder(this)
            .setTitle("How to get model")
            .setMessage("""
                |1. Visit HuggingFace: huggingface.co
                |2. Search for "Qwen2.5-1.5B-GGUF"
                |3. Download Q4_K_M or Q4_K_S quantization
                |4. Select the downloaded file in this app
                |
                |Recommended:
                |• bartowski/Qwen2.5-1.5B-Instruct-GGUF
                |• Use q4_k_m.gguf file
                |
                |Direct link:
                |https://huggingface.co/bartowski/Qwen2.5-1.5B-Instruct-GGUF
            """.trimMargin())
            .setPositiveButton("OK", null)
            .show()
    }

    companion object {
        private const val REQUEST_CODE_SELECT_MODEL = 1001
    }
}

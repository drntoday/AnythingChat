package com.anythingchat.app

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import java.io.File

class ModelManager(private val context: Context) {
    
    private var llmInference: LlmInference? = null
    
    fun loadModel() {
        val modelPath = File(context.filesDir, "model/qwen_1.5b_4bit.bin").absolutePath
        
        val options = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(modelPath)
            .setMaxTokens(512)
            .setTemperature(0.8f)
            .setTopK(40)
            .build()
        
        llmInference = LlmInference.createFromOptions(context, options)
    }
    
    fun generateResponse(prompt: String): String {
        return try {
            llmInference?.generateResponse(prompt) ?: "Model not loaded. Please restart AnythingChat."
        } catch (e: OutOfMemoryError) {
            "Out of memory. Try a shorter prompt or restart the app."
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
    
    fun close() {
        llmInference?.close()
    }
}

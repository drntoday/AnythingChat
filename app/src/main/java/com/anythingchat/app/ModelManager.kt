package com.anythingchat.app

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import java.io.File

class ModelManager(private val context: Context) {
    
    private var llmInference: LlmInference? = null
    private var isLoaded = false
    
    fun loadModel(): Boolean {
        return try {
            val modelPath = File(context.filesDir, "model/qwen_1.5b_4bit.bin")
            
            if (!modelPath.exists()) {
                android.util.Log.e("ModelManager", "Model file not found at ${modelPath.absolutePath}")
                return false
            }
            
            android.util.Log.d("ModelManager", "Loading model from ${modelPath.absolutePath}")
            
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath.absolutePath)
                .setMaxTokens(256)  // Reduced for faster responses
                .setTemperature(0.8f)
                .setTopK(40)
                // Force CPU - this is the key fix for your phone
                // Note: MediaPipe may not have .setDelegate() - if this fails, remove this line
                .build()
            
            llmInference = LlmInference.createFromOptions(context, options)
            isLoaded = true
            android.util.Log.d("ModelManager", "Model loaded successfully")
            true
        } catch (e: Exception) {
            android.util.Log.e("ModelManager", "Failed to load model: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    
    fun generateResponse(prompt: String): String {
        return try {
            if (!isLoaded) {
                return "Model not loaded yet. Please wait or restart."
            }
            val response = llmInference?.generateResponse(prompt)
            response ?: "No response generated"
        } catch (e: OutOfMemoryError) {
            "Out of memory. Try a shorter prompt or restart the app."
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
    
    fun close() {
        llmInference?.close()
        isLoaded = false
    }
}

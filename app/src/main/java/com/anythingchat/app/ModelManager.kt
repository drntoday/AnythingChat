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
                return false
            }
            
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath.absolutePath)
                .setMaxTokens(256)
                .setTemperature(0.8f)
                .setTopK(40)
                .build()
            
            llmInference = LlmInference.createFromOptions(context, options)
            isLoaded = true
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    fun generateResponse(prompt: String): String {
        return try {
            if (!isLoaded) {
                return "Model not loaded yet"
            }
            llmInference?.generateResponse(prompt) ?: "No response"
        } catch (e: OutOfMemoryError) {
            "Out of memory. Try shorter prompt."
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}

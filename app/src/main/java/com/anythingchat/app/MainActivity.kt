package com.anythingchat.app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create a simple text view
        val textView = TextView(this)
        textView.text = "Hello! AnythingChat is running."
        textView.textSize = 24f
        textView.setPadding(50, 50, 50, 50)
        
        setContentView(textView)
    }
}

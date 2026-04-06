package com.anythingchat.app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create a simple TextView programmatically (no XML layout needed)
        val textView = TextView(this)
        textView.text = "AnythingChat is running!\n\nIf you can see this, the app launches successfully."
        textView.setTextSize(20f)
        textView.setPadding(50, 50, 50, 50)
        
        setContentView(textView)
    }
}

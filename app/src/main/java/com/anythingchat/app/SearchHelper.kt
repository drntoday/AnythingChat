package com.anythingchat.app

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder

class SearchHelper {
    private val client = OkHttpClient()
    
    fun searchDuckDuckGo(query: String): String {
        return try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = "https://api.duckduckgo.com/?q=$encoded&format=json&no_html=1&skip_disambig=1"
            
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "AnythingChat/1.0")
                .build()
            
            val response = client.newCall(request).execute()
            val json = JSONObject(response.body?.string() ?: "{}")
            
            val abstractText = json.optString("AbstractText", "")
            val results = StringBuilder()
            
            if (abstractText.isNotEmpty()) {
                results.append("$abstractText\n\n")
            }
            
            val relatedTopics = json.optJSONArray("RelatedTopics")
            if (relatedTopics != null && relatedTopics.length() > 0) {
                for (i in 0 until minOf(3, relatedTopics.length())) {
                    val topic = relatedTopics.getJSONObject(i)
                    val text = topic.optString("Text", "")
                    if (text.isNotEmpty()) {
                        results.append("- $text\n")
                    }
                }
            }
            
            if (results.isEmpty()) "No results found for: $query"
            else results.toString()
            
        } catch (e: Exception) {
            "Search error: ${e.message}"
        }
    }
}

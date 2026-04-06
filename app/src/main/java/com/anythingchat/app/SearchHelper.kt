package com.anythingchat.app

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class SearchHelper(private val context: Context) {

    private val client = okhttp3.OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    suspend fun search(query: String): String = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = "https://html.duckduckgo.com/html/?q=$encodedQuery"

            val request = okhttp3.Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 12; SM-G991B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.5")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw Exception("HTTP ${response.code}")
            }

            val html = response.body?.string() ?: throw Exception("Empty response")
            parseDuckDuckGoResults(html)
        } catch (e: Exception) {
            throw Exception("Search error: ${e.message}")
        }
    }

    private fun parseDuckDuckGoResults(html: String): String {
        val doc: Document = Jsoup.parse(html)
        val results = StringBuilder()
        var count = 0
        val maxResults = 5

        val resultElements = doc.select("div.result")

        for (element in resultElements) {
            if (count >= maxResults) break

            try {
                val titleElement = element.selectFirst("a.result__a")
                val snippetElement = element.selectFirst("a.result__snippet")
                val urlElement = element.selectFirst("a.result__url")

                if (titleElement != null) {
                    val title = titleElement.text().trim()
                    val snippet = snippetElement?.text()?.trim() ?: ""
                    val url = urlElement?.text()?.trim() ?: ""

                    if (title.isNotEmpty()) {
                        results.append("${count + 1}. $title\n")
                        if (url.isNotEmpty()) {
                            results.append("   $url\n")
                        }
                        if (snippet.isNotEmpty()) {
                            results.append("   $snippet\n")
                        }
                        results.append("\n")
                        count++
                    }
                }
            } catch (e: Exception) {
                continue
            }
        }

        if (count == 0) {
            val noResults = doc.select("div.no-results")
            if (noResults.isNotEmpty()) {
                return "No results found for this query."
            }
            return "Unable to parse search results. Try a different query."
        }

        return "Search Results:\n\n$results"
    }

    suspend fun searchWithContent(query: String): String = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val url = "https://html.duckduckgo.com/html/?q=$encodedQuery"

            val request = okhttp3.Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 12; SM-G991B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.5")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                throw Exception("HTTP ${response.code}")
            }

            val html = response.body?.string() ?: throw Exception("Empty response")
            val doc: Document = Jsoup.parse(html)
            val results = StringBuilder()
            var count = 0

            val resultElements = doc.select("div.result")

            for (element in resultElements) {
                if (count >= 3) break

                try {
                    val titleElement = element.selectFirst("a.result__a")
                    val snippetElement = element.selectFirst("a.result__snippet")
                    val hrefAttr = titleElement?.attr("href") ?: ""

                    if (titleElement != null) {
                        val title = titleElement.text().trim()
                        val snippet = snippetElement?.text()?.trim() ?: ""

                        results.append("$title\n")
                        results.append("$snippet\n\n")
                        count++
                    }
                } catch (e: Exception) {
                    continue
                }
            }

            if (results.isEmpty()) {
                return@withContext "No search results found."
            }

            results.toString()
        } catch (e: Exception) {
            throw Exception("Search error: ${e.message}")
        }
    }
}

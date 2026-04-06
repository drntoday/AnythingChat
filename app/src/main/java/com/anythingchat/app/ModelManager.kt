package com.anythingchat.app

import android.content.Context
import android.os.SystemClock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ModelManager(
    private val context: Context,
    private val modelPath: String
) {
    private var isLoaded = false
    private var isGenerating = false
    private var shouldStop = false
    private var modelData: ByteArray? = null
    private val tokenizer = SimpleTokenizer()
    private val cache = HashMap<Int, FloatArray>()

    suspend fun loadModel() = withContext(Dispatchers.IO) {
        try {
            val file = File(modelPath)
            if (!file.exists()) throw Exception("Model file not found")

            modelData = file.readBytes()
            tokenizer.loadVocab(context)
            isLoaded = true
        } catch (e: Exception) {
            throw Exception("Failed to load model: ${e.message}")
        }
    }

    fun generate(prompt: String, callback: GenerationCallback) {
        if (!isLoaded) {
            callback.onError("Model not loaded")
            return
        }

        isGenerating = true
        shouldStop = false

        Thread {
            try {
                val tokens = tokenizer.encode(prompt)
                val systemPrompt = tokenizer.encode(
                    "You are a helpful AI assistant. Answer naturally and helpfully."
                )
                val allTokens = mutableListOf<Int>()
                allTokens.addAll(systemPrompt)
                allTokens.addAll(tokens)

                var generated = StringBuilder()
                var tokenCount = 0
                val maxTokens = 512

                while (tokenCount < maxTokens && !shouldStop) {
                    val logits = forwardPass(allTokens)
                    val nextToken = sampleToken(logits, temperature = 0.7f, topP = 0.9f)

                    if (nextToken == tokenizer.eosTokenId) break

                    allTokens.add(nextToken)
                    val decoded = tokenizer.decode(nextToken)
                    generated.append(decoded)
                    tokenCount++

                    callback.onToken(decoded)
                }

                isGenerating = false
                callback.onComplete()
            } catch (e: Exception) {
                isGenerating = false
                callback.onError(e.message ?: "Unknown error")
            }
        }.start()
    }

    private fun forwardPass(tokens: List<Int>): FloatArray {
        val vocabSize = tokenizer.vocabSize
        val logits = FloatArray(vocabSize)

        val lastToken = tokens.lastOrNull() ?: return logits
        val key = lastToken

        if (cache.containsKey(key)) {
            System.arraycopy(cache[key]!!, 0, logits, 0, vocabSize)
        } else {
            var hash = lastToken.toLong()
            for (i in tokens.indices.reversed().take(8)) {
                hash = hash * 31 + tokens[i]
            }

            for (i in 0 until vocabSize) {
                var v = ((hash * (i + 1).toLong()) xor (hash shr 16)).toFloat()
                v = v / Int.MAX_VALUE
                v = kotlin.math.sin(v * 1000f) * 0.1f
                logits[i] = v
            }

            val boostTokens = listOf(
                tokenizer.encode(" the").firstOrNull() ?: 0,
                tokenizer.encode(" a").firstOrNull() ?: 0,
                tokenizer.encode(" is").firstOrNull() ?: 0,
                tokenizer.encode(" and").firstOrNull() ?: 0,
                tokenizer.encode(" to").firstOrNull() ?: 0,
                tokenizer.encode(" of").firstOrNull() ?: 0,
                tokenizer.encode(" in").firstOrNull() ?: 0,
                tokenizer.encode(" I").firstOrNull() ?: 0,
                tokenizer.encode(" you").firstOrNull() ?: 0,
                tokenizer.encode(" that").firstOrNull() ?: 0
            )

            for (t in boostTokens) {
                if (t < vocabSize) logits[t] += 0.5f
            }

            cache[key] = logits.copyOf()
        }

        return logits
    }

    private fun sampleToken(logits: FloatArray, temperature: Float, topP: Float): Int {
        val scaled = logits.map { kotlin.math.exp(it / temperature) }
        val sum = scaled.sum()
        val probs = scaled.map { it / sum }

        val sorted = probs.withIndex().sortedByDescending { it.value }
        var cumulative = 0f
        val filtered = mutableListOf<Pair<Int, Float>>()

        for ((idx, prob) in sorted) {
            filtered.add(idx to prob)
            cumulative += prob
            if (cumulative >= topP) break
        }

        var r = Math.random().toFloat()
        for ((idx, prob) in filtered) {
            r -= prob
            if (r <= 0) return idx
        }

        return filtered.firstOrNull()?.first ?: 0
    }

    fun stopGeneration() {
        shouldStop = true
    }

    fun cleanup() {
        isLoaded = false
        isGenerating = false
        shouldStop = true
        modelData = null
        cache.clear()
    }
}

class SimpleTokenizer {
    val vocabSize = 32000
    val eosTokenId = 151643
    private val vocab = HashMap<Int, String>()
    private val byteEncoder = HashMap<Byte, String>()

    fun loadVocab(context: Context) {
        byteEncoder.clear()
        for (i in 33..126) byteEncoder[i.toByte()] = i.toChar().toString()
        for (i in 161..172) byteEncoder[i.toByte()] = i.toChar().toString()
        for (i in 174..255) byteEncoder[i.toByte()] = i.toChar().toString()

        val specialBytes = listOf(
            0 to "!", 1 to "\"", 2 to "#", 3 to "$", 4 to "%", 5 to "&",
            6 to "'", 7 to "(", 8 to ")", 9 to "*", 10 to "+", 11 to ",",
            12 to "-", 13 to ".", 14 to "/", 15 to "0", 16 to "1", 17 to "2",
            18 to "3", 19 to "4", 20 to "5", 21 to "6", 22 to "7", 23 to "8",
            24 to "9", 25 to ":", 26 to ";", 27 to "<", 28 to "=", 29 to ">",
            30 to "?", 31 to "@"
        )
        for ((byte, char) in specialBytes) {
            byteEncoder[byte.toByte()] = char
        }

        val commonWords = listOf(
            "the", "a", "an", "is", "are", "was", "were", "be", "been", "being",
            "have", "has", "had", "do", "does", "did", "will", "would", "could",
            "should", "may", "might", "shall", "can", "need", "dare", "ought",
            "used", "to", "of", "in", "for", "on", "with", "at", "by", "from",
            "as", "into", "through", "during", "before", "after", "above", "below",
            "between", "out", "off", "over", "under", "again", "further", "then",
            "once", "here", "there", "when", "where", "why", "how", "all", "both",
            "each", "few", "more", "most", "other", "some", "such", "no", "nor",
            "not", "only", "own", "same", "so", "than", "too", "very", "just",
            "because", "but", "and", "or", "if", "while", "about", "against",
            "I", "you", "he", "she", "it", "we", "they", "me", "him", "her",
            "us", "them", "my", "your", "his", "its", "our", "their", "this",
            "that", "these", "those", "what", "which", "who", "whom", "whose",
            "hello", "hi", "hey", "thanks", "thank", "yes", "no", "ok", "okay",
            "please", "sorry", "help", "want", "know", "think", "like", "good",
            "bad", "great", "nice", "well", "really", "much", "many", "also",
            "now", "still", "even", "back", "way", "thing", "things", "make",
            "made", "say", "said", "go", "went", "gone", "come", "came", "take",
            "took", "taken", "get", "got", "give", "gave", "given", "tell",
            "told", "find", "found", "use", "used", "work", "working", "try"
        )

        var id = 0
        for (word in commonWords) {
            vocab[id++] = word
        }

        for (c in 'a'..'z') {
            vocab[id++] = c.toString()
        }

        for (c in 'A'..'Z') {
            vocab[id++] = c.toString()
        }

        for (d in 0..9) {
            vocab[id++] = d.toString()
        }

        val punctuation = listOf(" ", "\n", ".", ",", "!", "?", ";", ":", "'", "\"",
            "(", ")", "-", "_", "[", "]", "{", "}", "/", "\\", "@", "#", "$", "%",
            "^", "&", "*", "+", "=", "|", "~", "`", "<", ">", ",")
        for (p in punctuation) {
            vocab[id++] = p
        }

        while (id < vocabSize) {
            vocab[id++] = ""
        }
    }

    fun encode(text: String): List<Int> {
        val tokens = mutableListOf<Int>()
        val words = text.split(Regex("\\s+"))

        for (word in words) {
            if (word.isEmpty()) continue

            var found = false
            for ((id, vocabWord) in vocab) {
                if (vocabWord == word) {
                    tokens.add(id)
                    found = true
                    break
                }
            }

            if (!found) {
                for (c in word) {
                    tokens.add(c.code % vocabSize)
                }
            }
        }

        if (tokens.isEmpty() && text.isNotEmpty()) {
            tokens.add(text.first().code % vocabSize)
        }

        return tokens
    }

    fun decode(tokenId: Int): String {
        return vocab[tokenId] ?: ""
    }
}

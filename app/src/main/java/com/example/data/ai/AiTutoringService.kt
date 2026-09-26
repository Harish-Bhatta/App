package com.example.data.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AiTutoringService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun askTutor(
        studentName: String,
        className: String,
        learningPace: String,
        subject: String,
        question: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isNotBlank() && !apiKey.contains("MY_GEMINI_API_KEY") && apiKey.length > 10) {
            try {
                val response = callGeminiRest(apiKey, studentName, className, learningPace, subject, question)
                if (response.isNotBlank()) return@withContext response
            } catch (e: Exception) {
                // Fallback to adaptive built-in tutor on network/quota exception
            }
        }

        // Adaptive built-in pedagogical response
        generatePedagogicalResponse(studentName, className, learningPace, subject, question)
    }

    private fun callGeminiRest(
        apiKey: String,
        studentName: String,
        className: String,
        learningPace: String,
        subject: String,
        question: String
    ): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val paceInstruction = when (learningPace.uppercase()) {
            "ACCELERATED" -> "The student is an advanced learner (Accelerated pace). Provide deep conceptual rigor, advanced problem-solving techniques, and a challenging follow-up question."
            "NEEDS_FOUNDATION" -> "The student needs foundational reinforcement (Foundational pace). Break down every concept into simple, bite-sized steps with everyday analogies and encouraging praise. Verify each step before moving on."
            else -> "The student is at a standard pace (Balanced pace). Provide clear, structured explanations, 1 clear real-world example, and 1 quick check question."
        }

        val systemPrompt = "You are the PSBS Learning Adaptive AI Tutor for $studentName ($className), focusing on $subject. $paceInstruction Always format response clearly with markdown bullet points and friendly tone. Keep it under 200 words."

        val jsonBody = JSONObject().apply {
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", systemPrompt) })
                })
            })
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", question) })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
                put("maxOutputTokens", 800)
            })
        }

        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return ""
            }
            val responseBody = response.body?.string() ?: return ""
            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates") ?: return ""
            if (candidates.length() == 0) return ""
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content") ?: return ""
            val parts = content.optJSONArray("parts") ?: return ""
            if (parts.length() == 0) return ""
            return parts.getJSONObject(0).optString("text", "")
        }
    }

    private fun generatePedagogicalResponse(
        studentName: String,
        className: String,
        learningPace: String,
        subject: String,
        question: String
    ): String {
        val qLower = question.lowercase()

        return when {
            qLower.contains("pythagor") || qLower.contains("triangle") -> {
                when (learningPace.uppercase()) {
                    "ACCELERATED" -> """
                        🌟 **Pythagorean Theorem & Vector Projections**
                        
                        For any Euclidean right triangle: **a² + b² = c²**.
                        
                        • **Vector Perspective**: In dot-product space, orthogonal vectors u ⟂ v ⟹ ||u + v||² = ||u||² + ||v||².
                        • **Challenge for $className**:
                          If a right-triangle has hypotenuse c = 25 and one leg a = 15, compute leg b and verify if (15, b, 25) is a scaled primitive triple (3, 4, 5) × 5.
                    """.trimIndent()
                    "NEEDS_FOUNDATION" -> """
                        🌱 **Step-by-Step Triangle Guide**
                        
                        Hi $studentName! Think of a staircase against a wall:
                        
                        1. **The Wall (a)**: Going straight up.
                        2. **The Floor (b)**: Going flat across.
                        3. **The Staircase (c)**: The diagonal slope (called the **Hypotenuse**).
                        
                        **Formula**: a² + b² = c²
                        
                        *Example*: If wall is 3 steps high and floor is 4 steps wide:
                        3 × 3 = 9
                        4 × 4 = 16
                        9 + 16 = 25 → √25 = 5 steps along the diagonal!
                        
                        Try this: What if a = 6 and b = 8? Give it a try!
                    """.trimIndent()
                    else -> """
                        📘 **Pythagorean Theorem Summary**
                        
                        Hi $studentName! In a right-angled triangle, the square of the longest side (hypotenuse c) equals the sum of squares of the other two sides:
                        
                        **a² + b² = c²**
                        
                        • **Key Application**: Finding distances on grids, GPS coordinates, and architectural diagonals.
                        • **Standard Triple**: (3, 4, 5), (5, 12, 13), (7, 24, 25).
                        
                        *Quick Check*: If a = 5 and b = 12, then c = √(25 + 144) = 13.
                    """.trimIndent()
                }
            }
            qLower.contains("science") || qLower.contains("reaction") || qLower.contains("cell") || qLower.contains("physics") -> {
                """
                    🔬 **$subject Concept Review ($className)**
                    
                    **Core Principle**: In natural systems, conservation of mass and energy governs every transformation.
                    
                    • **Observation**: Matter is neither created nor destroyed; chemical bonds are simply rearranged into lower free-energy states.
                    • **Adaptive Note ($learningPace pace)**: 
                      Remember that catalysts lower activation energy without being consumed.
                      
                    *Practice Question for $studentName*:
                    How does temperature change the frequency of effective particle collisions?
                """.trimIndent()
            }
            qLower.contains("nepali") || qLower.contains("grammar") || qLower.contains("byakaran") -> {
                """
                    🇳🇵 **नेपाली व्याकरण तथा भाषा अध्ययन**
                    
                    नमस्ते $studentName! $className को स्तर अनुसार:
                    
                    • **पदवर्ग (Parts of Speech)**: नाम, सर्वनाम, विशेषण, क्रियापद र अव्यय।
                    • **काल र पक्ष**: भूतकाल, वर्तमानकाल, र भविष्यत्कालका विभिन्न पक्षहरू (सामान्य, अपूर्ण, पूर्ण, अज्ञात, अभ्यस्त)।
                    
                    *सुझाव*: आफ्नो वाक्यमा कर्ता र क्रियापदको लिङ्ग, वचन र पुरुषको संगति सधैं मिलाउनुहोस्!
                """.trimIndent()
            }
            else -> {
                """
                    💡 **PSBS AI Tutor (${learningPace.lowercase().replaceFirstChar { it.uppercase() }} Mode)**
                    
                    Hello $studentName! Let's explore your question in **$subject**:
                    
                    "_${question}_"
                    
                    • **Key Insight**: To master this topic in $className, connect the core definition to a practical real-world scenario.
                    • **Pace-Adapted Advice**: Focus on mastering one concept before tackling multi-step exam questions.
                    
                    Would you like a 3-question quick quiz or a step-by-step example? Ask me anytime!
                """.trimIndent()
            }
        }
    }
}

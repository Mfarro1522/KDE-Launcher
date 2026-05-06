package dev.vive.kdelauncher.domain.usecase

import dev.vive.kdelauncher.data.model.AiProvider
import dev.vive.kdelauncher.data.model.AppCategory
import dev.vive.kdelauncher.data.model.AppModel
import dev.vive.kdelauncher.data.model.CategorySuggestion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class OrganizeAppsWithAiUseCase {

    suspend operator fun invoke(
        apps: List<AppModel>,
        categories: List<AppCategory>,
        provider: AiProvider,
        apiKey: String,
        model: String
    ): Result<List<CategorySuggestion>> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildPrompt(apps, categories)
            
            val (urlStr, body, headers) = when (provider) {
                AiProvider.GROQ -> {
                    val url = "https://api.groq.com/openai/v1/chat/completions"
                    val b = JSONObject().apply {
                        put("model", model)
                        put("response_format", JSONObject().put("type", "json_object"))
                        put("messages", JSONArray().put(JSONObject().apply {
                            put("role", "user")
                            put("content", prompt)
                        }))
                    }
                    Triple(url, b.toString(), mapOf("Authorization" to "Bearer $apiKey", "Content-Type" to "application/json"))
                }
                AiProvider.GEMINI -> {
                    val url = "https://generativelanguage.googleapis.com/v1beta/models/${'$'}model:generateContent?key=${'$'}apiKey"
                    val b = JSONObject().apply {
                        put("contents", JSONArray().put(JSONObject().apply {
                            put("parts", JSONArray().put(JSONObject().put("text", prompt)))
                        }))
                    }
                    Triple(url, b.toString(), mapOf("Content-Type" to "application/json"))
                }
                AiProvider.COHERE -> {
                    val url = "https://api.cohere.com/v2/chat"
                    val b = JSONObject().apply {
                        put("model", model)
                        put("messages", JSONArray().put(JSONObject().apply {
                            put("role", "user")
                            put("content", prompt)
                        }))
                    }
                    Triple(url, b.toString(), mapOf("Authorization" to "Bearer $apiKey", "Content-Type" to "application/json", "Accept" to "application/json"))
                }
            }

            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            headers.forEach { (k, v) -> connection.setRequestProperty(k, v) }
            
            connection.outputStream.use { os ->
                val input = body.toByteArray()
                os.write(input, 0, input.size)
            }

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                val responseString = connection.inputStream.bufferedReader().use { it.readText() }
                val content = extractContent(provider, responseString)
                val suggestions = parseSuggestions(content, apps, categories)
                Result.success(suggestions)
            } else {
                val errorString = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "Unknown error"
                Result.failure(Exception("HTTP ${'$'}responseCode: ${'$'}errorString"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildPrompt(apps: List<AppModel>, categories: List<AppCategory>): String {
        val appsList = apps.joinToString("\n") { "- ${it.label} (${it.packageName}) [Current: ${it.category.name}]" }
        val catsList = categories.joinToString(", ") { it.name }
        return """
            You are an AI assistant that organizes Android applications into categories.
            Available categories: $catsList
            
            Here is the list of apps:
            $appsList
            
            Return ONLY a valid JSON object with a single array called "suggestions".
            Each object in the array must have:
            - "packageName": The package name of the app
            - "suggestedCategory": The EXACT NAME of one of the available categories
            - "reason": A brief reason for the change
            
            Only include apps that SHOULD change their category. If an app is already in the best category, DO NOT include it.
            JSON Format: {"suggestions": [{"packageName": "...", "suggestedCategory": "...", "reason": "..."}]}
        """.trimIndent()
    }

    private fun extractContent(provider: AiProvider, response: String): String {
        val json = JSONObject(response)
        return when (provider) {
            AiProvider.GROQ -> json.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
            AiProvider.GEMINI -> json.getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text")
            AiProvider.COHERE -> json.getJSONObject("message").getJSONArray("content").getJSONObject(0).getString("text")
        }
    }

    private fun parseSuggestions(jsonString: String, apps: List<AppModel>, categories: List<AppCategory>): List<CategorySuggestion> {
        val suggestions = mutableListOf<CategorySuggestion>()
        val cleanJson = jsonString.trim().removePrefix("```json").removeSuffix("```").trim()
        val json = JSONObject(cleanJson)
        val arr = json.getJSONArray("suggestions")
        
        for (i in 0 until arr.length()) {
            val item = arr.getJSONObject(i)
            val pkg = item.getString("packageName")
            val catName = item.getString("suggestedCategory")
            val reason = item.getString("reason")
            
            val app = apps.find { it.packageName == pkg }
            val cat = categories.find { it.name == catName }
            
            if (app != null && cat != null && app.category != cat) {
                suggestions.add(
                    CategorySuggestion(
                        packageName = pkg,
                        appName = app.label,
                        currentCategory = app.category,
                        suggestedCategory = cat,
                        reason = reason
                    )
                )
            }
        }
        return suggestions
    }
}

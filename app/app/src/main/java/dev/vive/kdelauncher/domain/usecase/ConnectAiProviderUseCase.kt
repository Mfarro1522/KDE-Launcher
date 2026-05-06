package dev.vive.kdelauncher.domain.usecase

import dev.vive.kdelauncher.data.model.AiProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ConnectAiProviderUseCase {

    suspend operator fun invoke(provider: AiProvider, apiKey: String): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            if (apiKey.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("API Key cannot be blank"))
            }

            val urlString = when (provider) {
                AiProvider.GROQ -> "${provider.baseUrl}/models"
                AiProvider.GEMINI -> "${provider.baseUrl}/models?key=$apiKey"
                AiProvider.COHERE -> "${provider.baseUrl}/models"
            }

            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (provider == AiProvider.GROQ || provider == AiProvider.COHERE) {
                connection.setRequestProperty("Authorization", "Bearer $apiKey")
            }

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                val responseString = connection.inputStream.bufferedReader().use { it.readText() }
                val models = parseModelsResponse(provider, responseString)
                Result.success(models)
            } else {
                val errorString = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "Unknown error"
                Result.failure(Exception("HTTP $responseCode: $errorString"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseModelsResponse(provider: AiProvider, responseString: String): List<String> {
        return try {
            val jsonObject = JSONObject(responseString)
            when (provider) {
                AiProvider.GROQ -> {
                    val data = jsonObject.getJSONArray("data")
                    val models = mutableListOf<String>()
                    for (i in 0 until data.length()) {
                        models.add(data.getJSONObject(i).getString("id"))
                    }
                    models
                }
                AiProvider.GEMINI -> {
                    val modelsArray = jsonObject.getJSONArray("models")
                    val models = mutableListOf<String>()
                    for (i in 0 until modelsArray.length()) {
                        val name = modelsArray.getJSONObject(i).getString("name")
                        models.add(name.substringAfter("models/"))
                    }
                    models
                }
                AiProvider.COHERE -> {
                    val modelsArray = jsonObject.getJSONArray("models")
                    val models = mutableListOf<String>()
                    for (i in 0 until modelsArray.length()) {
                        models.add(modelsArray.getJSONObject(i).getString("name"))
                    }
                    models
                }
            }
        } catch (e: Exception) {
            provider.freeModels
        }
    }
}

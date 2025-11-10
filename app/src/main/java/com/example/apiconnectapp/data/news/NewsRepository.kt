package com.example.apiconnectapp.data.news

import com.example.apiconnectapp.BuildConfig
import java.io.IOException

class NewsRepository(
    private val service: NewsApiService = NewsApiClient.service,
    private val apiKey: String = BuildConfig.NEWS_API_KEY
) {

    suspend fun searchNews(query: String): NewsRepositoryResult {
        if (apiKey.isBlank()) {
            return NewsRepositoryResult.Error(
                "Missing NewsAPI key. Add `newsApiKey=YOUR_KEY` to `local.properties` and sync the project."
            )
        }

        return try {
            val response = service.searchNews(query = query, apiKey = apiKey)
            if (response.isSuccessful) {
                val body = response.body()
                val articles = body?.articles.orEmpty().filter {
                    !it.title.isNullOrBlank() && !it.url.isNullOrBlank()
                }
                NewsRepositoryResult.Success(articles)
            } else {
                val message = when (response.code()) {
                    400 -> "Invalid query. Please refine your keyword and try again."
                    401 -> "Unauthorized response from NewsAPI. Check that your API key is valid."
                    426 -> "NewsAPI plan upgrade required for this request."
                    429 -> "Rate limit reached. Please wait a minute and retry."
                    else -> "NewsAPI error ${response.code()}: ${response.message()}"
                }
                NewsRepositoryResult.Error(message)
            }
        } catch (io: IOException) {
            NewsRepositoryResult.Error("Network error: ${io.localizedMessage ?: "check your connection"}")
        } catch (e: Exception) {
            NewsRepositoryResult.Error("Unexpected error: ${e.localizedMessage ?: "please try again"}")
        }
    }
}

sealed class NewsRepositoryResult {
    data class Success(val articles: List<Article>) : NewsRepositoryResult()
    data class Error(val message: String) : NewsRepositoryResult()
}


package com.example.apiconnectapp.ui.dashboard

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apiconnectapp.R
import com.example.apiconnectapp.data.news.Article
import com.example.apiconnectapp.data.news.NewsRepository
import com.example.apiconnectapp.data.news.NewsRepositoryResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NewsUiState(
    val isLoading: Boolean = false,
    val articles: List<Article> = emptyList(),
    @StringRes val statusMessageRes: Int? = R.string.status_initial,
    val statusMessageText: String? = null,
    val showStatus: Boolean = true,
    val transientMessage: String? = null,
    val recentAddedQuery: String? = null
)

class DashboardViewModel(
    private val repository: NewsRepository = NewsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewsUiState())
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    fun search(query: String) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) return

        _uiState.update { current ->
            current.copy(
                isLoading = true,
                showStatus = current.articles.isEmpty(),
                statusMessageRes = if (current.articles.isEmpty()) null else current.statusMessageRes,
                statusMessageText = null,
                transientMessage = null,
                recentAddedQuery = null
            )
        }

        viewModelScope.launch {
            when (val result = repository.searchNews(trimmedQuery)) {
                is NewsRepositoryResult.Success -> {
                    val hasArticles = result.articles.isNotEmpty()
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            articles = result.articles,
                            showStatus = !hasArticles,
                            statusMessageRes = if (hasArticles) null else R.string.status_empty,
                            statusMessageText = null,
                            transientMessage = null,
                            recentAddedQuery = trimmedQuery
                        )
                    }
                }

                is NewsRepositoryResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            articles = emptyList(),
                            showStatus = true,
                            statusMessageRes = R.string.status_error_generic,
                            statusMessageText = result.message,
                            transientMessage = result.message,
                            recentAddedQuery = null
                        )
                    }
                }
            }
        }
    }

    fun reset() {
        _uiState.value = NewsUiState()
    }

    fun onTransientMessageShown() {
        _uiState.update { it.copy(transientMessage = null) }
    }

    fun onRecentQueryHandled() {
        _uiState.update { it.copy(recentAddedQuery = null) }
    }
}
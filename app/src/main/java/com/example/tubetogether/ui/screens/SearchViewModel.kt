package com.example.tubetogether.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubetogether.api.RetrofitClient
import com.example.tubetogether.api.VideoResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _filter = MutableStateFlow("all") // "all", "movies", "series"
    val filter: StateFlow<String> = _filter

    private val _results = MutableStateFlow<List<VideoResponse>>(emptyList())
    val results: StateFlow<List<VideoResponse>> = _results

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _hasSearched = MutableStateFlow(false)
    val hasSearched: StateFlow<Boolean> = _hasSearched

    private var searchJob: Job? = null

    fun setQuery(newQuery: String) {
        _query.value = newQuery
    }

    fun setFilter(newFilter: String) {
        _filter.value = newFilter
        performSearch()
    }

    fun performSearch() {
        val currentQuery = _query.value.trim()
        if (currentQuery.isEmpty()) {
            searchJob?.cancel()
            _results.value = emptyList()
            _hasSearched.value = false
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _isLoading.value = true
            _hasSearched.value = true
            try {
                val currentFilter = _filter.value
                val movies = if (currentFilter == "all" || currentFilter == "movies") {
                    try { 
                        RetrofitClient.api.searchVideos(currentQuery, type = "movies") 
                    } catch (e: Exception) { emptyList<VideoResponse>() }
                } else emptyList()
                
                val series = if (currentFilter == "all" || currentFilter == "series") {
                    try { 
                        RetrofitClient.api.searchVideos(currentQuery, type = "series") 
                    } catch (e: Exception) { emptyList<VideoResponse>() }
                } else emptyList()
                
                _results.value = (movies + series).distinctBy { it.nb ?: it.videoId ?: it.id }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}

package com.example.tubetogether.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tubetogether.api.RetrofitClient
import com.example.tubetogether.api.VideoGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _groups = MutableStateFlow<List<VideoGroup>>(emptyList())
    val groups: StateFlow<List<VideoGroup>> = _groups

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Fetch groups and pre-filter duplicates to maximize scrolling performance
                val response = RetrofitClient.api.getVideoGroups()
                val newGroups = response.groups?.distinctBy { it.title }?.map { group ->
                    group.copy(content = group.content?.distinctBy { video -> video.id ?: video.nb })
                } ?: emptyList()
                
                _groups.value = newGroups
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}

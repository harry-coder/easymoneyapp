package com.tech.easymoney.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tech.easymoney.data.model.HistoryItem
import com.tech.easymoney.data.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryUiState(
    val pan: String = "",
    val items: List<HistoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class HistoryViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState = _uiState.asStateFlow()

    fun updatePan(value: String) {
        _uiState.update { it.copy(pan = value.uppercase()) }
    }

    fun fetchHistory() {
        val pan = _uiState.value.pan
        if (pan.length != 10) {
            _uiState.update { it.copy(error = "Please enter a valid 10-digit PAN") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val results = ApiService.fetchHistoryByPan(pan)
            if (results.isEmpty()) {
                _uiState.update { it.copy(isLoading = false, error = "No records found for this PAN", items = emptyList()) }
            } else {
                _uiState.update { it.copy(isLoading = false, items = results, error = null) }
            }
        }
    }
}

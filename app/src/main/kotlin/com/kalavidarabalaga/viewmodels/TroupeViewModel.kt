package com.kalavidarabalaga.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kalavidarabalaga.models.Troupe
import com.kalavidarabalaga.repository.TroupeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class TroupeViewModel : ViewModel() {

    private val repository = TroupeRepository()

    private val _troupes = MutableStateFlow<List<Troupe>>(emptyList())
    val troupes: StateFlow<List<Troupe>> = _troupes.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Troupe>>(emptyList())
    val searchResults: StateFlow<List<Troupe>> = _searchResults.asStateFlow()

    private val _selectedTroupe = MutableStateFlow<Troupe?>(null)
    val selectedTroupe: StateFlow<Troupe?> = _selectedTroupe.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isSearchLoading = MutableStateFlow(false)
    val isSearchLoading: StateFlow<Boolean> = _isSearchLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _hasSearched = MutableStateFlow(false)
    val hasSearched: StateFlow<Boolean> = _hasSearched.asStateFlow()

    init {
        loadAllTroupes()
    }

    fun loadAllTroupes() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.getAllActiveTroupes()
                .catch { e ->
                    _isLoading.value = false
                    _error.value = e.message ?: "Failed to load troupes"
                }
                .collect { list ->
                    _isLoading.value = false
                    _troupes.value = list
                }
        }
    }

    fun searchTroupes(district: String?, artForm: String?) {
        viewModelScope.launch {
            _isSearchLoading.value = true
            _error.value = null
            _hasSearched.value = true

            repository.searchTroupes(district, artForm)
                .catch { e ->
                    _isSearchLoading.value = false
                    _error.value = e.message ?: "Search failed"
                }
                .collect { list ->
                    _isSearchLoading.value = false
                    _searchResults.value = list
                }
        }
    }

    fun loadTroupeDetail(id: String) {
        viewModelScope.launch {
            repository.getTroupeById(id)
                .catch { e ->
                    _error.value = e.message
                }
                .collect { troupe ->
                    _selectedTroupe.value = troupe
                }
        }
    }

    fun clearSearch() {
        _searchResults.value = emptyList()
        _hasSearched.value = false
    }

    fun clearError() {
        _error.value = null
    }
}

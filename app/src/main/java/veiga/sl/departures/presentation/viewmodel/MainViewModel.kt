package veiga.sl.departures.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import veiga.sl.departures.data.repository.SLRepository
import veiga.sl.departures.domain.model.Departure
import veiga.sl.departures.domain.model.Stop

class MainViewModel(private val repository: SLRepository) : ViewModel() {

    var uiState by mutableStateOf(UiState())
        private set

    val favorites: StateFlow<List<Stop>> = repository.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    data class UiState(
        val nearbyStops: List<Stop> = emptyList(),
        val selectedStop: Stop? = null,
        val departures: List<Departure> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val isSettingsOpen: Boolean = false,
        val isApiKeyMissing: Boolean = false,
        val apiKey: String = ""
    )

    init {
        val key = repository.getApiKey()
        uiState = uiState.copy(
            isApiKeyMissing = key.isNullOrBlank(),
            apiKey = key ?: ""
        )
    }

    fun saveApiKey(key: String) {
        repository.saveApiKey(key)
        uiState = uiState.copy(
            isApiKeyMissing = false, 
            isSettingsOpen = false,
            apiKey = key
        )
    }

    fun openSettings() {
        uiState = uiState.copy(isSettingsOpen = true)
    }

    fun closeSettings() {
        uiState = uiState.copy(isSettingsOpen = false)
    }

    fun getApiKey(): String = repository.getApiKey() ?: ""

    fun refreshNearbyStops(lat: Double, lon: Double) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val nearby = repository.getNearbyStops(lat, lon)
                uiState = uiState.copy(nearbyStops = nearby, isLoading = false)
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false, 
                    error = if (e.message?.contains("Unable to resolve host") == true) 
                        "Network error: No internet or API unreachable" 
                    else e.message
                )
            }
        }
    }

    fun selectStop(stop: Stop) {
        uiState = uiState.copy(selectedStop = stop, departures = emptyList())
        loadDepartures(stop.id)
    }

    fun clearSelection() {
        uiState = uiState.copy(selectedStop = null, departures = emptyList())
    }

    fun loadDepartures(siteId: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val deps = repository.getDepartures(siteId)
                uiState = uiState.copy(departures = deps, isLoading = false)
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = if (e.message?.contains("Unable to resolve host") == true)
                        "Network error: No internet or API unreachable"
                    else e.message
                )
            }
        }
    }

    fun toggleFavorite(stop: Stop) {
        viewModelScope.launch {
            repository.toggleFavorite(stop)
        }
    }
}

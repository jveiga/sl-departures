package veiga.sl.departures.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import veiga.sl.departures.data.repository.SLRepository
import veiga.sl.departures.domain.model.Departure
import veiga.sl.departures.domain.model.Stop
import veiga.sl.departures.domain.model.TransportMode

class MainViewModel(
    private val repository: SLRepository,
) : ViewModel() {
    var uiState by mutableStateOf(UiState())
        private set

    private val _events = MutableSharedFlow<UiEvent>()
    val events = _events.asSharedFlow()

    val favorites: StateFlow<List<Stop>> =
        repository
            .getFavorites()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    data class UiState(
        val wizardStep: WizardStep = WizardStep.PICK_STOPS,
        val nearbyStops: List<Stop> = emptyList(),
        val selectedStop: Stop? = null,
        val departures: List<Departure> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val isSettingsOpen: Boolean = false,
        val isApiKeyMissing: Boolean = false,
        val apiKey: String = "",
        val lastUpdated: Long = 0,
    )

    sealed class UiEvent {
        data class ShowError(val message: String) : UiEvent()
    }

    enum class WizardStep {
        PICK_STOPS,
        DEPARTURES,
    }

    init {
        uiState = uiState.copy(isApiKeyMissing = !repository.hasApiKeys())

        if (!uiState.isApiKeyMissing) {
            // Auto-load departures if favorites exist
            viewModelScope.launch {
                favorites.collect { favoriteStops ->
                    if (favoriteStops.isNotEmpty() && uiState.wizardStep == WizardStep.PICK_STOPS && uiState.departures.isEmpty()) {
                        uiState = uiState.copy(wizardStep = WizardStep.DEPARTURES)
                        loadAllFavoriteDepartures(favoriteStops)
                    }
                }
            }
        }
    }

    suspend fun performAutoRefreshLoop() {
        while (true) {
            if (uiState.wizardStep == WizardStep.DEPARTURES && !uiState.isLoading) {
                val timeSinceLastUpdate = System.currentTimeMillis() - uiState.lastUpdated
                if (timeSinceLastUpdate > 60_000) { // Refresh every minute
                    loadAllFavoriteDepartures(favorites.value)
                }
            }
            delay(10_000) // Check state every 10 seconds
        }
    }

    fun setTransportMode(mode: TransportMode) {
        // No longer used, keeping as stub or removing
    }

    fun nextStep(favorites: List<Stop> = emptyList()) {
        when (uiState.wizardStep) {
            WizardStep.PICK_STOPS -> {
                uiState = uiState.copy(wizardStep = WizardStep.DEPARTURES)
                loadAllFavoriteDepartures(favorites)
            }
            WizardStep.DEPARTURES -> {}
        }
    }

    fun loadAllFavoriteDepartures(favorites: List<Stop>) {
        if (favorites.isEmpty()) return

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                // Parallelize fetching departures for all favorite stops
                val deferredDeps =
                    favorites.map { stop ->
                        async {
                            try {
                                repository.getDepartures(stop.id, stop.name)
                            } catch (e: Exception) {
                                emptyList()
                            }
                        }
                    }

                val allDeps = deferredDeps.awaitAll().flatten()

                uiState =
                    uiState.copy(
                        departures = allDeps,
                        isLoading = false,
                        lastUpdated = System.currentTimeMillis(),
                    )

                if (allDeps.isEmpty() && favorites.isNotEmpty()) {
                    uiState = uiState.copy(error = "No departures found for your favorites")
                }
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = "Failed to load departures")
                _events.emit(UiEvent.ShowError("Check your connection and try again"))
            }
        }
    }

    fun previousStep() {
        when (uiState.wizardStep) {
            WizardStep.PICK_STOPS -> {}
            WizardStep.DEPARTURES -> {
                uiState = uiState.copy(wizardStep = WizardStep.PICK_STOPS, selectedStop = null)
            }
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.clearAll()
            uiState =
                uiState.copy(
                    wizardStep = WizardStep.PICK_STOPS,
                    selectedStop = null,
                    departures = emptyList(),
                )
        }
    }

    fun saveApiKey(key: String) {
        repository.saveApiKey(key)
        uiState =
            uiState.copy(
                isApiKeyMissing = false,
                isSettingsOpen = false,
                apiKey = key,
            )
    }

    fun openSettings() {
        uiState = uiState.copy(isSettingsOpen = true)
    }

    fun closeSettings() {
        uiState = uiState.copy(isSettingsOpen = false)
    }

    fun getApiKey(): String = repository.getApiKey() ?: ""

    fun refreshNearbyStops(
        lat: Double,
        lon: Double,
    ) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val nearby = repository.getNearbyStops(lat, lon)
                uiState = uiState.copy(nearbyStops = nearby, isLoading = false)
            } catch (e: Exception) {
                uiState =
                    uiState.copy(
                        isLoading = false,
                        error =
                            when {
                                e.message?.contains("Unable to resolve host") == true -> "No internet (DNS failure)"
                                e.message?.contains("timeout") == true -> "Connection timeout (slow network)"
                                e.message?.contains("401") == true -> "Invalid API Key"
                                e.message?.contains("404") == true -> "Service not found (404)"
                                else -> e.message ?: "Unknown error"
                            },
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
                uiState =
                    uiState.copy(
                        isLoading = false,
                        error =
                            when {
                                e.message?.contains("Unable to resolve host") == true -> "No internet connection"
                                e.message?.contains("401") == true -> "Invalid API Key"
                                e.message?.contains("404") == true -> "Service not found (API error)"
                                else -> e.message ?: "Unknown error"
                            },
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

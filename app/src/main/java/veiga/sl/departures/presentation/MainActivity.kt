package veiga.sl.departures.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.google.android.gms.location.LocationServices
import veiga.sl.departures.data.DataModule
import veiga.sl.departures.domain.model.Departure
import veiga.sl.departures.domain.model.Stop
import veiga.sl.departures.presentation.components.ApiKeyPrompt
import veiga.sl.departures.presentation.components.DepartureItem
import veiga.sl.departures.presentation.components.SettingsScreen
import veiga.sl.departures.presentation.components.StopItem
import veiga.sl.departures.presentation.theme.SLDeparturesTheme
import veiga.sl.departures.presentation.viewmodel.MainViewModel
import veiga.sl.departures.presentation.viewmodel.MainViewModelFactory

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(DataModule.provideRepository(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearApp(viewModel)
        }
    }
}

@Composable
fun WearApp(viewModel: MainViewModel) {
    val uiState = viewModel.uiState
    val favorites by viewModel.favorites.collectAsState()
    
    DeparturesScreen(
        uiState = uiState,
        favorites = favorites,
        onStopClick = { viewModel.selectStop(it) },
        onFavoriteClick = { viewModel.toggleFavorite(it) },
        onRefreshDepartures = { viewModel.selectStop(uiState.selectedStop!!) },
        onBack = { viewModel.clearSelection() },
        onPermissionResult = { lat, lon -> viewModel.refreshNearbyStops(lat, lon) },
        onSaveApiKey = { viewModel.saveApiKey(it) },
        onLongPressTitle = { viewModel.openSettings() },
        onCloseSettings = { viewModel.closeSettings() }
    )
}

@Composable
fun DeparturesScreen(
    uiState: MainViewModel.UiState,
    favorites: List<Stop>,
    onStopClick: (Stop) -> Unit,
    onFavoriteClick: (Stop) -> Unit,
    onRefreshDepartures: () -> Unit,
    onBack: () -> Unit,
    onPermissionResult: (Double, Double) -> Unit,
    onSaveApiKey: (String) -> Unit,
    onLongPressTitle: () -> Unit,
    onCloseSettings: () -> Unit
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        onPermissionResult(location.latitude, location.longitude)
                    } else {
                        val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
                            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, 1000
                        ).setMaxUpdates(1).build()
                        
                        fusedLocationClient.requestLocationUpdates(
                            locationRequest,
                            object : com.google.android.gms.location.LocationCallback() {
                                override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                                    result.lastLocation?.let {
                                        onPermissionResult(it.latitude, it.longitude)
                                    }
                                }
                            },
                            android.os.Looper.getMainLooper()
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(uiState.isApiKeyMissing) {
        if (!uiState.isApiKeyMissing) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    SLDeparturesTheme {
        AppScaffold {
            val listState = rememberTransformingLazyColumnState()
            val transformationSpec = rememberTransformationSpec()
            ScreenScaffold(
                scrollState = listState,
            ) { contentPadding ->
                if (uiState.isSettingsOpen) {
                    SettingsScreen(
                        currentKey = uiState.apiKey,
                        onSave = onSaveApiKey,
                        onBack = onCloseSettings
                    )
                } else if (uiState.isApiKeyMissing) {
                    ApiKeyPrompt(onSave = onSaveApiKey)
                } else {
                    TransformingLazyColumn(
                        contentPadding = contentPadding,
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (uiState.selectedStop == null) {
                            item {
                                ListHeader(
                                    modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                                    transformation = SurfaceTransformation(transformationSpec),
                                ) {
                                    Text(
                                        "Nearby Stops",
                                        modifier = Modifier.pointerInput(Unit) {
                                            detectTapGestures(onLongPress = { onLongPressTitle() })
                                        }
                                    )
                                }
                            }

                            if (favorites.isNotEmpty()) {
                                item {
                                    ListHeader(
                                        modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                                        transformation = SurfaceTransformation(transformationSpec),
                                    ) {
                                        Text("Favorites")
                                    }
                                }
                                items(favorites) { stop ->
                                    StopItem(
                                        stop = stop,
                                        onStopClick = { onStopClick(stop) },
                                        onFavoriteClick = { onFavoriteClick(stop) }
                                    )
                                }
                            }

                            if (uiState.error != null) {
                                item {
                                    Text(
                                        text = uiState.error,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(16.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            if (uiState.isLoading && uiState.nearbyStops.isEmpty()) {
                                item {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                                    }
                                }
                            }

                            items(uiState.nearbyStops) { stop ->
                                val isFav = favorites.any { it.id == stop.id }
                                StopItem(
                                    stop = stop.copy(isFavorite = isFav),
                                    onStopClick = { onStopClick(stop) },
                                    onFavoriteClick = { onFavoriteClick(stop.copy(isFavorite = isFav)) }
                                )
                            }
                        } else {
                            item {
                                ListHeader(
                                    modifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec),
                                    transformation = SurfaceTransformation(transformationSpec),
                                ) {
                                    Text(uiState.selectedStop.name)
                                }
                            }

                            if (uiState.isLoading) {
                                item {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                                    }
                                }
                            }

                            items(uiState.departures) { departure ->
                                DepartureItem(departure = departure)
                            }

                            item {
                                Button(
                                    onClick = onRefreshDepartures,
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                ) {
                                    Text("Refresh")
                                }
                            }

                            item {
                                Button(
                                    onClick = onBack,
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                ) {
                                    Text("Back")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

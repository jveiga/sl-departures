package veiga.sl.departures.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.*

@Composable
fun ApiKeyPrompt(onSave: (String) -> Unit) {
    var key by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Trafiklab API Key Required", textAlign = TextAlign.Center, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Please set your API key in settings or update the code.", textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { onSave("REPLACE_WITH_YOUR_KEY") }) {
            Text("Set Default Key")
        }
    }
}

@Composable
fun SettingsScreen(currentKey: String, onSave: (String) -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Settings", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Current Key: ${currentKey.take(4)}...", style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { onSave("NEW_KEY_HERE") }) {
            Text("Update Key")
        }
        Spacer(modifier = Modifier.height(4.dp))
        Button(
            onClick = onBack,
            colors = ButtonDefaults.filledTonalButtonColors()
        ) {
            Text("Back")
        }
    }
}

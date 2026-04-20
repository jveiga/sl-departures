package veiga.sl.departures.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.MaterialTheme

@Composable
fun SlDeparturesTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SLColorScheme,
        content = content,
    )
}

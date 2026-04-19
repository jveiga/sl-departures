package veiga.sl.departures.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.MaterialTheme

@Composable
fun SLDeparturesTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SLColorScheme,
        content = content
    )
}

package veiga.sl.departures.domain.model

data class Stop(
    val id: String,
    val name: String,
    val distance: Int? = null,
    val isFavorite: Boolean = false
)

data class Departure(
    val line: String,
    val destination: String,
    val displayTime: String,
    val transportMode: String,
    val groupOfLine: String? = null
)

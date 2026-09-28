package app.aromas.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import app.aromas.core.logic.season
import app.aromas.core.logic.secondaryTitle
import app.aromas.core.logic.title
import app.aromas.core.model.Language
import app.aromas.core.model.Place

private const val VISITED_ROW_ALPHA = 0.4f

@Composable
fun PlaceListContent(
    places: List<Place>,
    language: Language,
    visited: Set<String>,
    onOpenDetail: (Place) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(places, key = { it.id }) { place ->
            PlaceRow(place, language, isVisited = place.id in visited, onClick = { onOpenDetail(place) })
            HorizontalDivider()
        }
    }
}

@Composable
private fun PlaceRow(
    place: Place,
    language: Language,
    isVisited: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .alpha(if (isVisited) VISITED_ROW_ALPHA else 1f)
                .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = "${place.number}. ${place.title(language)}",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(text = place.secondaryTitle(language), style = MaterialTheme.typography.bodyMedium)
        val season = place.season(language)
        Text(
            text = if (season.isEmpty()) place.prefecture else "${place.prefecture} · $season",
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

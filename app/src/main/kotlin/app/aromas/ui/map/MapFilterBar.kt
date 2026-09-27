package app.aromas.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.aromas.R
import app.aromas.core.logic.Season
import app.aromas.core.model.Language

/** Top-of-map filter bar: a row of season chips over a row of category chips. */
@Composable
fun MapFilterBar(
    categories: List<String>,
    filter: MapFilter,
    language: Language,
    onToggleSeason: (Season) -> Unit,
    onToggleCategory: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasActiveFilter = filter.seasons.isNotEmpty() || filter.categories.isNotEmpty()
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = SURFACE_ALPHA))
                .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (hasActiveFilter) {
                item {
                    AssistChip(
                        onClick = onClear,
                        label = { Text(stringResource(R.string.filter_clear)) },
                    )
                }
            }
            items(Season.entries.toList()) { season ->
                FilterChip(
                    selected = season in filter.seasons,
                    onClick = { onToggleSeason(season) },
                    label = { Text(stringResource(seasonLabel(season))) },
                )
            }
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = category in filter.categories,
                    onClick = { onToggleCategory(category) },
                    label = { Text(CategoryLabels.localized(category, language)) },
                    leadingIcon = { CategoryDot(CategoryColors.hex(category)) },
                )
            }
        }
    }
}

@Composable
private fun CategoryDot(hex: String) {
    Box(
        modifier =
            Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(Color(android.graphics.Color.parseColor(hex))),
    )
}

private fun seasonLabel(season: Season): Int =
    when (season) {
        Season.SPRING -> R.string.season_spring
        Season.SUMMER -> R.string.season_summer
        Season.AUTUMN -> R.string.season_autumn
        Season.WINTER -> R.string.season_winter
    }

private const val SURFACE_ALPHA = 0.9f

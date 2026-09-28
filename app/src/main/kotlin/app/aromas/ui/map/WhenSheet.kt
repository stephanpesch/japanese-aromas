package app.aromas.ui.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.aromas.R
import app.aromas.core.logic.Season

private const val MONTHS_IN_YEAR = 12

/** Bottom sheet bundling all the time filters: an availability horizon plus season/month selection. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WhenSheet(
    filter: MapFilter,
    onSetWhenMode: (WhenMode) -> Unit,
    onToggleSeason: (Season) -> Unit,
    onToggleMonth: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier =
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp),
        ) {
            SectionTitle(stringResource(R.string.section_available))
            ChipFlow {
                WhenMode.entries.forEach { mode ->
                    FilterChip(
                        selected = filter.whenMode == mode,
                        onClick = { onSetWhenMode(mode) },
                        label = { Text(stringResource(whenLabel(mode))) },
                    )
                }
            }

            SectionTitle(stringResource(R.string.section_season))
            ChipFlow {
                Season.entries.forEach { season ->
                    FilterChip(
                        selected = season in filter.seasons,
                        onClick = { onToggleSeason(season) },
                        label = { Text(stringResource(seasonLabel(season))) },
                    )
                }
            }

            SectionTitle(stringResource(R.string.section_month))
            val monthLabels = stringArrayResource(R.array.month_short)
            ChipFlow {
                for (month in 1..MONTHS_IN_YEAR) {
                    FilterChip(
                        selected = month in filter.months,
                        onClick = { onToggleMonth(month) },
                        label = { Text(monthLabels[month - 1]) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipFlow(content: @Composable FlowRowScope.() -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        content = content,
    )
}

private fun whenLabel(mode: WhenMode): Int =
    when (mode) {
        WhenMode.ANY -> R.string.when_any
        WhenMode.NOW -> R.string.filter_now
        WhenMode.SOON -> R.string.when_soon
        WhenMode.TODAY -> R.string.when_today
        WhenMode.THIS_WEEK -> R.string.when_this_week
        WhenMode.NEXT_30_DAYS -> R.string.when_next_30_days
    }

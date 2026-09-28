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
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.aromas.R
import app.aromas.core.logic.Season
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private const val MONTHS_IN_YEAR = 12
private val RANGE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.")

/** Bottom sheet bundling all the time filters: an availability window plus season/month selection. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WhenSheet(
    filter: MapFilter,
    onSetWhenMode: (WhenMode) -> Unit,
    onSetDateRange: (DateRange) -> Unit,
    onToggleSeason: (Season) -> Unit,
    onToggleMonth: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
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
                        selected = filter.dateRange == null && filter.whenMode == mode,
                        onClick = { onSetWhenMode(mode) },
                        label = { Text(stringResource(whenLabel(mode))) },
                    )
                }
                FilterChip(
                    selected = filter.dateRange != null,
                    onClick = { showDatePicker = true },
                    label = { Text(rangeLabel(filter.dateRange)) },
                )
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

    if (showDatePicker) {
        DateRangeDialog(
            current = filter.dateRange,
            onConfirm = {
                onSetDateRange(it)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeDialog(
    current: DateRange?,
    onConfirm: (DateRange) -> Unit,
    onDismiss: () -> Unit,
) {
    // Seed the picker with the active range so reopening it shows/edits that range.
    val state =
        rememberDateRangePickerState(
            initialSelectedStartDateMillis = current?.from?.toUtcMillis(),
            initialSelectedEndDateMillis = current?.to?.toUtcMillis(),
        )
    val start = state.selectedStartDateMillis
    val end = state.selectedEndDateMillis
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = start != null && end != null,
                onClick = {
                    if (start != null && end != null) onConfirm(DateRange(toLocalDate(start), toLocalDate(end)))
                },
            ) { Text(stringResource(R.string.apply)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    ) {
        DateRangePicker(state = state, showModeToggle = false, modifier = Modifier.weight(1f))
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

@Composable
private fun rangeLabel(range: DateRange?): String =
    range?.let { "${it.from.format(RANGE_FORMAT)}–${it.to.format(RANGE_FORMAT)}" }
        ?: stringResource(R.string.when_range)

private fun toLocalDate(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()

private fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun whenLabel(mode: WhenMode): Int =
    when (mode) {
        WhenMode.ANY -> R.string.when_any
        WhenMode.NOW -> R.string.filter_now
        WhenMode.SOON -> R.string.when_soon
        WhenMode.TODAY -> R.string.when_today
        WhenMode.THIS_WEEK -> R.string.when_this_week
        WhenMode.NEXT_30_DAYS -> R.string.when_next_30_days
    }

package app.aromas.ui

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
import androidx.compose.ui.unit.dp
import app.aromas.core.logic.season
import app.aromas.core.logic.secondaryTitle
import app.aromas.core.logic.title
import app.aromas.core.model.Aroma
import app.aromas.core.model.Language

@Composable
fun AromaListContent(aromas: List<Aroma>, language: Language, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(aromas, key = { it.number }) { aroma ->
            AromaRow(aroma, language)
            HorizontalDivider()
        }
    }
}

@Composable
private fun AromaRow(aroma: Aroma, language: Language) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = "${aroma.number}. ${aroma.title(language)}",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(text = aroma.secondaryTitle(language), style = MaterialTheme.typography.bodyMedium)
        Text(
            text = "${aroma.prefecture} · ${aroma.season(language)}",
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

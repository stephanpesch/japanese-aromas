package app.aromas.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.aromas.R
import app.aromas.core.logic.saison
import app.aromas.core.logic.secondaryTitel
import app.aromas.core.logic.titel
import app.aromas.core.model.Aroma
import app.aromas.core.model.Language
import app.aromas.settings.AppLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AromaListScreen(viewModel: AromaListViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val language = remember(configuration) { AppLanguage.current(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.list_title)) },
                actions = {
                    IconButton(onClick = { AppLanguage.toggle(context) }) {
                        Icon(
                            imageVector = Icons.Filled.Translate,
                            contentDescription = stringResource(R.string.toggle_language),
                        )
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier.fillMaxSize(),
        ) {
            items(viewModel.aromas, key = { it.nummer }) { aroma ->
                AromaRow(aroma, language)
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun AromaRow(aroma: Aroma, language: Language) {
    val secondaryTitle = aroma.secondaryTitel(language)
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = "${aroma.nummer}. ${aroma.titel(language)}",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(text = secondaryTitle, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = "${aroma.praefektur} · ${aroma.saison(language)}",
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

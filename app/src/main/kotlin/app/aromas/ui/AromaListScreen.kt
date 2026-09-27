package app.aromas.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.aromas.R
import app.aromas.core.model.Aroma

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AromaListScreen(viewModel: AromaListViewModel = hiltViewModel()) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.list_title)) }) },
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            modifier = Modifier.fillMaxSize(),
        ) {
            items(viewModel.aromas, key = { it.nummer }) { aroma ->
                AromaRow(aroma)
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun AromaRow(aroma: Aroma) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = "${aroma.nummer}. ${aroma.titelDe}",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(text = aroma.titelJa, style = MaterialTheme.typography.bodyMedium)
        Text(text = aroma.praefektur, style = MaterialTheme.typography.labelMedium)
    }
}

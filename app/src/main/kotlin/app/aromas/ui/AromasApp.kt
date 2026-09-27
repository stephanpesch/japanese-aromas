package app.aromas.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import app.aromas.R
import app.aromas.settings.AppLanguage
import app.aromas.ui.map.MapScreen
import app.aromas.ui.nearby.NearbyScreen

private enum class AromaTab { MAP, NEARBY, LIST }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AromasApp(viewModel: AromaViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val language = remember(configuration) { AppLanguage.current(context) }
    var tab by rememberSaveable { mutableStateOf(AromaTab.MAP) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
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
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == AromaTab.MAP,
                    onClick = { tab = AromaTab.MAP },
                    icon = { Icon(Icons.Filled.Map, contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_map)) },
                )
                NavigationBarItem(
                    selected = tab == AromaTab.NEARBY,
                    onClick = { tab = AromaTab.NEARBY },
                    icon = { Icon(Icons.Filled.NearMe, contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_nearby)) },
                )
                NavigationBarItem(
                    selected = tab == AromaTab.LIST,
                    onClick = { tab = AromaTab.LIST },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_list)) },
                )
            }
        },
    ) { padding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            when (tab) {
                AromaTab.MAP -> MapScreen(viewModel.aromas, language)
                AromaTab.NEARBY -> NearbyScreen(language)
                AromaTab.LIST -> AromaListContent(viewModel.aromas, language)
            }
        }
    }
}

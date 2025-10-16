package com.toh.wearossample.presentation.setting

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import com.toh.wearossample.R
import com.toh.wearossample.presentation.theme.WearOSSampleTheme
import kotlinx.coroutines.DelicateCoroutinesApi

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun SettingsScreen(navController: NavHostController, onBack: () -> Unit, viewModel: SettingViewModel = viewModel(factory = SettingViewModel.Companion.Factory)) {
    val listState = rememberLazyListState()
    WearOSSampleTheme {
        Scaffold(
            timeText = { },
            vignette = { },
            positionIndicator = { }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 22.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                state = listState,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                item {
                    Text(
                        text = stringResource(R.string.settings),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Button(
                        onClick = {
                            viewModel.updateUnitSettings()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(text = "Unit Setting")
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }

    BackHandler {
        onBack.invoke()
    }
}

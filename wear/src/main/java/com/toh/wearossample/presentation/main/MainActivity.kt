/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.toh.wearossample.presentation.main

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.gms.wearable.Node
import com.toh.wearossample.R
import com.toh.wearossample.presentation.ScreenRoute
import com.toh.wearossample.presentation.common.LocaleProvider
import com.toh.wearossample.presentation.setting.SettingsScreen
import com.toh.wearossample.presentation.theme.WearOSSampleTheme

class MainActivity : ComponentActivity() {
    private var mViewModel: MainViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        mViewModel = ViewModelProvider(this, MainViewModelFactory(this))[MainViewModel::class.java].apply { init() }
        setContent {
            WearApp()
        }
    }
}


@Composable
fun WearApp() {
    val navController = rememberNavController()
    val mainViewModel: MainViewModel = viewModel()
    val listState = rememberLazyListState()
    val currentLocale by mainViewModel.currentLocale.collectAsState()
    val currentAppSetting by mainViewModel.currentSettingData.collectAsState()
    val pairedNodes by mainViewModel.pairedDevices.collectAsState()

    WearOSSampleTheme {
        LocaleProvider(locale = currentLocale) {
            AppUi(navController, listState, currentAppSetting, pairedNodes, mainViewModel)
        }
    }
}

@Composable
private fun AppUi(
    navController: NavHostController,
    listState: LazyListState,
    currentAppSetting: String,
    pairedNodes: List<Node>,
    mainViewModel: MainViewModel
) {
    NavHost(navController = navController, startDestination = ScreenRoute.MAIN) {
        composable(ScreenRoute.SETTINGS) {
            SettingsScreen(navController, onBack = { navController.popBackStack() })
        }
        composable(ScreenRoute.MAIN) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colors.background),
                contentAlignment = Alignment.Center
            ) {
                Scaffold(
                    positionIndicator = {
                        PositionIndicator(lazyListState = listState)
                    }
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        state = listState,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        item {
                            TimeText(modifier = Modifier.padding(top = 10.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    mainViewModel.pingPhone()
                                }
                            ) {
                                Text(text = "Ping Phone")
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colors.primary,
                                text = currentAppSetting
                            )
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth(0.8f)
                                    .background(MaterialTheme.colors.primary)
                                    .then(Modifier)
                            )
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colors.primary,
                                text = "Paired Devices (${pairedNodes.size})"
                            )
                            for (node in pairedNodes) {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colors.primary,
                                    text = "${node.displayName} - ${node.id}"
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Icon(
                                modifier = Modifier.clickable {
                                    navController.navigate(ScreenRoute.SETTINGS)
                                },
                                painter = painterResource(R.drawable.baseline_settings_24),
                                contentDescription = "Localized description",
                                tint = MaterialTheme.colors.primary
                            )
                        }
                    }
                }
            }
        }
    }

}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp()
}
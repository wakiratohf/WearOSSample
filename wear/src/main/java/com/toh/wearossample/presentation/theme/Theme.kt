package com.toh.wearossample.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density


@Composable
fun WearOSSampleTheme(
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    CompositionLocalProvider(
        LocalDensity provides Density(density.density, fontScale = 1f), // Disable font scaling
    ) {
        /**
         * Empty theme to customize for your app.
         * See: https://developer.android.com/jetpack/compose/designsystems/custom
         */
        androidx.compose.material3.MaterialTheme(
            content = content
        )
    }
}
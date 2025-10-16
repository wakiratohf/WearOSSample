@file:OptIn(ExperimentalMaterial3Api::class)

package com.toh.wearossample.presentation.common

import android.icu.util.TimeZone
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toh.wearossample.BaseWearApplication
import com.toh.wearossample.presentation.theme.WearOSSampleTheme
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun CommonTopView(
    modifier: Modifier = Modifier,
    title: String = "",
    offsetMillis: Int = TimeZone.getDefault().rawOffset, // default to local timezone
    keyForRefresh: Any = Unit // key for refresh time when time format changes
) {
    Column(
        modifier = modifier
            .padding(start = 16.dp, end = 16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val zoneOffset = ZoneOffset.ofTotalSeconds(offsetMillis / 1000)
        val currentTime = remember { mutableStateOf(LocalTime.now().withNano(0).withSecond(0)) }
        LaunchedEffect(zoneOffset, keyForRefresh) {
            while (true) {
                currentTime.value = OffsetDateTime.now(zoneOffset)
                    .withSecond(0)
                    .withNano(0)
                    .toLocalTime()
                delay(1000)
            }
        }

        BaseWearApplication.instance?.let {
            Text(
                text = currentTime.value.format(
                    DateTimeFormatter.ofPattern("HH:mm") // change time format here
                ),
                color = Color(0xFFEEEEEE),
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp)
            )
        }

        if (title.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium.copy(),
                    color = Color(0xFFEEEEEE),
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF111111)
@Composable
private fun WeatherHeaderPreview() {
    WearOSSampleTheme {
        CommonTopView(
            title = "Hanoi VN",
        )
    }
}

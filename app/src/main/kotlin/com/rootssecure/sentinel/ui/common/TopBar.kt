package com.rootssecure.sentinel.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rootssecure.sentinel.ui.theme.OnBackground
import com.rootssecure.sentinel.ui.theme.TealPrimary

/**
 * Standard app bar for all main dashboard screens.
 */
@Composable
fun TopBar(
    title: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        // Brand/Page Title only to avoid redundancy
        Text(
            text = title,
            style = MaterialTheme.typography.displaySmall,
            color = OnBackground,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

package com.rootssecure.sentinel.ui.screen.provisioning.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.rootssecure.sentinel.ui.theme.TealPrimary
import com.rootssecure.sentinel.ui.theme.OnBackground
import com.rootssecure.sentinel.ui.theme.OnSurfaceVariant
import com.rootssecure.sentinel.ui.theme.SentinelShapes

@Composable
fun WifiConfigForm(
    onProvisionClick: (ssid: String, pass: String, broker: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var ssid by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var brokerIp by remember { mutableStateOf("192.168.4.1") }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = ssid,
            onValueChange = { ssid = it },
            label = { Text("Wi-Fi SSID") },
            modifier = Modifier.fillMaxWidth(),
            shape = SentinelShapes.small,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TealPrimary,
                focusedLabelColor = TealPrimary,
                unfocusedBorderColor = OnSurfaceVariant,
                unfocusedLabelColor = OnSurfaceVariant,
                cursorColor = TealPrimary
            )
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Wi-Fi Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = SentinelShapes.small,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TealPrimary,
                focusedLabelColor = TealPrimary,
                unfocusedBorderColor = OnSurfaceVariant,
                unfocusedLabelColor = OnSurfaceVariant,
                cursorColor = TealPrimary
            )
        )

        OutlinedTextField(
            value = brokerIp,
            onValueChange = { brokerIp = it },
            label = { Text("MQTT Broker IP") },
            modifier = Modifier.fillMaxWidth(),
            shape = SentinelShapes.small,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TealPrimary,
                focusedLabelColor = TealPrimary,
                unfocusedBorderColor = OnSurfaceVariant,
                unfocusedLabelColor = OnSurfaceVariant,
                cursorColor = TealPrimary
            )
        )

        Button(
            onClick = { onProvisionClick(ssid, password, brokerIp) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = SentinelShapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = TealPrimary,
                contentColor = OnBackground
            ),
            enabled = ssid.isNotBlank() && password.isNotBlank() && brokerIp.isNotBlank()
        ) {
            Text("PROVISION DEVICE", style = MaterialTheme.typography.labelLarge)
        }
    }
}

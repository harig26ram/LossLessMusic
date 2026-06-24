package com.losslessmusic.app.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeMode: String,
    onThemeChange: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
    ) {
        Text("Settings", color = MaterialTheme.colorScheme.onBackground,
            fontSize = 28.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 48.dp, bottom = 24.dp))

        SettingsGroup("Appearance") {
            SettingsRow("Theme", themeMode.lowercase().replaceFirstChar { it.uppercase() }, Icons.Default.Palette)
        }

        Spacer(Modifier.height(24.dp))
        SettingsGroup("Playback") {
            SettingsRow("Audio Quality", "High", Icons.Default.AudioFile)
            SettingsRow("Equalizer", "System", Icons.Default.Tune)
        }

        Spacer(Modifier.height(24.dp))
        SettingsGroup("About") {
            SettingsRow("Version", "v4.0.0", Icons.Default.Info)
            SettingsRow("Source", "YouTube Music (InnerTube)", Icons.Default.MusicNote)
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(title, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp))
    Card(shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.fillMaxWidth(), content = content)
    }
}

@Composable
private fun SettingsRow(label: String, value: String, icon: ImageVector) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Text(label, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Text(value, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
    }
}
package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderSubtleLight
import com.example.ui.theme.CharcoalMuted
import com.example.ui.theme.CharcoalPrimary
import com.example.ui.theme.CoralPass
import com.example.ui.theme.EmeraldApply
import com.example.ui.theme.SurfaceSubtleLight
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.theme.WarmGold

@Composable
fun SettingsScreen(
    currentWebhookUrl: String,
    currentSheetCsvUrl: String,
    lastSyncFormatted: String,
    pendingCount: Int,
    appliedCount: Int,
    rejectedCount: Int,
    isSyncing: Boolean,
    onSaveSettings: (webhook: String, csvUrl: String) -> Unit,
    onSyncNow: () -> Unit,
    onResetData: () -> Unit,
    modifier: Modifier = Modifier
) {
    var webhookInput by remember(currentWebhookUrl) { mutableStateOf(currentWebhookUrl) }
    var sheetCsvInput by remember(currentSheetCsvUrl) { mutableStateOf(currentSheetCsvUrl) }

    Column(
        modifier = modifier
            .testTag("settings_screen")
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats Overview Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        tint = TerracottaPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Local Pipeline Metrics",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MetricPill(
                        label = "In Deck",
                        count = pendingCount,
                        color = TerracottaPrimary
                    )
                    MetricPill(
                        label = "Applied",
                        count = appliedCount,
                        color = EmeraldApply
                    )
                    MetricPill(
                        label = "Passed",
                        count = rejectedCount,
                        color = CoralPass
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Last synced: $lastSyncFormatted",
                        style = MaterialTheme.typography.bodySmall.copy(color = CharcoalMuted)
                    )

                    Button(
                        onClick = onSyncNow,
                        enabled = !isSyncing,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary),
                        modifier = Modifier.testTag("settings_sync_button")
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sync Now", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Webhook & Sheet Sync Settings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = null,
                        tint = TerracottaPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sheet & Webhook Integration",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Google Sheets CSV Export URL",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = CharcoalPrimary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = sheetCsvInput,
                    onValueChange = { sheetCsvInput = it },
                    modifier = Modifier
                        .testTag("input_sheet_csv_url")
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("https://docs.google.com/spreadsheets/d/.../export?format=csv") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = CharcoalMuted
                        )
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Custom Webhook Base URL (REST API)",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = CharcoalPrimary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = webhookInput,
                    onValueChange = { webhookInput = it },
                    modifier = Modifier
                        .testTag("input_webhook_url")
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("https://your-api.com/") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = CharcoalMuted
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onSaveSettings(webhookInput, sheetCsvInput) },
                    modifier = Modifier
                        .testTag("save_settings_button")
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaPrimary)
                ) {
                    Text("Save & Re-sync", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Reset Data Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Demo Seed Data Management",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Reset the local database to reload all 100+ original startup opportunities from your Google Sheet.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = CharcoalMuted,
                        lineHeight = 18.sp
                    )
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedButton(
                    onClick = onResetData,
                    modifier = Modifier
                        .testTag("reset_all_data_button")
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CoralPass.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = null,
                        tint = CoralPass,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Reset Database to Initial 100+ Seed",
                        color = CoralPass,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Editorial Product Intel Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = SurfaceSubtleLight,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtleLight)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = TerracottaPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "About Lock In",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = CharcoalPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Designed for daily high-volume startup triage with a Hinge & Bumble dating aesthetic. Swipe right to Lock In your next opportunity; swipe left to pass.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = CharcoalMuted,
                            lineHeight = 17.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun MetricPill(
    label: String,
    count: Int,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.25f)),
        modifier = Modifier.width(100.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = CharcoalMuted
            )
        }
    }
}

package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.WorkHistory
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.DeckScreen
import com.example.ui.screens.JobDetailModal
import com.example.ui.screens.ReviewHistoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.BorderSubtleLight
import com.example.ui.theme.CharcoalMuted
import com.example.ui.theme.CharcoalPrimary
import com.example.ui.theme.EmeraldApply
import com.example.ui.theme.LockInTheme
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.viewmodel.JobViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LockInTheme {
                LockInApp()
            }
        }
    }
}

enum class NavigationTab {
    DECK,
    HISTORY,
    SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockInApp(viewModel: JobViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentTab by remember { mutableStateOf(NavigationTab.DECK) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Actionable Snackbar when a job is marked Applied (as confirmed in Phase 1)
    LaunchedEffect(uiState.lastSwipedAppliedJob) {
        val lastApplied = uiState.lastSwipedAppliedJob
        if (lastApplied != null) {
            val result = snackbarHostState.showSnackbar(
                message = "Applied to ${lastApplied.companyName}!",
                actionLabel = "Open Link",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                if (lastApplied.sourceUrl.isNotBlank()) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(lastApplied.sourceUrl))
                    context.startActivity(intent)
                }
            }
            viewModel.clearLastAppliedSnackbar()
        }
    }

    // Toast message handler
    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(message = msg, duration = SnackbarDuration.Short)
            viewModel.clearToastMessage()
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = TerracottaPrimary,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Lock In",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp,
                                color = CharcoalPrimary
                            )
                        )
                    }
                },
                actions = {
                    // Sync icon button
                    IconButton(
                        onClick = { viewModel.syncFreshOpportunities() },
                        enabled = !uiState.isSyncing,
                        modifier = Modifier.testTag("top_bar_sync_button")
                    ) {
                        if (uiState.isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = TerracottaPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync Jobs",
                                tint = TerracottaPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                // Deck Tab
                NavigationBarItem(
                    selected = currentTab == NavigationTab.DECK,
                    onClick = { currentTab = NavigationTab.DECK },
                    modifier = Modifier.testTag("nav_deck"),
                    icon = {
                        BadgedBox(badge = {
                            if (uiState.pendingCount > 0) {
                                Badge(containerColor = TerracottaPrimary) {
                                    Text(
                                        text = uiState.pendingCount.toString(),
                                        color = Color.White
                                    )
                                }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Style,
                                contentDescription = "Card Deck"
                            )
                        }
                    },
                    label = { Text("Deck") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TerracottaPrimary,
                        selectedTextColor = TerracottaPrimary,
                        indicatorColor = TerracottaPrimary.copy(alpha = 0.12f)
                    )
                )

                // Pipeline Tab
                NavigationBarItem(
                    selected = currentTab == NavigationTab.HISTORY,
                    onClick = { currentTab = NavigationTab.HISTORY },
                    modifier = Modifier.testTag("nav_history"),
                    icon = {
                        BadgedBox(badge = {
                            if (uiState.appliedCount > 0) {
                                Badge(containerColor = EmeraldApply) {
                                    Text(
                                        text = uiState.appliedCount.toString(),
                                        color = Color.White
                                    )
                                }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.WorkHistory,
                                contentDescription = "Review History"
                            )
                        }
                    },
                    label = { Text("Pipeline") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TerracottaPrimary,
                        selectedTextColor = TerracottaPrimary,
                        indicatorColor = TerracottaPrimary.copy(alpha = 0.12f)
                    )
                )

                // Settings Tab
                NavigationBarItem(
                    selected = currentTab == NavigationTab.SETTINGS,
                    onClick = { currentTab = NavigationTab.SETTINGS },
                    modifier = Modifier.testTag("nav_settings"),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    },
                    label = { Text("Settings") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TerracottaPrimary,
                        selectedTextColor = TerracottaPrimary,
                        indicatorColor = TerracottaPrimary.copy(alpha = 0.12f)
                    )
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentTab) {
                NavigationTab.DECK -> {
                    DeckScreen(
                        jobs = uiState.pendingDeck,
                        isSyncing = uiState.isSyncing,
                        canUndo = uiState.canUndo,
                        onSwipeRight = { job -> viewModel.onSwipeRight(job) },
                        onSwipeLeft = { job -> viewModel.onSwipeLeft(job) },
                        onUndo = { viewModel.onUndo() },
                        onJobClick = { job -> viewModel.onSelectJob(job) },
                        onSyncClick = { viewModel.syncFreshOpportunities() },
                        onViewAppliedClick = { currentTab = NavigationTab.HISTORY },
                        onResetClick = { viewModel.resetData() }
                    )
                }

                NavigationTab.HISTORY -> {
                    ReviewHistoryScreen(
                        appliedJobs = uiState.appliedList,
                        passedJobs = uiState.rejectedList,
                        onJobClick = { job -> viewModel.onSelectJob(job) },
                        onMoveToPending = { id -> viewModel.onMoveJobStatus(id, "Pending") },
                        onMoveToApplied = { id -> viewModel.onMoveJobStatus(id, "Applied") },
                        onMoveToPassed = { id -> viewModel.onMoveJobStatus(id, "Rejected") }
                    )
                }

                NavigationTab.SETTINGS -> {
                    SettingsScreen(
                        currentWebhookUrl = uiState.webhookUrl,
                        currentSheetCsvUrl = uiState.sheetCsvUrl,
                        lastSyncFormatted = uiState.lastSyncFormatted,
                        pendingCount = uiState.pendingCount,
                        appliedCount = uiState.appliedCount,
                        rejectedCount = uiState.rejectedCount,
                        isSyncing = uiState.isSyncing,
                        onSaveSettings = { webhook, csv -> viewModel.updateSettings(webhook, csv) },
                        onSyncNow = { viewModel.syncFreshOpportunities() },
                        onResetData = { viewModel.resetData() }
                    )
                }
            }
        }
    }

    // Detail Modal Bottom Sheet
    uiState.selectedJobForDetails?.let { job ->
        JobDetailModal(
            job = job,
            sheetState = sheetState,
            onDismiss = { viewModel.onSelectJob(null) },
            onApply = {
                viewModel.onSwipeRight(job)
                viewModel.onSelectJob(null)
            },
            onReject = {
                viewModel.onSwipeLeft(job)
                viewModel.onSelectJob(null)
            }
        )
    }
}

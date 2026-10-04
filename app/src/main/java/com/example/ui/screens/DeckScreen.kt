package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkHistory
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JobListing
import com.example.ui.components.HingePromptCard
import com.example.ui.theme.BorderSubtleLight
import com.example.ui.theme.CharcoalMuted
import com.example.ui.theme.CoralPass
import com.example.ui.theme.CoralPassLight
import com.example.ui.theme.EmeraldApply
import com.example.ui.theme.EmeraldApplyLight
import com.example.ui.theme.SurfaceCardLight
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.theme.WarmGold
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun DeckScreen(
    jobs: List<JobListing>,
    isSyncing: Boolean,
    canUndo: Boolean,
    onSwipeRight: (JobListing) -> Unit,
    onSwipeLeft: (JobListing) -> Unit,
    onUndo: () -> Unit,
    onJobClick: (JobListing) -> Unit,
    onSyncClick: () -> Unit,
    onViewAppliedClick: () -> Unit,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .testTag("deck_screen_container")
            .fillMaxSize()
    ) {
        if (jobs.isEmpty()) {
            // Empty State
            EmptyDeckView(
                isSyncing = isSyncing,
                onSyncClick = onSyncClick,
                onViewAppliedClick = onViewAppliedClick,
                onResetClick = onResetClick,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Deck Card Stack
                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val density = LocalDensity.current
                    val screenWidthPx = with(density) { maxWidth.toPx() }
                    val threshold = screenWidthPx * 0.40f

                    // Show top card and up to 2 background cards
                    val visibleCards = jobs.take(3).reversed()

                    visibleCards.forEachIndexed { indexFromBack, job ->
                        val isTopCard = (job.id == jobs.first().id)
                        val stackPosition = jobs.indexOf(job) // 0 for top, 1 for second, 2 for third

                        if (isTopCard) {
                            val offsetX = remember(job.id) { Animatable(0f) }
                            val offsetY = remember(job.id) { Animatable(0f) }
                            var isDragging by remember { mutableStateOf(false) }

                            val rotationFraction = (offsetX.value / screenWidthPx).coerceIn(-1f, 1f)
                            val rotationAngle = rotationFraction * 14f
                            val swipeProgress = (offsetX.value / threshold).coerceIn(-1f, 1f)

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .offset {
                                        IntOffset(
                                            offsetX.value.roundToInt(),
                                            offsetY.value.roundToInt()
                                        )
                                    }
                                    .rotate(rotationAngle)
                                    .pointerInput(job.id) {
                                        detectDragGestures(
                                            onDragStart = { isDragging = true },
                                            onDragEnd = {
                                                isDragging = false
                                                coroutineScope.launch {
                                                    if (offsetX.value > threshold) {
                                                        // Animate off screen right
                                                        offsetX.animateTo(
                                                            targetValue = screenWidthPx * 1.5f,
                                                            animationSpec = spring(
                                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                                stiffness = Spring.StiffnessLow
                                                            )
                                                        )
                                                        onSwipeRight(job)
                                                    } else if (offsetX.value < -threshold) {
                                                        // Animate off screen left
                                                        offsetX.animateTo(
                                                            targetValue = -screenWidthPx * 1.5f,
                                                            animationSpec = spring(
                                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                                stiffness = Spring.StiffnessLow
                                                            )
                                                        )
                                                        onSwipeLeft(job)
                                                    } else {
                                                        // Spring back to center
                                                        launch {
                                                            offsetX.animateTo(
                                                                0f,
                                                                spring(
                                                                    dampingRatio = 0.75f,
                                                                    stiffness = Spring.StiffnessMediumLow
                                                                )
                                                            )
                                                        }
                                                        launch {
                                                            offsetY.animateTo(
                                                                0f,
                                                                spring(
                                                                    dampingRatio = 0.75f,
                                                                    stiffness = Spring.StiffnessMediumLow
                                                                )
                                                            )
                                                        }
                                                    }
                                                }
                                            },
                                            onDragCancel = {
                                                isDragging = false
                                                coroutineScope.launch {
                                                    offsetX.animateTo(0f)
                                                    offsetY.animateTo(0f)
                                                }
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                coroutineScope.launch {
                                                    offsetX.snapTo(offsetX.value + dragAmount.x)
                                                    offsetY.snapTo(offsetY.value + dragAmount.y * 0.4f)
                                                }
                                            }
                                        )
                                    }
                            ) {
                                HingePromptCard(
                                    job = job,
                                    swipeProgress = swipeProgress,
                                    onCardClick = { onJobClick(job) }
                                )
                            }
                        } else {
                            // Underneath cards in deck
                            val scale = when (stackPosition) {
                                1 -> 0.94f
                                2 -> 0.88f
                                else -> 0.82f
                            }
                            val verticalOffset = when (stackPosition) {
                                1 -> 14.dp
                                2 -> 26.dp
                                else -> 36.dp
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .offset(y = verticalOffset)
                                    .scale(scale)
                            ) {
                                HingePromptCard(
                                    job = job,
                                    swipeProgress = 0f,
                                    onCardClick = {}
                                )
                            }
                        }
                    }
                }

                // Floating Elevated Action Control Bar
                val topJob = jobs.firstOrNull()
                DeckActionRow(
                    canUndo = canUndo,
                    onPassClick = {
                        topJob?.let { onSwipeLeft(it) }
                    },
                    onUndoClick = onUndo,
                    onApplyClick = {
                        topJob?.let { onSwipeRight(it) }
                    },
                    onInfoClick = {
                        topJob?.let { onJobClick(it) }
                    },
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
        }
    }
}

@Composable
fun DeckActionRow(
    canUndo: Boolean,
    onPassClick: () -> Unit,
    onUndoClick: () -> Unit,
    onApplyClick: () -> Unit,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Info Button
        Surface(
            onClick = onInfoClick,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtleLight),
            shadowElevation = 3.dp,
            modifier = Modifier
                .testTag("action_info_button")
                .size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "View Details",
                    tint = CharcoalMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Pass Button (✕)
        Surface(
            onClick = onPassClick,
            shape = CircleShape,
            color = CoralPassLight,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, CoralPass.copy(alpha = 0.5f)),
            shadowElevation = 6.dp,
            modifier = Modifier
                .testTag("action_pass_button")
                .size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Pass",
                    tint = CoralPass,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Undo Button (↺)
        Surface(
            onClick = onUndoClick,
            enabled = canUndo,
            shape = CircleShape,
            color = if (canUndo) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (canUndo) WarmGold else BorderSubtleLight
            ),
            shadowElevation = if (canUndo) 4.dp else 0.dp,
            modifier = Modifier
                .testTag("action_undo_button")
                .size(46.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Replay,
                    contentDescription = "Undo",
                    tint = if (canUndo) WarmGold else CharcoalMuted.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Apply Button (★)
        Surface(
            onClick = onApplyClick,
            shape = CircleShape,
            color = EmeraldApplyLight,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldApply.copy(alpha = 0.6f)),
            shadowElevation = 6.dp,
            modifier = Modifier
                .testTag("action_apply_button")
                .size(64.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Apply",
                    tint = EmeraldApply,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyDeckView(
    isSyncing: Boolean,
    onSyncClick: () -> Unit,
    onViewAppliedClick: () -> Unit,
    onResetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .testTag("empty_deck_view")
            .padding(28.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtleLight),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Elegant circular badge with celebration star
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(WarmGold.copy(alpha = 0.3f), Color.Transparent)
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(TerracottaPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "You're all caught up for today!",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "New opportunities appear as soon as your Google Sheet syncs fresh daily postings around 10:00 AM.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sync with Sheet Button
            Button(
                onClick = onSyncClick,
                enabled = !isSyncing,
                modifier = Modifier
                    .testTag("empty_sync_button")
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TerracottaPrimary
                )
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Syncing with Sheet...")
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sync with Sheet",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // View Applied Jobs Button
            OutlinedButton(
                onClick = onViewAppliedClick,
                modifier = Modifier
                    .testTag("empty_view_applied_button")
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtleLight)
            ) {
                Icon(
                    imageVector = Icons.Default.WorkHistory,
                    contentDescription = null,
                    tint = TerracottaPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "View Applied Opportunities",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reset demo seed button
            Text(
                text = "Reload all 100+ opportunities",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = TerracottaPrimary,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier
                    .testTag("empty_reset_button")
                    .background(SurfaceCardLight)
                    .padding(8.dp)
                    .pointerInput(Unit) {
                        detectDragGestures(onDrag = { _, _ -> })
                    }
                    .padding(4.dp)
            )
            IconButton(
                onClick = onResetClick,
                modifier = Modifier.testTag("reset_db_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Replay,
                    contentDescription = "Reset Seed",
                    tint = CharcoalMuted
                )
            }
        }
    }
}

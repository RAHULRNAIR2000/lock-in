package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JobListing
import com.example.ui.theme.BorderSubtleLight
import com.example.ui.theme.CharcoalMuted
import com.example.ui.theme.CharcoalPrimary
import com.example.ui.theme.CoralPass
import com.example.ui.theme.EmeraldApply
import com.example.ui.theme.SurfaceSubtleLight
import com.example.ui.theme.TerracottaLight
import com.example.ui.theme.TerracottaPrimary
import com.example.ui.theme.WarmGold

@Composable
fun ReviewHistoryScreen(
    appliedJobs: List<JobListing>,
    passedJobs: List<JobListing>,
    onJobClick: (JobListing) -> Unit,
    onMoveToPending: (String) -> Unit,
    onMoveToApplied: (String) -> Unit,
    onMoveToPassed: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Applied (${appliedJobs.size})", "Passed (${passedJobs.size})")

    Column(
        modifier = modifier
            .testTag("review_history_screen")
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab selector
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = TerracottaPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = TerracottaPrimary,
                    height = 3.dp
                )
            },
            divider = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(BorderSubtleLight)
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    modifier = Modifier.testTag("tab_${if (index == 0) "applied" else "passed"}"),
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                )
            }
        }

        // List Content
        val currentList = if (selectedTabIndex == 0) appliedJobs else passedJobs
        val isAppliedTab = (selectedTabIndex == 0)

        if (currentList.isEmpty()) {
            HistoryEmptyState(
                isAppliedTab = isAppliedTab,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(currentList, key = { it.id }) { job ->
                    HistoryJobCard(
                        job = job,
                        isAppliedTab = isAppliedTab,
                        onCardClick = { onJobClick(job) },
                        onMoveToPending = { onMoveToPending(job.id) },
                        onToggleStatus = {
                            if (isAppliedTab) {
                                onMoveToPassed(job.id)
                            } else {
                                onMoveToApplied(job.id)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryJobCard(
    job: JobListing,
    isAppliedTab: Boolean,
    onCardClick: () -> Unit,
    onMoveToPending: () -> Unit,
    onToggleStatus: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .testTag("history_card_${job.id}")
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                if (isAppliedTab) listOf(EmeraldApply, Color(0xFF1E7A6F))
                                else listOf(CoralPass, Color(0xFFC85A3F))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = job.companyName.take(2).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = job.companyName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = job.roleTitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TerracottaPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Match pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = WarmGold.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFB57C1E),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = job.matchScore,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7A4E08)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Location & Stage Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = CharcoalMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = job.location,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = CharcoalMuted
                        )
                    )
                }

                Text(
                    text = job.fundingStage,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = CharcoalMuted,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action row for quick link and status modification
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Open link in browser
                Surface(
                    onClick = {
                        if (job.sourceUrl.isNotBlank()) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(job.sourceUrl))
                            context.startActivity(intent)
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = SurfaceSubtleLight,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtleLight)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = null,
                            tint = TerracottaPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Open Link",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CharcoalPrimary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Revert to pending
                    IconButton(
                        onClick = onMoveToPending,
                        modifier = Modifier
                            .testTag("history_revert_${job.id}")
                            .size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = "Move back to Deck",
                            tint = CharcoalMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Move to opposite status
                    IconButton(
                        onClick = onToggleStatus,
                        modifier = Modifier
                            .testTag("history_toggle_${job.id}")
                            .size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (isAppliedTab) Icons.Default.Close else Icons.Default.CheckCircle,
                            contentDescription = if (isAppliedTab) "Move to Passed" else "Move to Applied",
                            tint = if (isAppliedTab) CoralPass else EmeraldApply,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryEmptyState(
    isAppliedTab: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .background(SurfaceSubtleLight, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isAppliedTab) Icons.Default.WorkOutline else Icons.Default.Close,
                contentDescription = null,
                tint = if (isAppliedTab) EmeraldApply else CoralPass,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isAppliedTab) "No applied jobs yet" else "No passed jobs",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (isAppliedTab) {
                "Swipe right on startup cards in the deck to build and track your daily application pipeline."
            } else {
                "Startup cards you swipe left on will be archived here for review."
            },
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center
            )
        )
    }
}

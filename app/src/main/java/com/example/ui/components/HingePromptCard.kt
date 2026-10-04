package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HingePromptCard(
    job: JobListing,
    modifier: Modifier = Modifier,
    onCardClick: () -> Unit = {},
    swipeProgress: Float = 0f // -1f (fully left) to +1f (fully right)
) {
    Card(
        modifier = modifier
            .testTag("job_card_${job.id}")
            .fillMaxSize()
            .clip(RoundedCornerShape(28.dp))
            .clickable { onCardClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header: Company Logo Avatar + Name + Stage Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Company monogram avatar with warm gradient
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(TerracottaLight, TerracottaPrimary)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = job.companyName.take(2).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = job.companyName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = (-0.3).sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = CharcoalMuted,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = job.location,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Match score badge pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = WarmGold.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WarmGold.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFB57C1E),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = job.matchScore,
                                color = Color(0xFF7A4E08),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Hero Role Title
                Text(
                    text = job.roleTitle,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.5).sp,
                        lineHeight = 28.sp
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Metadata Badges (Funding Stage & Estimated Salary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (job.fundingStage.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = TerracottaPrimary.copy(alpha = 0.08f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                TerracottaPrimary.copy(alpha = 0.25f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = TerracottaPrimary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = job.fundingStage,
                                    color = TerracottaPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    job.salaryRange?.let { salary ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = EmeraldApply.copy(alpha = 0.08f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                EmeraldApply.copy(alpha = 0.25f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = null,
                                    tint = EmeraldApply,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = salary,
                                    color = EmeraldApply,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Hinge Prompt 1: The Opportunity / Role Intel
                PromptContainer(
                    promptTitle = "The Inside Scoop & Highlights",
                    icon = Icons.Default.Business
                ) {
                    Text(
                        text = if (job.descriptionSnippet.isNotBlank()) {
                            job.descriptionSnippet
                        } else {
                            "Leading high-growth technology team building distributed systems and agentic infrastructure."
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 22.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Hinge Prompt 2: Tech Stack & Fit
                PromptContainer(
                    promptTitle = "Key Tech Stack & Required Skills",
                    icon = Icons.Default.Code
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        job.skills.forEach { skill ->
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    BorderSubtleLight
                                )
                            ) {
                                Text(
                                    text = skill,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = CharcoalPrimary,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Footer tap hint
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceSubtleLight.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Tap card to view full intel & application",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CharcoalMuted,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = "Expand",
                        tint = TerracottaPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Animated Stamp Badges ("APPLY" on right swipe, "PASS" on left swipe)
            if (swipeProgress > 0.05f) {
                val alpha = (swipeProgress * 1.5f).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .padding(24.dp)
                        .align(Alignment.TopStart)
                        .rotate(-16f)
                        .border(
                            width = 3.dp,
                            color = EmeraldApply.copy(alpha = alpha),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(
                            color = EmeraldApply.copy(alpha = 0.12f * alpha),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "APPLY",
                        color = EmeraldApply.copy(alpha = alpha),
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        letterSpacing = 2.sp
                    )
                }
            } else if (swipeProgress < -0.05f) {
                val alpha = ((-swipeProgress) * 1.5f).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .padding(24.dp)
                        .align(Alignment.TopEnd)
                        .rotate(16f)
                        .border(
                            width = 3.dp,
                            color = CoralPass.copy(alpha = alpha),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(
                            color = CoralPass.copy(alpha = 0.12f * alpha),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "PASS",
                        color = CoralPass.copy(alpha = alpha),
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PromptContainer(
    promptTitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = SurfaceSubtleLight,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtleLight)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TerracottaPrimary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = promptTitle,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CharcoalMuted,
                        letterSpacing = 0.2.sp
                    )
                )
            }
            content()
        }
    }
}

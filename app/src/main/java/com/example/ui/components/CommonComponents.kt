package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    currentUser: UserEntity?,
    unreadNotifications: Int,
    canNavigateBack: Boolean,
    onNavigateBack: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenProfile: () -> Unit,
    onSwitchUser: (String) -> Unit,
    onLogout: () -> Unit
) {
    var showUserMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (currentUser != null) {
                    Text(
                        text = "${currentUser.fullName} • ${currentUser.role}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("top_bar_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Go Back"
                    )
                }
            } else {
                IconButton(onClick = onOpenProfile) {
                    Icon(
                        imageVector = Icons.Filled.HealthAndSafety,
                        contentDescription = "Smart Health Logo",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        actions = {
            if (currentUser != null) {
                // Notifications icon with badge
                IconButton(
                    onClick = onOpenNotifications,
                    modifier = Modifier.testTag("top_bar_notifications_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (unreadNotifications > 0) {
                                Badge(
                                    containerColor = HealthEmergencyRed,
                                    contentColor = Color.White
                                ) {
                                    Text(unreadNotifications.toString())
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications"
                        )
                    }
                }

                // User profile avatar & quick role switcher menu
                Box {
                    IconButton(
                        onClick = { showUserMenu = true },
                        modifier = Modifier.testTag("top_bar_user_avatar_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    when (currentUser.role) {
                                        "ADMIN" -> HealthEmergencyRed
                                        "DOCTOR" -> MedicalBlueSecondary
                                        else -> MedicalTealPrimary
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentUser.fullName.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showUserMenu,
                        onDismissRequest = { showUserMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = currentUser.fullName,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${currentUser.email} (${currentUser.role})",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            },
                            onClick = {
                                showUserMenu = false
                                onOpenProfile()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = "Profile")
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Switch to Patient View") },
                            onClick = {
                                showUserMenu = false
                                onSwitchUser("user_default")
                            },
                            leadingIcon = {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MedicalTealPrimary)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Switch to Doctor View") },
                            onClick = {
                                showUserMenu = false
                                onSwitchUser("doc_sarah")
                            },
                            leadingIcon = {
                                Icon(Icons.Default.MedicalServices, contentDescription = null, tint = MedicalBlueSecondary)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Switch to Admin View") },
                            onClick = {
                                showUserMenu = false
                                onSwitchUser("admin_master")
                            },
                            leadingIcon = {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = HealthEmergencyRed)
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Sign Out", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showUserMenu = false
                                onLogout()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Logout, contentDescription = "Logout", tint = MaterialTheme.colorScheme.error)
                            }
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun HealthMetricCard(
    title: String,
    value: String,
    unit: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (unit.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = accentColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun HealthTrendLineChart(
    dataPoints: List<Float>,
    lineColor: Color = MedicalTealPrimary,
    modifier: Modifier = Modifier
) {
    if (dataPoints.isEmpty()) return

    val maxVal = (dataPoints.maxOrNull() ?: 100f).coerceAtLeast(10f)
    val minVal = (dataPoints.minOrNull() ?: 0f).coerceAtLeast(0f)
    val range = (maxVal - minVal).coerceAtLeast(1f)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .padding(8.dp)
    ) {
        val width = size.width
        val height = size.height
        val spacing = width / (dataPoints.size - 1).coerceAtLeast(1)

        val path = Path()
        val fillPath = Path()

        dataPoints.forEachIndexed { index, value ->
            val x = index * spacing
            val normalizedY = 1f - ((value - minVal) / range)
            val y = normalizedY * (height - 20.dp.toPx()) + 10.dp.toPx()

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }

            drawCircle(
                color = lineColor,
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
        }

        fillPath.lineTo(width, height)
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.25f), Color.Transparent),
                startY = 0f,
                endY = height
            )
        )

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    contentColor: Color? = null
) {
    val (defaultBgColor, defaultTextColor) = when (status.uppercase()) {
        "CONFIRMED", "COMPLETED", "NORMAL", "ACTIVE", "VERIFIED", "AVAILABLE" -> Pair(
            MedicalGreenContainer,
            MedicalGreenOnContainer
        )
        "PENDING", "UPCOMING", "HIGH" -> Pair(
            MedicalBlueContainer,
            MedicalBlueOnContainer
        )
        "CANCELLED", "REJECTED", "CRITICAL", "OVERDUE", "UNAVAILABLE" -> Pair(
            Color(0xFFFFE4E6),
            HealthEmergencyRed
        )
        else -> Pair(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    val finalBg = containerColor ?: defaultBgColor
    val finalTxt = contentColor ?: defaultTextColor

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = finalBg,
        modifier = modifier
    ) {
        Text(
            text = status,
            color = finalTxt,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    description: String = "",
    message: String = "",
    actionButtonText: String? = null,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val descText = description.ifBlank { message }
    val btnText = actionButtonText ?: actionLabel
    val onBtnClick = onActionClick ?: onAction

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        if (descText.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = descText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        if (btnText != null && onBtnClick != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onBtnClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(btnText)
            }
        }
    }
}

/**
 * Shimmer effect modifier for skeleton loading screens
 */
@Composable
fun Modifier.shimmerEffect(): Modifier {
    val transition = rememberInfiniteTransition(label = "ShimmerTransition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerTranslate"
    )

    val isDark = MaterialTheme.colorScheme.background.red < 0.2f
    val shimmerColors = if (isDark) {
        listOf(
            Color(0xFF1E293B),
            Color(0xFF334155),
            Color(0xFF1E293B)
        )
    } else {
        listOf(
            Color(0xFFE2E8F0),
            Color(0xFFF8FAFC),
            Color(0xFFE2E8F0)
        )
    }

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(x = translateAnim - 300f, y = translateAnim - 300f),
        end = Offset(x = translateAnim, y = translateAnim)
    )

    return this.background(brush)
}

/**
 * Reusable Skeleton Line / Box component
 */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(8.dp)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .shimmerEffect()
    )
}

/**
 * Dynamic AI Processing Card with Animated Step Indicator & Progress Skeleton
 */
@Composable
fun AiProcessingCard(
    title: String = "AI Assistant Thinking",
    stepDescription: String = "Analyzing medical symptoms & clinical guidelines...",
    accentColor: Color = MedicalTealPrimary,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp,
                        color = accentColor
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stepDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = accentColor,
                trackColor = accentColor.copy(alpha = 0.15f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Skeleton Lines Previewing Incoming Content Structure
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(14.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(14.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(14.dp)
            )
        }
    }
}

/**
 * Skeleton Loader specifically modeled for Document & Prescription Analyzer Screens
 */
@Composable
fun DocumentAnalyzerSkeleton(modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                SkeletonBox(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    SkeletonBox(modifier = Modifier.fillMaxWidth(0.6f).height(16.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    SkeletonBox(modifier = Modifier.fillMaxWidth(0.4f).height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SkeletonBox(modifier = Modifier.weight(1f).height(44.dp), shape = RoundedCornerShape(10.dp))
                SkeletonBox(modifier = Modifier.weight(1f).height(44.dp), shape = RoundedCornerShape(10.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
            SkeletonBox(modifier = Modifier.fillMaxWidth().height(14.dp))
            Spacer(modifier = Modifier.height(8.dp))
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.85f).height(14.dp))
            Spacer(modifier = Modifier.height(8.dp))
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.65f).height(14.dp))

            Spacer(modifier = Modifier.height(18.dp))
            // Sample Structured Items Skeleton
            repeat(2) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SkeletonBox(modifier = Modifier.size(24.dp).clip(CircleShape))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            SkeletonBox(modifier = Modifier.fillMaxWidth(0.5f).height(12.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            SkeletonBox(modifier = Modifier.fillMaxWidth(0.8f).height(10.dp))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Skeleton Loader specifically modeled for Diet & Meal Plan Generation Screens
 */
@Composable
fun DietPlanSkeleton(modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                SkeletonBox(modifier = Modifier.width(140.dp).height(20.dp))
                SkeletonBox(modifier = Modifier.width(80.dp).height(24.dp), shape = RoundedCornerShape(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Macro Targets Grid Skeleton
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SkeletonBox(modifier = Modifier.weight(1f).height(60.dp), shape = RoundedCornerShape(12.dp))
                SkeletonBox(modifier = Modifier.weight(1f).height(60.dp), shape = RoundedCornerShape(12.dp))
                SkeletonBox(modifier = Modifier.weight(1f).height(60.dp), shape = RoundedCornerShape(12.dp))
            }

            Spacer(modifier = Modifier.height(18.dp))
            SkeletonBox(modifier = Modifier.fillMaxWidth(0.5f).height(16.dp))
            Spacer(modifier = Modifier.height(10.dp))

            repeat(3) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SkeletonBox(modifier = Modifier.size(32.dp).clip(CircleShape))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            SkeletonBox(modifier = Modifier.fillMaxWidth(0.4f).height(14.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            SkeletonBox(modifier = Modifier.fillMaxWidth(0.7f).height(10.dp))
                        }
                    }
                }
            }
        }
    }
}


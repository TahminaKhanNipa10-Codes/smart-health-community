package com.example.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MedicalTealContainer
import com.example.ui.theme.MedicalTealPrimary

/**
 * Top Tab Switcher to seamlessly transition between the core Wellness Trilogy:
 * 1. Health Tracker (Vitals & Biometrics)
 * 2. Medicine Reminders (Prescriptions & Schedule)
 * 3. Diet Planner (AI Nutrition & Meal Plans)
 */
@Composable
fun WellnessNavTabs(
    activeRoute: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabs = listOf(
                Triple("health_tracker", "📈 Vitals", AppDestination.HealthTracker.icon),
                Triple("medicines", "💊 Medicines", AppDestination.Medicines.icon),
                Triple("diet_ai", "🥗 Diet AI", AppDestination.DietPlanner.icon)
            )

            tabs.forEach { (route, label, icon) ->
                val isSelected = activeRoute == route
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                    label = "tabBg"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) MedicalTealPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    label = "tabContent"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(bgColor)
                        .clickable { onTabSelected(route) }
                        .padding(vertical = 10.dp)
                        .testTag("wellness_tab_$route"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = contentColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = contentColor
                        )
                    }
                }
            }
        }
    }
}

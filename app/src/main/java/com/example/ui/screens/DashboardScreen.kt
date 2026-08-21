package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.HealthMetricCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    user: UserEntity,
    latestRecord: HealthRecordEntity?,
    medicines: List<MedicineEntity>,
    appointments: List<AppointmentEntity>,
    articles: List<ArticleEntity>,
    onQuickAddWater: () -> Unit,
    onToggleMedicine: (MedicineEntity) -> Unit,
    onNavigate: (String) -> Unit
) {
    val activeMedicines = medicines.filter { it.isActive }
    val nextAppointment = appointments.firstOrNull { it.status == "CONFIRMED" || it.status == "PENDING" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp)
    ) {
        // Welcome & Health Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_welcome_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(MedicalTealPrimary, MedicalBlueSecondary)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "Welcome, ${user.fullName} 👋",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = "Blood Group: ${user.bloodGroup} • Role: ${user.role}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = user.bloodGroup,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick emergency alert line inside banner
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .clickable { onNavigate("emergency") }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Emergency,
                                contentDescription = "Emergency",
                                tint = Color(0xFFFFD1D1),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Emergency SOS & 24/7 Hospital Finder",
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Role-specific quick entry banner for Doctor or Admin
                        if (user.role == "DOCTOR" || user.role == "ADMIN") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.22f))
                                    .clickable { onNavigate(if (user.role == "ADMIN") "admin_panel" else "doctor_panel") }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = if (user.role == "ADMIN") Icons.Default.AdminPanelSettings else Icons.Default.MedicalServices,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (user.role == "ADMIN") "🛡️ Open Admin Oversight & Doctor Management" else "👨‍⚕️ Open Doctor Care Portal & Schedule",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Vital Statistics Grid
        item {
            Text(
                text = "Today's Vital Summary",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HealthMetricCard(
                    title = "Heart Rate",
                    value = latestRecord?.heartRate?.toString() ?: "--",
                    unit = if (latestRecord != null) "bpm" else "",
                    subtitle = if (latestRecord != null) "Resting Normal" else "No data",
                    icon = Icons.Default.Favorite,
                    accentColor = HealthHeartPink,
                    onClick = { onNavigate("health_tracker") },
                    modifier = Modifier.weight(1f)
                )

                HealthMetricCard(
                    title = "Blood Pressure",
                    value = if (latestRecord != null) "${latestRecord.systolic}/${latestRecord.diastolic}" else "--/--",
                    unit = if (latestRecord != null) "mmHg" else "",
                    subtitle = if (latestRecord != null) "Optimal" else "No data",
                    icon = Icons.Default.Speed,
                    accentColor = MedicalTealPrimary,
                    onClick = { onNavigate("health_tracker") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val hasVitals = latestRecord != null
                val bmiVal = latestRecord?.bmi
                HealthMetricCard(
                    title = "BMI Index",
                    value = if (bmiVal != null) String.format("%.1f", bmiVal) else "--",
                    unit = if (hasVitals) "kg/m²" else "",
                    subtitle = latestRecord?.bmiCategory ?: "No vitals logged yet",
                    icon = Icons.Default.AccessibilityNew,
                    accentColor = MedicalGreenTertiary,
                    onClick = { onNavigate("health_tracker") },
                    modifier = Modifier.weight(1f)
                )

                HealthMetricCard(
                    title = "Hydration",
                    value = latestRecord?.let { "${it.waterMl}" } ?: "--",
                    unit = if (latestRecord != null) "ml" else "",
                    subtitle = if (latestRecord != null) "+250ml Tap Log" else "Tap to log water",
                    icon = Icons.Default.WaterDrop,
                    accentColor = MedicalBlueSecondary,
                    onClick = { onQuickAddWater() },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Shortcuts Carousel
        item {
            Text(
                text = "Care & AI Shortcuts",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    CareShortcutItem(
                        title = "Rx & Lab AI",
                        subtitle = "Scan & Extract",
                        icon = Icons.Default.DocumentScanner,
                        color = MedicalTealPrimary,
                        onClick = { onNavigate("document_analyzer") }
                    )
                }
                item {
                    CareShortcutItem(
                        title = "AI Health",
                        subtitle = "Symptom Q&A",
                        icon = Icons.Default.AutoAwesome,
                        color = MedicalTealPrimary,
                        onClick = { onNavigate("ai_assistant") }
                    )
                }
                item {
                    CareShortcutItem(
                        title = "Diet AI",
                        subtitle = "Meal Planner",
                        icon = Icons.Default.Restaurant,
                        color = MedicalGreenTertiary,
                        onClick = { onNavigate("diet_ai") }
                    )
                }
                item {
                    CareShortcutItem(
                        title = "Emergency",
                        subtitle = "1-Tap SOS",
                        icon = Icons.Default.Emergency,
                        color = HealthEmergencyRed,
                        onClick = { onNavigate("emergency") }
                    )
                }
                item {
                    CareShortcutItem(
                        title = "Blood Help",
                        subtitle = "Donors & Needs",
                        icon = Icons.Default.Bloodtype,
                        color = HealthBloodRed,
                        onClick = { onNavigate("blood_donation") }
                    )
                }
                item {
                    CareShortcutItem(
                        title = "EHR Records",
                        subtitle = "Lab & Rx",
                        icon = Icons.Default.FolderShared,
                        color = MedicalBlueSecondary,
                        onClick = { onNavigate("medical_records") }
                    )
                }
                item {
                    CareShortcutItem(
                        title = "Vaccinations",
                        subtitle = "Immunization",
                        icon = Icons.Default.Vaccines,
                        color = Color(0xFF8B5CF6),
                        onClick = { onNavigate("vaccinations") }
                    )
                }
            }
        }

        // Medicine Schedule for Today
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Today's Medicine Reminders",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                TextButton(onClick = { onNavigate("medicines") }) {
                    Text("View All (${activeMedicines.size})")
                }
            }

            if (activeMedicines.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MedicalGreenTertiary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "No active medicines scheduled. Tap to add one!",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    activeMedicines.take(3).forEach { med ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (med.isTakenToday) MedicalGreenContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(14.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    IconButton(
                                        onClick = { onToggleMedicine(med) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (med.isTakenToday) Icons.Default.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                            contentDescription = "Mark as taken",
                                            tint = if (med.isTakenToday) MedicalGreenTertiary else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = med.name,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${med.dosage} • ${med.timeOfDay} (${med.instructions})",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                StatusBadge(status = if (med.isTakenToday) "TAKEN" else "DUE")
                            }
                        }
                    }
                }
            }
        }

        // Upcoming Consultation Card
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Upcoming Doctor Visit",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                TextButton(onClick = { onNavigate("doctors") }) {
                    Text("Directory")
                }
            }

            if (nextAppointment != null) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MedicalBlueContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.MedicalServices,
                                        contentDescription = null,
                                        tint = MedicalBlueSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = nextAppointment.doctorName,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = nextAppointment.doctorSpecialty,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            StatusBadge(status = nextAppointment.status)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${nextAppointment.date} at ${nextAppointment.timeSlot}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                            TextButton(
                                onClick = { onNavigate("doctors") },
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("Details", fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "No upcoming doctor appointments",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "Book verified cardiologists, pediatricians & GPs",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = { onNavigate("doctors") },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MedicalTealPrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Book", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Featured Health Education
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Health Education & Insights",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                TextButton(onClick = { onNavigate("articles") }) {
                    Text("Explore All")
                }
            }

            articles.firstOrNull()?.let { article ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate("articles") }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Surface(
                            color = MedicalTealContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = article.category,
                                color = MedicalTealOnContainer,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = article.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = article.excerpt,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "By ${article.author} • ${article.readTimeMinutes} min read",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CareShortcutItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .width(130.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}

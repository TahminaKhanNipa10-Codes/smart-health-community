package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.HealthRecordEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.HealthMetricCard
import com.example.ui.components.HealthTrendLineChart
import com.example.ui.navigation.WellnessNavTabs
import com.example.ui.theme.*

@Composable
fun HealthTrackerScreen(
    records: List<HealthRecordEntity>,
    onAddRecord: (
        weightKg: Float,
        heightCm: Float,
        systolic: Int,
        diastolic: Int,
        heartRate: Int,
        bloodSugarMg: Float,
        temperatureC: Float,
        waterMl: Int,
        sleepHours: Float,
        steps: Int,
        exerciseMinutes: Int,
        mood: String,
        notes: String
    ) -> Unit,
    onDeleteRecord: (HealthRecordEntity) -> Unit,
    onNavigateToWellnessTab: (String) -> Unit = {}
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedMetricTab by remember { mutableStateOf(0) } // 0: Heart Rate, 1: Blood Pressure, 2: Blood Sugar, 3: Weight/BMI

    val latest = records.firstOrNull()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MedicalTealPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("health_tracker_fab_add")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Log Vitals")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Log Vitals", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
        ) {
            // Quick Wellness Trilogy Navigation Tabs
            item {
                WellnessNavTabs(
                    activeRoute = "health_tracker",
                    onTabSelected = onNavigateToWellnessTab,
                    modifier = Modifier.padding(horizontal = 0.dp)
                )
            }

            // Header summary
            item {
                Text(
                    text = "Health & Vitals Tracker",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Monitor cardiovascular, metabolic and physical health biomarkers",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // BMI & Metabolic Assessment Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "Body Mass Index (BMI)",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                val hasVitals = latest != null
                                val currentBmi = latest?.bmi
                                val category = latest?.bmiCategory ?: "No vitals logged yet"
                                Text(
                                    text = if (currentBmi != null) "Current: ${String.format("%.1f", currentBmi)} kg/m² • $category" else "No vitals logged yet • Tap Log Vitals below",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = when (category) {
                                        "Normal Weight" -> MedicalGreenTertiary
                                        "Underweight" -> MedicalBlueSecondary
                                        "Overweight" -> Color(0xFFEAB308)
                                        "No vitals logged yet" -> MaterialTheme.colorScheme.onSurfaceVariant
                                        else -> HealthEmergencyRed
                                    },
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MedicalTealContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AccessibilityNew, contentDescription = null, tint = MedicalTealPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Visual BMI Gauge Bar
                        val bmi = (latest?.bmi ?: 0f).coerceIn(0f, 40f)
                        val progress = if (latest != null) ((bmi - 15f) / 25f).coerceIn(0f, 1f) else 0f
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = when {
                                latest == null -> MaterialTheme.colorScheme.surfaceVariant
                                bmi < 18.5f -> MedicalBlueSecondary
                                bmi < 25f -> MedicalGreenTertiary
                                bmi < 30f -> Color(0xFFEAB308)
                                else -> HealthEmergencyRed
                            },
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("< 18.5 (Under)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("18.5 - 24.9 (Normal)", style = MaterialTheme.typography.labelSmall, color = MedicalGreenTertiary, fontWeight = FontWeight.Bold)
                            Text("25 - 29.9 (Over)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("30+ (Obese)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Interactive Trend Chart Section
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Biomarker Trends",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        ScrollableTabRow(
                            selectedTabIndex = selectedMetricTab,
                            containerColor = Color.Transparent,
                            edgePadding = 0.dp,
                            divider = {}
                        ) {
                            Tab(
                                selected = selectedMetricTab == 0,
                                onClick = { selectedMetricTab = 0 },
                                text = { Text("Heart Rate", fontSize = 12.sp, maxLines = 1, softWrap = false) }
                            )
                            Tab(
                                selected = selectedMetricTab == 1,
                                onClick = { selectedMetricTab = 1 },
                                text = { Text("Blood Pressure", fontSize = 12.sp, maxLines = 1, softWrap = false) }
                            )
                            Tab(
                                selected = selectedMetricTab == 2,
                                onClick = { selectedMetricTab = 2 },
                                text = { Text("Blood Sugar", fontSize = 12.sp, maxLines = 1, softWrap = false) }
                            )
                            Tab(
                                selected = selectedMetricTab == 3,
                                onClick = { selectedMetricTab = 3 },
                                text = { Text("Steps", fontSize = 12.sp, maxLines = 1, softWrap = false) }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val dataPoints = when (selectedMetricTab) {
                            0 -> records.map { it.heartRate.toFloat() }.reversed().ifEmpty { listOf(68f, 72f, 70f, 74f, 70f) }
                            1 -> records.map { it.systolic.toFloat() }.reversed().ifEmpty { listOf(124f, 120f, 118f, 122f, 118f) }
                            2 -> records.map { it.bloodSugarMg }.reversed().ifEmpty { listOf(98f, 95f, 92f, 96f, 92f) }
                            else -> records.map { it.steps.toFloat() }.reversed().ifEmpty { listOf(5000f, 7200f, 8100f, 6200f, 8450f) }
                        }

                        val chartColor = when (selectedMetricTab) {
                            0 -> HealthHeartPink
                            1 -> MedicalTealPrimary
                            2 -> Color(0xFFF97316)
                            else -> MedicalGreenTertiary
                        }

                        HealthTrendLineChart(
                            dataPoints = dataPoints,
                            lineColor = chartColor
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when (selectedMetricTab) {
                                0 -> "Average Resting Heart Rate: ${dataPoints.average().toInt()} bpm (Normal range: 60-100)"
                                1 -> "Average Systolic Pressure: ${dataPoints.average().toInt()} mmHg (Target: < 120)"
                                2 -> "Average Fasting Glucose: ${String.format("%.1f", dataPoints.average())} mg/dL (Target: 70-99)"
                                else -> "Daily Steps Goal: 8,000 steps"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Historical Logs Header
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Historical Logs (${records.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            if (records.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.FavoriteBorder,
                        title = "No Health Logs Recorded",
                        description = "Start by logging your daily blood pressure, weight, heart rate, or steps.",
                        actionButtonText = "Log First Reading",
                        onActionClick = { showAddDialog = true }
                    )
                }
            } else {
                items(records) { item ->
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = item.dateString,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = MedicalTealContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "Mood: ${item.mood}",
                                            color = MedicalTealOnContainer,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { onDeleteRecord(item) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = "Delete record",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Blood Pressure", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${item.systolic}/${item.diastolic} mmHg", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                }
                                Column {
                                    Text("Heart Rate", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${item.heartRate} bpm", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = HealthHeartPink)
                                }
                                Column {
                                    Text("Blood Sugar", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${item.bloodSugarMg} mg/dL", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                }
                                Column {
                                    Text("Water", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${item.waterMl} ml", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MedicalBlueSecondary)
                                }
                            }

                            if (item.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Notes: ${item.notes}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddHealthRecordDialog(
            onDismiss = { showAddDialog = false },
            onSave = { w, h, sys, dia, hr, sugar, temp, water, sleep, steps, ex, mood, notes ->
                onAddRecord(w, h, sys, dia, hr, sugar, temp, water, sleep, steps, ex, mood, notes)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddHealthRecordDialog(
    onDismiss: () -> Unit,
    onSave: (
        weightKg: Float,
        heightCm: Float,
        systolic: Int,
        diastolic: Int,
        heartRate: Int,
        bloodSugarMg: Float,
        temperatureC: Float,
        waterMl: Int,
        sleepHours: Float,
        steps: Int,
        exerciseMinutes: Int,
        mood: String,
        notes: String
    ) -> Unit
) {
    var weightStr by remember { mutableStateOf("72.5") }
    var heightStr by remember { mutableStateOf("178") }
    var systolicStr by remember { mutableStateOf("120") }
    var diastolicStr by remember { mutableStateOf("80") }
    var hrStr by remember { mutableStateOf("72") }
    var sugarStr by remember { mutableStateOf("95") }
    var tempStr by remember { mutableStateOf("36.6") }
    var waterStr by remember { mutableStateOf("2000") }
    var sleepStr by remember { mutableStateOf("7.5") }
    var stepsStr by remember { mutableStateOf("8000") }
    var exerciseStr by remember { mutableStateOf("30") }
    var mood by remember { mutableStateOf("Good") }
    var notes by remember { mutableStateOf("") }

    val moods = listOf("Great", "Good", "Calm", "Tired", "Stressed", "Unwell")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Log Daily Vitals",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Weight & Height
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = weightStr,
                        onValueChange = { weightStr = it },
                        label = { Text("Weight (kg)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = heightStr,
                        onValueChange = { heightStr = it },
                        label = { Text("Height (cm)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Blood Pressure (Sys / Dia)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = systolicStr,
                        onValueChange = { systolicStr = it },
                        label = { Text("Systolic (mmHg)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = diastolicStr,
                        onValueChange = { diastolicStr = it },
                        label = { Text("Diastolic (mmHg)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Heart Rate & Blood Sugar
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = hrStr,
                        onValueChange = { hrStr = it },
                        label = { Text("Heart Rate (bpm)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = sugarStr,
                        onValueChange = { sugarStr = it },
                        label = { Text("Blood Sugar (mg/dL)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Water & Sleep
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = waterStr,
                        onValueChange = { waterStr = it },
                        label = { Text("Water (ml)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = sleepStr,
                        onValueChange = { sleepStr = it },
                        label = { Text("Sleep (hours)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Steps & Exercise
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = stepsStr,
                        onValueChange = { stepsStr = it },
                        label = { Text("Steps") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = exerciseStr,
                        onValueChange = { exerciseStr = it },
                        label = { Text("Exercise (mins)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("How are you feeling today?", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    moods.take(3).forEach { m ->
                        FilterChip(
                            selected = mood == m,
                            onClick = { mood = m },
                            label = { Text(m, fontSize = 12.sp) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    moods.drop(3).forEach { m ->
                        FilterChip(
                            selected = mood == m,
                            onClick = { mood = m },
                            label = { Text(m, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Symptoms / Activity Notes") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val w = weightStr.toFloatOrNull() ?: 70f
                        val h = heightStr.toFloatOrNull() ?: 175f
                        val sys = systolicStr.toIntOrNull() ?: 120
                        val dia = diastolicStr.toIntOrNull() ?: 80
                        val hr = hrStr.toIntOrNull() ?: 72
                        val sugar = sugarStr.toFloatOrNull() ?: 95f
                        val temp = tempStr.toFloatOrNull() ?: 36.6f
                        val water = waterStr.toIntOrNull() ?: 2000
                        val sleep = sleepStr.toFloatOrNull() ?: 7.5f
                        val steps = stepsStr.toIntOrNull() ?: 8000
                        val ex = exerciseStr.toIntOrNull() ?: 30

                        onSave(w, h, sys, dia, hr, sugar, temp, water, sleep, steps, ex, mood, notes)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalTealPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Save Record", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

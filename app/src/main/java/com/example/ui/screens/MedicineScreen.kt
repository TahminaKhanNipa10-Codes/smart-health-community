package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
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
import com.example.data.model.MedicineEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusBadge
import com.example.ui.navigation.WellnessNavTabs
import com.example.ui.theme.*

@Composable
fun MedicineScreen(
    medicines: List<MedicineEntity>,
    onAddMedicine: (
        name: String,
        dosage: String,
        frequency: String,
        timeOfDay: String,
        startDate: String,
        endDate: String,
        instructions: String,
        notes: String
    ) -> Unit,
    onToggleTaken: (MedicineEntity) -> Unit,
    onDeleteMedicine: (MedicineEntity) -> Unit,
    onNavigateToWellnessTab: (String) -> Unit = {}
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "DUE", "TAKEN"

    val filteredMedicines = when (selectedFilter) {
        "DUE" -> medicines.filter { !it.isTakenToday && it.isActive }
        "TAKEN" -> medicines.filter { it.isTakenToday && it.isActive }
        else -> medicines
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MedicalTealPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("medicine_fab_add")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Medicine")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Schedule", fontWeight = FontWeight.Bold)
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
        ) {
            // Quick Wellness Trilogy Navigation Tabs
            item {
                WellnessNavTabs(
                    activeRoute = "medicines",
                    onTabSelected = onNavigateToWellnessTab,
                    modifier = Modifier.padding(horizontal = 0.dp)
                )
            }

            item {
                Text(
                    text = "Medicine Reminders & Prescriptions",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Never miss a dose with synchronized intake reminders",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Status Summary Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = { Text("All Prescriptions (${medicines.size})", maxLines = 1, softWrap = false) }
                    )
                    FilterChip(
                        selected = selectedFilter == "DUE",
                        onClick = { selectedFilter = "DUE" },
                        label = { Text("Due Today (${medicines.count { !it.isTakenToday && it.isActive }})", maxLines = 1, softWrap = false) }
                    )
                    FilterChip(
                        selected = selectedFilter == "TAKEN",
                        onClick = { selectedFilter = "TAKEN" },
                        label = { Text("Completed (${medicines.count { it.isTakenToday && it.isActive }})", maxLines = 1, softWrap = false) }
                    )
                }
            }

            if (filteredMedicines.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.Medication,
                        title = "No Medications Found",
                        description = "Keep track of daily vitamins, antibiotics, or chronic prescriptions.",
                        actionButtonText = "Add First Medication",
                        onActionClick = { showAddDialog = true }
                    )
                }
            } else {
                items(filteredMedicines) { med ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (med.isTakenToday) MedicalGreenContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                        ),
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
                                    IconButton(
                                        onClick = { onToggleTaken(med) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (med.isTakenToday) Icons.Default.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                            contentDescription = "Toggle taken",
                                            tint = if (med.isTakenToday) MedicalGreenTertiary else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = med.name,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "${med.dosage} • ${med.frequency}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    StatusBadge(status = if (med.isTakenToday) "TAKEN" else "DUE")
                                    IconButton(
                                        onClick = { onDeleteMedicine(med) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.DeleteOutline,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Schedule,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = med.timeOfDay,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Restaurant,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = med.instructions,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (med.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Notes: ${med.notes}",
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
        AddMedicineDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, dose, freq, time, start, end, instr, notes ->
                onAddMedicine(name, dose, freq, time, start, end, instr, notes)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddMedicineDialog(
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        dosage: String,
        frequency: String,
        timeOfDay: String,
        startDate: String,
        endDate: String,
        instructions: String,
        notes: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("500 mg") }
    var frequency by remember { mutableStateOf("Once Daily") }
    var timeOfDay by remember { mutableStateOf("08:00 AM") }
    var startDate by remember { mutableStateOf("2026-08-15") }
    var endDate by remember { mutableStateOf("2026-09-15") }
    var instructions by remember { mutableStateOf("After meal with water") }
    var notes by remember { mutableStateOf("") }

    val frequencies = listOf("Once Daily", "Twice Daily", "Three Times Daily", "As Needed")
    val instructionsList = listOf("After meal", "Before meal", "With water", "Bedtime")

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
                        text = "Add Medicine Schedule",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Medicine Name (e.g. Amoxicillin, Metformin)") },
                    leadingIcon = { Icon(Icons.Default.Medication, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = { Text("Dosage (e.g. 1000 mg)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = timeOfDay,
                        onValueChange = { timeOfDay = it },
                        label = { Text("Time (e.g. 08:00 AM)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Frequency:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    frequencies.take(2).forEach { f ->
                        FilterChip(
                            selected = frequency == f,
                            onClick = { frequency = f },
                            label = { Text(f, fontSize = 12.sp) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    frequencies.drop(2).forEach { f ->
                        FilterChip(
                            selected = frequency == f,
                            onClick = { frequency = f },
                            label = { Text(f, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Intake Instruction:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    instructionsList.forEach { inst ->
                        FilterChip(
                            selected = instructions == inst,
                            onClick = { instructions = inst },
                            label = { Text(inst, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Start Date") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("End Date") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Physician Notes / Purpose") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onSave(name, dosage, frequency, timeOfDay, startDate, endDate, instructions, notes)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalTealPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Save Medication Schedule", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

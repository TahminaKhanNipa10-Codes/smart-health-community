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
import com.example.data.model.VaccinationEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@Composable
fun VaccinationScreen(
    vaccinations: List<VaccinationEntity>,
    onAddVaccination: (
        name: String,
        doseNumber: String,
        dateReceived: String,
        nextDoseDate: String,
        doctorHospital: String,
        status: String,
        notes: String
    ) -> Unit,
    onDeleteVaccination: (VaccinationEntity) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredList = when (selectedFilter) {
        "COMPLETED" -> vaccinations.filter { it.status == "COMPLETED" }
        "UPCOMING" -> vaccinations.filter { it.status == "UPCOMING" || it.status == "OVERDUE" }
        else -> vaccinations
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MedicalTealPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("vaccination_fab_add")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Vaccine")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Vaccine", fontWeight = FontWeight.Bold)
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
            contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
        ) {
            item {
                Text(
                    text = "Immunization & Vaccine Passport",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Track your complete vaccination schedule and booster deadlines",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

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
                        label = { Text("All Records (${vaccinations.size})", maxLines = 1, softWrap = false) }
                    )
                    FilterChip(
                        selected = selectedFilter == "COMPLETED",
                        onClick = { selectedFilter = "COMPLETED" },
                        label = { Text("Administered (${vaccinations.count { it.status == "COMPLETED" }})", maxLines = 1, softWrap = false) }
                    )
                    FilterChip(
                        selected = selectedFilter == "UPCOMING",
                        onClick = { selectedFilter = "UPCOMING" },
                        label = { Text("Upcoming Boosters (${vaccinations.count { it.status != "COMPLETED" }})", maxLines = 1, softWrap = false) }
                    )
                }
            }

            if (filteredList.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.Vaccines,
                        title = "No Vaccination Records",
                        description = "Keep digital proof of your childhood, travel, and seasonal vaccines.",
                        actionButtonText = "Record Vaccine",
                        onActionClick = { showAddDialog = true }
                    )
                }
            } else {
                items(filteredList) { v ->
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
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (v.status == "COMPLETED") MedicalGreenContainer else MedicalBlueContainer
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Vaccines,
                                            contentDescription = null,
                                            tint = if (v.status == "COMPLETED") MedicalGreenTertiary else MedicalBlueSecondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = v.vaccineName,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "${v.doseNumber} • ${v.doctorOrHospital}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    StatusBadge(status = v.status)
                                    IconButton(
                                        onClick = { onDeleteVaccination(v) },
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
                                Column {
                                    Text("Date Received", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(v.dateReceived, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                                }
                                if (v.nextDoseDate.isNotBlank()) {
                                    Column {
                                        Text("Next Booster Due", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(v.nextDoseDate, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = MedicalTealPrimary)
                                    }
                                }
                            }

                            if (v.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Batch/Clinical notes: ${v.notes}",
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
        AddVaccinationDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, dose, dateRec, nextDose, docHosp, status, notes ->
                onAddVaccination(name, dose, dateRec, nextDose, docHosp, status, notes)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddVaccinationDialog(
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        doseNumber: String,
        dateReceived: String,
        nextDoseDate: String,
        doctorHospital: String,
        status: String,
        notes: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var doseNumber by remember { mutableStateOf("Dose 1") }
    var dateReceived by remember { mutableStateOf("2026-08-15") }
    var nextDoseDate by remember { mutableStateOf("") }
    var doctorHospital by remember { mutableStateOf("Springfield General Hospital") }
    var status by remember { mutableStateOf("COMPLETED") }
    var notes by remember { mutableStateOf("") }

    val doses = listOf("Dose 1", "Dose 2", "Booster 1", "Annual Seasonal")
    val statuses = listOf("COMPLETED", "UPCOMING", "OVERDUE")

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
                        text = "Add Vaccine Record",
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
                    label = { Text("Vaccine Name (e.g. Hepatitis B, Tdap, Flu)") },
                    leadingIcon = { Icon(Icons.Default.Vaccines, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Dose Type:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    doses.forEach { d ->
                        FilterChip(
                            selected = doseNumber == d,
                            onClick = { doseNumber = d },
                            label = { Text(d, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = dateReceived,
                        onValueChange = { dateReceived = it },
                        label = { Text("Date Given") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = nextDoseDate,
                        onValueChange = { nextDoseDate = it },
                        label = { Text("Next Due Date (Opt)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = doctorHospital,
                    onValueChange = { doctorHospital = it },
                    label = { Text("Administered At (Hospital/Clinic)") },
                    leadingIcon = { Icon(Icons.Default.LocalHospital, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Status:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    statuses.forEach { st ->
                        FilterChip(
                            selected = status == st,
                            onClick = { status = st },
                            label = { Text(st, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Batch Number / Side Effects / Notes") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onSave(name, doseNumber, dateReceived, nextDoseDate, doctorHospital, status, notes)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalTealPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Save Immunization Record", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

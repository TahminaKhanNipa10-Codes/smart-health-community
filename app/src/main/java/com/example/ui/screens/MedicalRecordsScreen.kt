package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.data.model.MedicalRecordEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.*

@Composable
fun MedicalRecordsScreen(
    records: List<MedicalRecordEntity>,
    onAddRecord: (
        title: String,
        doctorName: String,
        hospitalClinic: String,
        recordDate: String,
        category: String,
        notes: String,
        prescriptionText: String,
        labResults: String,
        fileName: String
    ) -> Unit,
    onDeleteRecord: (MedicalRecordEntity) -> Unit,
    onNavigateToAnalyzer: () -> Unit = {}
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedRecordForDetail by remember { mutableStateOf<MedicalRecordEntity?>(null) }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }

    val categories = listOf("ALL", "Lab Report", "Doctor Notes", "Prescription", "Blood Test", "Radiology")

    val filteredRecords = records.filter { record ->
        val matchesCategory = (selectedCategory == "ALL" || record.category.equals(selectedCategory, ignoreCase = true))
        val matchesSearch = searchQuery.isBlank() ||
                record.title.contains(searchQuery, ignoreCase = true) ||
                record.doctorName.contains(searchQuery, ignoreCase = true) ||
                record.hospitalClinic.contains(searchQuery, ignoreCase = true) ||
                record.notes.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MedicalTealPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("medical_records_fab_add")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = "Add EHR Document")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Upload EHR", fontWeight = FontWeight.Bold)
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
                    text = "Electronic Health Records (EHR)",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Secure digital repository for lab panels, clinical notes & scans",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // AI Document Analyzer Card Banner
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MedicalTealContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToAnalyzer() }
                        .testTag("open_ai_doc_analyzer_card")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MedicalTealPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.DocumentScanner,
                                contentDescription = "AI Scanner",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Prescription & Lab Reader AI",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MedicalTealOnContainer
                            )
                            Text(
                                text = "Instantly scan or extract medicines, dosages & lab reference ranges with Gemini AI",
                                style = MaterialTheme.typography.bodySmall,
                                color = MedicalTealOnContainer.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        }
                        Icon(
                            Icons.Default.ArrowForwardIos,
                            contentDescription = "Open",
                            tint = MedicalTealPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }


            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search records, doctors, clinics...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Category Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp, maxLines = 1, softWrap = false) }
                        )
                    }
                }
            }

            if (filteredRecords.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.FolderShared,
                        title = "No Health Records Found",
                        description = "Upload your blood tests, ECG results, or doctor prescriptions.",
                        actionButtonText = "Upload Document",
                        onActionClick = { showAddDialog = true }
                    )
                }
            } else {
                items(filteredRecords) { doc ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedRecordForDetail = doc }
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
                                            .background(MedicalTealContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Description,
                                            contentDescription = null,
                                            tint = MedicalTealPrimary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = doc.title,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "${doc.doctorName} • ${doc.hospitalClinic}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    color = MedicalBlueContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = doc.category,
                                        color = MedicalBlueOnContainer,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.AttachFile,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = doc.fileAttachmentName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Text(
                                    text = doc.recordDate,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Full Details Modal
    selectedRecordForDetail?.let { detailDoc ->
        Dialog(onDismissRequest = { selectedRecordForDetail = null }) {
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
                        Surface(
                            color = MedicalTealContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = detailDoc.category,
                                color = MedicalTealOnContainer,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        IconButton(onClick = { selectedRecordForDetail = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = detailDoc.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Physician: ${detailDoc.doctorName} • ${detailDoc.hospitalClinic}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Date Recorded: ${detailDoc.recordDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(14.dp))

                    if (detailDoc.labResults.isNotBlank()) {
                        Text(
                            text = "Lab Biomarkers & Quantitative Values:",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = detailDoc.labResults,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    if (detailDoc.prescriptionText.isNotBlank()) {
                        Text(
                            text = "Prescription Orders:",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = MedicalGreenContainer,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = detailDoc.prescriptionText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MedicalGreenOnContainer,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    Text(
                        text = "Clinical Findings & Interpretation:",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = detailDoc.notes,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onDeleteRecord(detailDoc)
                                selectedRecordForDetail = null
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Delete")
                        }

                        Button(
                            onClick = { selectedRecordForDetail = null },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MedicalTealPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Done")
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddMedicalRecordDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, doc, hosp, date, cat, notes, rx, lab, file ->
                onAddRecord(title, doc, hosp, date, cat, notes, rx, lab, file)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddMedicalRecordDialog(
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        doctorName: String,
        hospitalClinic: String,
        recordDate: String,
        category: String,
        notes: String,
        prescriptionText: String,
        labResults: String,
        fileName: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var doctorName by remember { mutableStateOf("Dr. Sarah Mitchell") }
    var hospitalClinic by remember { mutableStateOf("Metro General Medical Center") }
    var recordDate by remember { mutableStateOf("2026-08-15") }
    var category by remember { mutableStateOf("Lab Report") }
    var notes by remember { mutableStateOf("") }
    var prescriptionText by remember { mutableStateOf("") }
    var labResults by remember { mutableStateOf("") }
    var fileName by remember { mutableStateOf("diagnostic_scan_2026.pdf") }

    val categories = listOf("Lab Report", "Doctor Notes", "Prescription", "Blood Test", "Radiology")

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
                        text = "Upload Medical Record",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Document Title (e.g. Lipid Panel, Cardiac ECG)") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Category:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.take(3).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.drop(3).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = doctorName,
                        onValueChange = { doctorName = it },
                        label = { Text("Doctor Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = recordDate,
                        onValueChange = { recordDate = it },
                        label = { Text("Date") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = hospitalClinic,
                    onValueChange = { hospitalClinic = it },
                    label = { Text("Hospital / Diagnostic Lab") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = labResults,
                    onValueChange = { labResults = it },
                    label = { Text("Lab Results / Biomarker Data") },
                    placeholder = { Text("e.g. Glucose: 92 mg/dL, HbA1c: 5.2%") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = prescriptionText,
                    onValueChange = { prescriptionText = it },
                    label = { Text("Prescriptions / Medication Orders") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Clinical Assessment / Diagnosis Summary") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onSave(title, doctorName, hospitalClinic, recordDate, category, notes, prescriptionText, labResults, fileName)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalTealPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Save to Electronic Health Record", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

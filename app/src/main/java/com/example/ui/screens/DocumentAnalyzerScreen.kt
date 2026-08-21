package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.DocumentAnalysisResult
import com.example.data.model.ExtractedLabItem
import com.example.data.model.ExtractedMedicine
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentAnalyzerScreen(
    analysisResult: DocumentAnalysisResult?,
    isAnalyzing: Boolean,
    onAnalyze: (documentText: String, documentType: String, base64Image: String?) -> Unit,
    onClear: () -> Unit,
    onSaveToRecords: (DocumentAnalysisResult) -> Unit,
    onImportMedicines: (List<ExtractedMedicine>) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedAnalysisType by remember { mutableStateOf("PRESCRIPTION") }
    var inputText by remember { mutableStateOf("") }
    var activeSampleIndex by remember { mutableIntStateOf(-1) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedBase64 by remember { mutableStateOf<String?>(null) }
    var showSourceDialog by remember { mutableStateOf(false) }
    var showPermissionRationale by remember { mutableStateOf(false) }

    fun processBitmap(bitmap: Bitmap) {
        val maxDimension = 1024
        val scaled = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
            val ratio = minOf(maxDimension.toFloat() / bitmap.width, maxDimension.toFloat() / bitmap.height)
            Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt(), true)
        } else {
            bitmap
        }
        selectedBitmap = scaled
        val outputStream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val bytes = outputStream.toByteArray()
        selectedBase64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        if (inputText.isBlank()) {
            inputText = if (selectedAnalysisType == "PRESCRIPTION") {
                "Scanned Prescription Photo attached for AI OCR & clinical analysis."
            } else {
                "Scanned Lab Report Photo attached for biomarker extraction & analysis."
            }
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            processBitmap(bitmap)
            activeSampleIndex = -1
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            takePictureLauncher.launch(null)
        } else {
            showPermissionRationale = true
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    if (bitmap != null) {
                        processBitmap(bitmap)
                        activeSampleIndex = -1
                    }
                }
            } catch (_: Exception) {}
        }
    }

    if (showSourceDialog) {
        AlertDialog(
            onDismissRequest = { showSourceDialog = false },
            title = {
                Text(
                    text = "Scan or Upload Document",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Choose how to capture your medical document or lab report:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showSourceDialog = false
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED

                                if (hasPermission) {
                                    takePictureLauncher.launch(null)
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = MedicalTealPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Take Photo with Camera", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Capture prescription with your device camera", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showSourceDialog = false
                                pickImageLauncher.launch("image/*")
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = MedicalBlueSecondary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Choose from Gallery / Files", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Select an existing image or document scan", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSourceDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            title = { Text("Camera Permission Required") },
            text = {
                Text("Camera access is needed to take photos of prescriptions and lab reports for AI optical character recognition.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionRationale = false
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                ) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionRationale = false }) {
                    Text("Dismiss")
                }
            }
        )
    }

    val samplePrescriptions = listOf(
        "Rx - Dr. Elizabeth Sterling, MD (Cardiology)\nPatient: Alex Johnson (Age 32)\nDate: August 14, 2026\n1. Atorvastatin 20mg Tablet - 1 tablet once daily at bedtime for 30 days. Take with water. Note: Check lipid profile in 3 months.\n2. CoQ10 100mg Softgel - 1 softgel daily after breakfast for 30 days.\n3. Aspirin 81mg (Low Dose) - 1 tablet once daily with lunch.",
        "Rx - Dr. Marcus Chen, MD (Pulmonology & ENT)\nPatient: Alex Johnson\nDiagnosis: Acute Bronchial Congestion\n1. Amoxicillin-Clavulanate 625mg - 1 tab twice daily (every 12h) with meals for 7 days. Complete full course.\n2. Levocetirizine 5mg - 1 tab at bedtime for 5 days. May cause drowsiness.\n3. Salbutamol Inhaler 100mcg - 2 puffs as needed every 4-6h for wheezing.",
        "Rx - Dr. Sarah Jenkins, MD (Endocrinology)\nPatient: Alex Johnson\n1. Metformin 500mg ER - 1 tab with evening meal for 60 days.\n2. Omega-3 Fish Oil 1000mg - 1 capsule twice daily with meals."
    )

    val sampleLabReports = listOf(
        "LAB REPORT - St. Jude Comprehensive Diagnostics\nTest: Complete Blood Count & Lipid Panel\nFasting Blood Glucose: 108 mg/dL (Reference: 70 - 99 mg/dL) [HIGH]\nHbA1c: 5.6 % (Reference: 4.0 - 5.6 %) [NORMAL]\nTotal Cholesterol: 218 mg/dL (Reference: < 200 mg/dL) [HIGH]\nTriglycerides: 145 mg/dL (Reference: < 150 mg/dL) [NORMAL]\nHDL Cholesterol: 52 mg/dL (Reference: > 40 mg/dL) [NORMAL]\nLDL Cholesterol: 137 mg/dL (Reference: < 100 mg/dL) [HIGH]\nSerum Creatinine: 0.92 mg/dL (Reference: 0.7 - 1.3 mg/dL) [NORMAL]",
        "DIAGNOSTIC REPORT - City Health Pathology Labs\nTest: Renal Function & Electrolytes Panel\nSodium: 140 mEq/L (Reference: 135 - 145 mEq/L) [NORMAL]\nPotassium: 4.4 mEq/L (Reference: 3.5 - 5.0 mEq/L) [NORMAL]\nBlood Urea Nitrogen (BUN): 16 mg/dL (Reference: 7 - 20 mg/dL) [NORMAL]\nEstimated GFR: > 90 mL/min/1.73m2 (Reference: >= 90) [NORMAL]\nUric Acid: 6.8 mg/dL (Reference: 3.5 - 7.2 mg/dL) [NORMAL]",
        "LAB REPORT - Thyroid & Hormonal Profile\nTSH (Thyroid Stimulating Hormone): 2.45 uIU/mL (Reference: 0.45 - 4.50 uIU/mL) [NORMAL]\nFree T4: 1.28 ng/dL (Reference: 0.82 - 1.77 ng/dL) [NORMAL]\nVitamin D (25-Hydroxy): 26.4 ng/mL (Reference: 30.0 - 100.0 ng/mL) [LOW]\nVitamin B12: 480 pg/mL (Reference: 200 - 900 pg/mL) [NORMAL]"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp)
    ) {
        // Hero Header
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Prescription & Lab Analyzer",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "AI-powered clinical OCR & structured extraction. Convert handwritten or printed medical documents into structured records and medication reminders.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MedicalTealContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DocumentScanner,
                                contentDescription = "Scanner",
                                tint = MedicalTealPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Mode Selection Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(4.dp)
                    ) {
                        TabOption(
                            text = "💊 Read Prescription",
                            selected = selectedAnalysisType == "PRESCRIPTION",
                            modifier = Modifier.weight(1f)
                        ) {
                            selectedAnalysisType = "PRESCRIPTION"
                            activeSampleIndex = -1
                            inputText = ""
                            selectedBitmap = null
                            selectedBase64 = null
                        }
                        TabOption(
                            text = "🧪 Analyze Lab Report",
                            selected = selectedAnalysisType == "LAB_REPORT",
                            modifier = Modifier.weight(1f)
                        ) {
                            selectedAnalysisType = "LAB_REPORT"
                            activeSampleIndex = -1
                            inputText = ""
                            selectedBitmap = null
                            selectedBase64 = null
                        }
                    }
                }
            }
        }

        // Input & Sample Document Selector
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (selectedAnalysisType == "PRESCRIPTION") "1. Upload Prescription or Choose Sample" else "1. Upload Lab Report or Choose Sample",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Sample Document Chips
                    Text(
                        text = "Clinical Sample Presets (Tap to test):",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val samples = if (selectedAnalysisType == "PRESCRIPTION") samplePrescriptions else sampleLabReports
                        val titles = if (selectedAnalysisType == "PRESCRIPTION")
                            listOf("Cardiology Rx", "ENT & Antibiotic", "Metabolic Rx")
                        else
                            listOf("Lipid Panel", "Renal Function", "Thyroid Profile")

                        titles.forEachIndexed { index, title ->
                            FilterChip(
                                selected = activeSampleIndex == index,
                                onClick = {
                                    activeSampleIndex = index
                                    inputText = samples[index]
                                    selectedBitmap = null
                                    selectedBase64 = null
                                },
                                label = { Text(title, fontSize = 11.sp, maxLines = 1, softWrap = false) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MedicalTealContainer,
                                    selectedLabelColor = MedicalTealPrimary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Document Image Preview if photo is selected / taken
                    if (selectedBitmap != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MedicalTealContainer.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, MedicalTealPrimary.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    bitmap = selectedBitmap!!.asImageBitmap(),
                                    contentDescription = "Document Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MedicalTealPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Photo Attached",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MedicalTealPrimary
                                        )
                                    }
                                    Text(
                                        text = "Image ready for optical recognition",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        selectedBitmap = null
                                        selectedBase64 = null
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove photo",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Document Text Area / OCR Input
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = {
                            inputText = it
                            activeSampleIndex = -1
                        },
                        label = {
                            Text(if (selectedAnalysisType == "PRESCRIPTION") "Prescription Document Text / OCR" else "Lab Test Values & Report Text")
                        },
                        placeholder = {
                            Text(
                                if (selectedAnalysisType == "PRESCRIPTION")
                                    "Paste or type prescription details (e.g. Dr. Name, Medicines, Dosage, Frequency, Duration)..."
                                else
                                    "Paste or type lab report details (e.g. Test names, results, units, reference intervals)..."
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .testTag("document_text_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action Buttons: Capture Camera / Scan / Analyze
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                showSourceDialog = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Camera", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Scan / Photo", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val textToAnalyze = inputText.ifBlank {
                                    if (selectedBase64 != null) {
                                        "Scanned medical document image attached for OCR & clinical analysis."
                                    } else if (selectedAnalysisType == "PRESCRIPTION") {
                                        samplePrescriptions[0]
                                    } else {
                                        sampleLabReports[0]
                                    }
                                }
                                onAnalyze(textToAnalyze, selectedAnalysisType, selectedBase64)
                            },
                            enabled = !isAnalyzing,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MedicalTealPrimary),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(48.dp)
                                .testTag("analyze_document_button")
                        ) {
                            if (isAnalyzing) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analyzing...", fontSize = 13.sp)
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Analyze with AI", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Loading Feedback Skeleton during AI Analysis
        if (isAnalyzing) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    com.example.ui.components.AiProcessingCard(
                        title = "Reading Medical Document",
                        stepDescription = if (selectedAnalysisType == "PRESCRIPTION")
                            "Detecting medicines, dosage guidelines, frequencies & contraindications..."
                        else
                            "Extracting laboratory biomarkers, reference ranges & clinical abnormalities...",
                        accentColor = MedicalTealPrimary
                    )
                    com.example.ui.components.DocumentAnalyzerSkeleton()
                }
            }
        }

        // Analysis Results Section
        if (analysisResult != null && !isAnalyzing) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.5.dp, MedicalTealPrimary.copy(alpha = 0.5f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MedicalGreenContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Success",
                                        tint = MedicalGreenOnContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = analysisResult.documentTitle,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${analysisResult.doctorOrLabName} • ${analysisResult.dateDetected}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    selectedBitmap = null
                                    selectedBase64 = null
                                    onClear()
                                }
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        // Clinical Summary
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MedicalBlueContainer.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MedicalBlueSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Clinical Overview & Summary",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MedicalBlueOnContainer
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = analysisResult.clinicalSummary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Key Warnings
                        if (analysisResult.keyWarnings.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFEF3C7),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = HealthWarningAmber,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = analysisResult.keyWarnings,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF92400E)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Extracted Medicines (If Prescription)
            if (analysisResult.medicines.isNotEmpty()) {
                item {
                    Text(
                        text = "Extracted Medications (${analysisResult.medicines.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                items(analysisResult.medicines) { med ->
                    ExtractedMedicineCard(medicine = med)
                }

                item {
                    Button(
                        onClick = { onImportMedicines(analysisResult.medicines) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MedicalBlueSecondary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("import_medicines_button")
                    ) {
                        Icon(Icons.Default.AlarmAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add All to Daily Medication Schedule", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Extracted Lab Items (If Lab Report)
            if (analysisResult.labItems.isNotEmpty()) {
                item {
                    Text(
                        text = "Extracted Diagnostic Test Markers (${analysisResult.labItems.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                items(analysisResult.labItems) { lab ->
                    ExtractedLabCard(labItem = lab)
                }
            }

            // Save to EHR Record Action
            item {
                Button(
                    onClick = { onSaveToRecords(analysisResult) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalGreenTertiary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_to_records_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Analysis to Electronic Health Records (EHR)", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Informational Safety Notice
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Shield,
                        contentDescription = null,
                        tint = MedicalTealPrimary,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Clinical AI Safety Notice: Document analysis is intended for personal health tracking and informational convenience. Always confirm prescriptions, dosages, and abnormal lab readings with your prescribing physician or certified diagnostic laboratory.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TabOption(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) MaterialTheme.colorScheme.surface else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 12.sp,
            color = if (selected) MedicalTealPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ExtractedMedicineCard(medicine: ExtractedMedicine) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MedicalTealContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Medication,
                            contentDescription = null,
                            tint = MedicalTealPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = medicine.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${medicine.dosage} • ${medicine.dosageForm}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(status = medicine.frequency)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Duration: ${medicine.duration}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = medicine.instructions, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            if (medicine.warnings.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⚠️ Instructions / Caution: ${medicine.warnings}",
                    style = MaterialTheme.typography.bodySmall,
                    color = HealthWarningAmber,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun ExtractedLabCard(labItem: ExtractedLabItem) {
    val isAbnormal = labItem.status.equals("HIGH", ignoreCase = true) ||
            labItem.status.equals("LOW", ignoreCase = true) ||
            labItem.status.equals("ABNORMAL", ignoreCase = true)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = labItem.testName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                StatusBadge(
                    status = labItem.status,
                    containerColor = if (isAbnormal) Color(0xFFFFE4E6) else MedicalGreenContainer,
                    contentColor = if (isAbnormal) HealthEmergencyRed else MedicalGreenOnContainer
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = labItem.resultValue,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isAbnormal) HealthEmergencyRed else MedicalTealPrimary
                )
                if (labItem.unit.isNotBlank()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = labItem.unit,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Ref: ${labItem.referenceRange}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = labItem.explanation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

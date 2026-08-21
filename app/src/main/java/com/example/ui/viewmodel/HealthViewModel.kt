package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiAiService
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.HealthRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HealthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HealthRepository

    init {
        val db = AppDatabase.getDatabase(application)
        val ai = GeminiAiService()
        repository = HealthRepository(db.healthDao(), ai)
    }

    private val prefs = application.getSharedPreferences("health_app_prefs", android.content.Context.MODE_PRIVATE)

    // Theme Mode: "LIGHT", "DARK", "SYSTEM" (Default is LIGHT as per healthcare-first design)
    private val _themeMode = MutableStateFlow(prefs.getString("theme_mode", "LIGHT") ?: "LIGHT")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode).apply()
        val label = when (mode) {
            "DARK" -> "Dark Mode"
            "SYSTEM" -> "System Default"
            else -> "Light Mode"
        }
        showFeedback("Appearance set to $label")
    }

    // App Preferences
    private val _notificationsEnabled = MutableStateFlow(prefs.getBoolean("notifications_enabled", true))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
    }

    private val _vitalAlertsEnabled = MutableStateFlow(prefs.getBoolean("vital_alerts_enabled", true))
    val vitalAlertsEnabled: StateFlow<Boolean> = _vitalAlertsEnabled.asStateFlow()

    fun setVitalAlertsEnabled(enabled: Boolean) {
        _vitalAlertsEnabled.value = enabled
        prefs.edit().putBoolean("vital_alerts_enabled", enabled).apply()
    }

    // Navigation state
    private val _currentScreen = MutableStateFlow("dashboard")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    private val screenHistory = mutableListOf<String>()

    fun navigateTo(screen: String) {
        if (_currentScreen.value != screen) {
            screenHistory.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (screenHistory.isNotEmpty()) {
            val prev = screenHistory.removeAt(screenHistory.size - 1)
            _currentScreen.value = prev
            return true
        }
        return false
    }

    // Toast / Feedback
    private val _userFeedback = MutableStateFlow<String?>(null)
    val userFeedback: StateFlow<String?> = _userFeedback.asStateFlow()

    fun showFeedback(msg: String) {
        _userFeedback.value = msg
    }

    fun clearFeedback() {
        _userFeedback.value = null
    }

    // Auth State
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private var currentUserJob: kotlinx.coroutines.Job? = null

    private fun observeCurrentUser(userId: String) {
        repository.startUserSync(userId)
        currentUserJob?.cancel()
        currentUserJob = viewModelScope.launch {
            repository.getUserFlow(userId).collect { user ->
                if (user != null) {
                    _currentUser.value = user
                }
            }
        }
    }

    fun refreshCurrentUser() {
        viewModelScope.launch {
            val current = _currentUser.value ?: return@launch
            val latest = repository.getUser(current.id)
            if (latest != null) {
                _currentUser.value = latest
            }
        }
    }

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private fun getRoleHomeDestination(role: String): String {
        return when (role.uppercase()) {
            "DOCTOR" -> "doctor_panel"
            "ADMIN" -> "admin_panel"
            else -> "dashboard"
        }
    }

    init {
        // Check if there is an active Firebase Auth session on startup
        viewModelScope.launch {
            val firebaseUser = repository.getCurrentFirebaseUser()
            if (firebaseUser != null) {
                val uid = firebaseUser.uid
                val email = firebaseUser.email ?: ""
                var user = repository.getUser(uid)
                if (user == null && email.isNotBlank()) {
                    user = repository.getUserByEmail(email)
                }
                if (user != null) {
                    if (user.isActive) {
                        _currentUser.value = user
                        observeCurrentUser(user.id)
                        _currentScreen.value = getRoleHomeDestination(user.role)
                    } else {
                        repository.logoutFirebaseAuth()
                        _currentUser.value = null
                        _currentScreen.value = "login"
                    }
                } else {
                    val newUser = UserEntity(
                        id = uid,
                        email = email,
                        fullName = firebaseUser.displayName?.ifBlank { "Smart Health Member" } ?: "Smart Health Member",
                        role = "USER"
                    )
                    repository.insertUser(newUser)
                    _currentUser.value = newUser
                    observeCurrentUser(newUser.id)
                    _currentScreen.value = getRoleHomeDestination(newUser.role)
                }
            } else {
                _currentUser.value = null
                _currentScreen.value = "login"
            }
        }
    }

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authError.value = "Please provide both email and password."
            return
        }
        _authLoading.value = true
        _authError.value = null
        viewModelScope.launch {
            val trimmedEmail = email.trim()

            // Firebase Auth Login is the ONLY authority for email/password authentication
            val authResult = repository.loginWithFirebase(trimmedEmail, pass)
            if (authResult.isSuccess) {
                val firebaseUser = authResult.getOrNull()
                val uid = firebaseUser?.uid
                var user = if (uid != null) repository.getUser(uid) else null
                if (user == null) {
                    user = repository.getUserByEmail(trimmedEmail)
                }
                _authLoading.value = false
                if (user != null) {
                    if (!user.isActive) {
                        _authError.value = "This account has been deactivated. Please contact support."
                        return@launch
                    }
                    _currentUser.value = user
                    observeCurrentUser(user.id)
                    val targetScreen = getRoleHomeDestination(user.role)
                    showFeedback("Welcome back, ${user.fullName} (${user.role})!")
                    _currentScreen.value = targetScreen
                } else {
                    // Create user profile in Firestore and local db if first time
                    val newUser = UserEntity(
                        id = uid ?: ("user_" + System.currentTimeMillis()),
                        email = trimmedEmail,
                        fullName = firebaseUser?.displayName?.ifBlank { "Smart Health Member" } ?: "Smart Health Member",
                        role = "USER"
                    )
                    repository.insertUser(newUser)
                    _currentUser.value = newUser
                    observeCurrentUser(newUser.id)
                    showFeedback("Welcome to Smart Health, ${newUser.fullName}!")
                    _currentScreen.value = "dashboard"
                }
            } else {
                _authLoading.value = false
                val exception = authResult.exceptionOrNull()
                val errorMsg = when (exception) {
                    is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ->
                        "Invalid email or password. Please check your credentials or register a new account."
                    is com.google.firebase.auth.FirebaseAuthInvalidUserException ->
                        "No account found with this email. Please register first."
                    else -> exception?.localizedMessage
                        ?: "Authentication failed. Please check your credentials or register."
                }
                _authError.value = errorMsg
            }
        }
    }

    fun setAuthLoading(loading: Boolean) {
        _authLoading.value = loading
    }

    fun setAuthError(error: String?) {
        _authError.value = error
    }

    fun loginWithGoogleCredential(idToken: String, fallbackEmail: String? = null, fallbackName: String? = null) {
        _authLoading.value = true
        _authError.value = null
        viewModelScope.launch {
            val authResult = repository.signInWithGoogleCredential(idToken)
            if (authResult.isSuccess) {
                val firebaseUser = authResult.getOrNull()
                val uid = firebaseUser?.uid ?: ("user_" + System.currentTimeMillis())
                val email = firebaseUser?.email ?: fallbackEmail ?: ""
                var user = repository.getUser(uid)
                if (user == null && email.isNotBlank()) {
                    user = repository.getUserByEmail(email)
                }
                _authLoading.value = false
                if (user != null) {
                    if (!user.isActive) {
                        _authError.value = "This account has been deactivated. Please contact support."
                        return@launch
                    }
                    _currentUser.value = user
                    observeCurrentUser(user.id)
                    val targetScreen = getRoleHomeDestination(user.role)
                    showFeedback("Welcome back, ${user.fullName} (${user.role})!")
                    _currentScreen.value = targetScreen
                } else {
                    val displayName = firebaseUser?.displayName?.ifBlank { fallbackName } 
                        ?: fallbackName?.ifBlank { "Google Member" } ?: "Google Member"
                    val newUser = UserEntity(
                        id = uid,
                        email = email,
                        fullName = displayName,
                        role = "USER"
                    )
                    repository.insertUser(newUser)
                    _currentUser.value = newUser
                    observeCurrentUser(newUser.id)
                    showFeedback("Welcome to Smart Health, ${newUser.fullName}!")
                    _currentScreen.value = "dashboard"
                }
            } else {
                _authLoading.value = false
                _authError.value = authResult.exceptionOrNull()?.localizedMessage ?: "Google Sign-In failed."
            }
        }
    }

    fun quickSwitchUser(userId: String) {
        viewModelScope.launch {
            val user = repository.getUser(userId)
            if (user != null) {
                _currentUser.value = user
                observeCurrentUser(user.id)
                val targetScreen = getRoleHomeDestination(user.role)
                showFeedback("Switched to ${user.role} mode: ${user.fullName}")
                _currentScreen.value = targetScreen
            }
        }
    }

    fun register(
        fullName: String,
        email: String,
        pass: String,
        confirmPass: String,
        phone: String,
        dob: String,
        gender: String,
        bloodGroup: String,
        address: String
    ) {
        if (fullName.isBlank() || email.isBlank() || pass.isBlank()) {
            _authError.value = "Please fill in all mandatory fields."
            return
        }
        if (pass != confirmPass) {
            _authError.value = "Passwords do not match."
            return
        }
        if (pass.length < 6) {
            _authError.value = "Password must be at least 6 characters."
            return
        }
        _authLoading.value = true
        _authError.value = null
        viewModelScope.launch {
            val trimmedEmail = email.trim()
            val existing = repository.getUserByEmail(trimmedEmail)
            if (existing != null) {
                _authLoading.value = false
                _authError.value = "An account with this email already exists."
                return@launch
            }

            // Register with Firebase Auth
            val authResult = repository.registerWithFirebase(trimmedEmail, pass)
            if (authResult.isFailure) {
                _authLoading.value = false
                _authError.value = authResult.exceptionOrNull()?.localizedMessage ?: "Registration failed."
                return@launch
            }

            val firebaseUser = authResult.getOrNull()
            val firebaseUid = firebaseUser?.uid ?: ("user_" + System.currentTimeMillis())

            val newUser = UserEntity(
                id = firebaseUid,
                email = trimmedEmail,
                fullName = fullName.trim(),
                phone = phone.trim(),
                dob = dob,
                gender = gender,
                bloodGroup = bloodGroup,
                address = address.trim(),
                role = "USER",
                isVerifiedDoctor = false
            )
            repository.insertUser(newUser)

            _authLoading.value = false
            _currentUser.value = newUser
            observeCurrentUser(newUser.id)
            showFeedback("Account registered successfully! Welcome to Smart Health.")
            _currentScreen.value = getRoleHomeDestination(newUser.role)
        }
    }

    fun forgotPassword(email: String) {
        if (email.isBlank() || !email.contains("@")) {
            _authError.value = "Please enter a valid email address."
            return
        }
        viewModelScope.launch {
            repository.sendPasswordResetEmail(email.trim())
            showFeedback("Password reset link sent to $email. Please check your inbox.")
        }
    }

    fun logout() {
        currentUserJob?.cancel()
        currentUserJob = null
        repository.stopUserSync()
        repository.logoutFirebaseAuth()
        _currentUser.value = null
        showFeedback("Logged out successfully.")
    }

    fun updateProfile(user: UserEntity) {
        viewModelScope.launch {
            repository.updateUser(user)
            _currentUser.value = user
            showFeedback("Profile updated successfully.")
        }
    }

    // Health Records
    val healthRecords: StateFlow<List<HealthRecordEntity>> = _currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList()) else repository.getHealthRecords(user.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestHealthRecord: StateFlow<HealthRecordEntity?> = _currentUser.flatMapLatest { user ->
        if (user == null) flowOf(null) else repository.getLatestHealthRecord(user.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun addHealthRecord(
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
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val record = HealthRecordEntity(
                userId = user.id,
                dateString = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
                weightKg = weightKg,
                heightCm = heightCm,
                systolic = systolic,
                diastolic = diastolic,
                heartRate = heartRate,
                bloodSugarMg = bloodSugarMg,
                temperatureC = temperatureC,
                waterMl = waterMl,
                sleepHours = sleepHours,
                steps = steps,
                exerciseMinutes = exerciseMinutes,
                mood = mood,
                notes = notes
            )
            repository.insertHealthRecord(record)
            showFeedback("Health record saved! BMI calculated: ${String.format("%.1f", record.bmi)} (${record.bmiCategory})")
        }
    }

    fun quickAddWater(amountMl: Int = 250) {
        val user = _currentUser.value ?: return
        val currentLatest = latestHealthRecord.value
        viewModelScope.launch {
            if (currentLatest != null) {
                val updated = currentLatest.copy(waterMl = currentLatest.waterMl + amountMl)
                repository.updateHealthRecord(updated)
            } else {
                repository.insertHealthRecord(
                    HealthRecordEntity(
                        userId = user.id,
                        dateString = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
                        waterMl = amountMl
                    )
                )
            }
            showFeedback("Logged +$amountMl ml water. Stay hydrated!")
        }
    }

    fun deleteHealthRecord(record: HealthRecordEntity) {
        viewModelScope.launch {
            repository.deleteHealthRecord(record)
            showFeedback("Health record removed.")
        }
    }

    // Medicines
    val medicines: StateFlow<List<MedicineEntity>> = _currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList()) else repository.getMedicines(user.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addMedicine(
        name: String,
        dosage: String,
        frequency: String,
        timeOfDay: String,
        startDate: String,
        endDate: String,
        instructions: String,
        notes: String
    ) {
        val user = _currentUser.value ?: return
        if (name.isBlank()) {
            showFeedback("Please specify medicine name.")
            return
        }
        viewModelScope.launch {
            repository.insertMedicine(
                MedicineEntity(
                    userId = user.id,
                    name = name.trim(),
                    dosage = dosage.trim(),
                    frequency = frequency,
                    timeOfDay = timeOfDay,
                    startDate = startDate,
                    endDate = endDate,
                    instructions = instructions,
                    notes = notes
                )
            )
            showFeedback("Medicine reminder scheduled for $name.")
        }
    }

    fun toggleMedicineTaken(medicine: MedicineEntity) {
        viewModelScope.launch {
            val updated = medicine.copy(
                isTakenToday = !medicine.isTakenToday,
                lastTakenTimestamp = if (!medicine.isTakenToday) System.currentTimeMillis() else medicine.lastTakenTimestamp
            )
            repository.updateMedicine(updated)
            showFeedback(if (updated.isTakenToday) "Marked ${medicine.name} as TAKEN today! Great job." else "Marked ${medicine.name} as pending.")
        }
    }

    fun deleteMedicine(medicine: MedicineEntity) {
        viewModelScope.launch {
            repository.deleteMedicine(medicine)
            showFeedback("Medicine reminder deleted.")
        }
    }

    // Vaccinations
    val vaccinations: StateFlow<List<VaccinationEntity>> = _currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList()) else repository.getVaccinations(user.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addVaccination(
        name: String,
        doseNumber: String,
        dateReceived: String,
        nextDoseDate: String,
        doctorHospital: String,
        status: String,
        notes: String
    ) {
        val user = _currentUser.value ?: return
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insertVaccination(
                VaccinationEntity(
                    userId = user.id,
                    vaccineName = name.trim(),
                    doseNumber = doseNumber,
                    dateReceived = dateReceived,
                    nextDoseDate = nextDoseDate,
                    doctorOrHospital = doctorHospital.trim(),
                    status = status,
                    notes = notes
                )
            )
            showFeedback("Vaccination record for $name saved.")
        }
    }

    fun deleteVaccination(v: VaccinationEntity) {
        viewModelScope.launch {
            repository.deleteVaccination(v)
            showFeedback("Vaccination record deleted.")
        }
    }

    // Medical Records
    val medicalRecords: StateFlow<List<MedicalRecordEntity>> = _currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList()) else repository.getMedicalRecords(user.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addMedicalRecord(
        title: String,
        doctorName: String,
        hospitalClinic: String,
        recordDate: String,
        category: String,
        notes: String,
        prescriptionText: String,
        labResults: String,
        fileName: String
    ) {
        val user = _currentUser.value ?: return
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.insertMedicalRecord(
                MedicalRecordEntity(
                    userId = user.id,
                    title = title.trim(),
                    doctorName = doctorName.trim(),
                    hospitalClinic = hospitalClinic.trim(),
                    recordDate = recordDate,
                    category = category,
                    notes = notes,
                    prescriptionText = prescriptionText,
                    labResults = labResults,
                    fileAttachmentName = fileName.ifBlank { "report_attachment.pdf" }
                )
            )
            showFeedback("Medical document saved securely to your personal record.")
        }
    }

    fun deleteMedicalRecord(record: MedicalRecordEntity) {
        viewModelScope.launch {
            repository.deleteMedicalRecord(record)
            showFeedback("Medical record deleted.")
        }
    }

    // Doctors & Appointments
    val doctors: StateFlow<List<DoctorEntity>> = repository.getAllDoctors()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val patientAppointments: StateFlow<List<AppointmentEntity>> = _currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList()) else repository.getAppointmentsForPatient(user.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val doctorAppointments: StateFlow<List<AppointmentEntity>> = _currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList()) else repository.getAppointmentsForDoctor(user.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAppointmentsAdmin: StateFlow<List<AppointmentEntity>> = repository.getAllAppointmentsAdmin()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun bookAppointment(
        doctor: DoctorEntity,
        date: String,
        timeSlot: String,
        symptoms: String,
        notes: String
    ) {
        val user = _currentUser.value ?: return
        if (date.isBlank() || timeSlot.isBlank()) {
            showFeedback("Please select appointment date and time.")
            return
        }
        viewModelScope.launch {
            repository.insertAppointment(
                AppointmentEntity(
                    patientId = user.id,
                    patientName = user.fullName,
                    patientPhone = user.phone,
                    doctorId = doctor.id,
                    doctorName = doctor.name,
                    doctorSpecialty = doctor.specialty,
                    date = date,
                    timeSlot = timeSlot,
                    symptoms = symptoms,
                    notes = notes,
                    status = "PENDING"
                )
            )
            repository.insertNotification(
                NotificationEntity(
                    userId = user.id,
                    title = "Appointment Requested",
                    message = "Your consultation request with ${doctor.name} for $date at $timeSlot is pending confirmation.",
                    type = "APPOINTMENT"
                )
            )
            showFeedback("Appointment request submitted to ${doctor.name}!")
        }
    }

    fun updateAppointmentStatus(appointment: AppointmentEntity, newStatus: String, prescription: String = "", notes: String = "") {
        viewModelScope.launch {
            val updated = appointment.copy(
                status = newStatus,
                doctorPrescription = prescription.ifBlank { appointment.doctorPrescription },
                consultationNotes = notes.ifBlank { appointment.consultationNotes }
            )
            repository.updateAppointment(updated)
            repository.insertNotification(
                NotificationEntity(
                    userId = appointment.patientId,
                    title = "Appointment $newStatus",
                    message = "Your appointment with ${appointment.doctorName} on ${appointment.date} is now $newStatus.",
                    type = "APPOINTMENT"
                )
            )
            showFeedback("Appointment status updated to $newStatus.")
        }
    }

    // Community
    val communityPosts: StateFlow<List<CommunityPostEntity>> = repository.getCommunityPosts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val communityPostsAdmin: StateFlow<List<CommunityPostEntity>> = repository.getCommunityPostsAdmin()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createCommunityPost(title: String, content: String, category: String) {
        val user = _currentUser.value ?: return
        if (title.isBlank() || content.isBlank()) return
        viewModelScope.launch {
            repository.insertPost(
                CommunityPostEntity(
                    authorId = user.id,
                    authorName = user.fullName,
                    authorRole = user.role,
                    title = title.trim(),
                    content = content.trim(),
                    category = category
                )
            )
            showFeedback("Post published to Smart Health Community!")
        }
    }

    fun toggleLikePost(post: CommunityPostEntity) {
        viewModelScope.launch {
            val updated = post.copy(
                isLikedByMe = !post.isLikedByMe,
                likesCount = if (post.isLikedByMe) post.likesCount - 1 else post.likesCount + 1
            )
            repository.updatePost(updated)
        }
    }

    fun deletePost(post: CommunityPostEntity) {
        viewModelScope.launch {
            repository.deletePost(post)
            showFeedback("Post removed.")
        }
    }

    fun reportPost(post: CommunityPostEntity) {
        viewModelScope.launch {
            val updated = post.copy(isReported = true)
            repository.updatePost(updated)
            showFeedback("Post reported to community moderators. Thank you.")
        }
    }

    // Comments for Post
    fun getCommentsForPost(postId: Long) = repository.getComments(postId)

    fun addComment(postId: Long, content: String) {
        val user = _currentUser.value ?: return
        if (content.isBlank()) return
        viewModelScope.launch {
            repository.insertComment(
                CommentEntity(
                    postId = postId,
                    authorId = user.id,
                    authorName = user.fullName,
                    authorRole = user.role,
                    content = content.trim()
                )
            )
            showFeedback("Comment added.")
        }
    }

    // Chat
    private val _currentChatChannel = MutableStateFlow("general")
    val currentChatChannel: StateFlow<String> = _currentChatChannel.asStateFlow()

    fun selectChatChannel(channel: String) {
        _currentChatChannel.value = channel
    }

    val chatMessages: StateFlow<List<ChatMessageEntity>> = _currentChatChannel.flatMapLatest { channel ->
        repository.getChatMessages(channel)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun sendChatMessage(message: String) {
        val user = _currentUser.value ?: return
        if (message.isBlank()) return
        viewModelScope.launch {
            repository.sendChatMessage(
                ChatMessageEntity(
                    senderId = user.id,
                    senderName = user.fullName,
                    senderRole = user.role,
                    channelId = _currentChatChannel.value,
                    message = message.trim()
                )
            )
        }
    }

    fun deleteChatMessage(msg: ChatMessageEntity) {
        viewModelScope.launch {
            repository.deleteChatMessage(msg)
        }
    }

    // Gemini AI Health Assistant
    private val _aiMessages = MutableStateFlow(
        listOf(
            AiChatMessage(
                sender = "ai",
                text = "Hello! I am your Smart Health AI Assistant. Ask me anything about health conditions, symptom explanations, healthy habits, or medication guidelines.\n\n⚠️ Disclaimer: I provide health educational guidance and cannot replace clinical diagnosis by a licensed doctor. For emergency symptoms, please call 911 immediately."
            )
        )
    )
    val aiMessages: StateFlow<List<AiChatMessage>> = _aiMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    fun sendAiPrompt(prompt: String) {
        if (prompt.isBlank()) return
        val userMsg = AiChatMessage(sender = "user", text = prompt.trim())
        _aiMessages.value = _aiMessages.value + userMsg
        _isAiThinking.value = true

        viewModelScope.launch {
            val user = _currentUser.value
            val latestVitals = latestHealthRecord.value
            val meds = medicines.value.filter { it.isActive }
            val (reply, isEmergency) = repository.askHealthAi(
                prompt = prompt,
                userProfile = user,
                latestRecord = latestVitals,
                activeMeds = meds
            )
            _isAiThinking.value = false
            val aiMsg = AiChatMessage(sender = "ai", text = reply, isEmergency = isEmergency)
            _aiMessages.value = _aiMessages.value + aiMsg
        }
    }

    fun clearAiChat() {
        _aiMessages.value = listOf(
            AiChatMessage(
                sender = "ai",
                text = "Chat cleared! How can I assist you with your health today?"
            )
        )
    }

    // Gemini Diet AI
    private val _dietPlanResult = MutableStateFlow<DietPlanResult?>(null)
    val dietPlanResult: StateFlow<DietPlanResult?> = _dietPlanResult.asStateFlow()

    private val _isDietAiGenerating = MutableStateFlow(false)
    val isDietAiGenerating: StateFlow<Boolean> = _isDietAiGenerating.asStateFlow()

    fun generateDietPlan(
        age: Int,
        gender: String,
        heightCm: Float,
        weightKg: Float,
        goal: String,
        activityLevel: String,
        dietPreference: String,
        allergies: String,
        medicalNotes: String
    ) {
        _isDietAiGenerating.value = true
        viewModelScope.launch {
            val plan = repository.generateDietPlan(
                age, gender, heightCm, weightKg, goal, activityLevel, dietPreference, allergies, medicalNotes
            )
            _dietPlanResult.value = plan
            _isDietAiGenerating.value = false
            showFeedback("Personalized Diet & Nutrition Plan generated successfully!")
        }
    }

    // Gemini Document Analyzer & Prescription Reader
    private val _documentAnalysisResult = MutableStateFlow<DocumentAnalysisResult?>(null)
    val documentAnalysisResult: StateFlow<DocumentAnalysisResult?> = _documentAnalysisResult.asStateFlow()

    private val _isAnalyzingDocument = MutableStateFlow(false)
    val isAnalyzingDocument: StateFlow<Boolean> = _isAnalyzingDocument.asStateFlow()

    fun analyzeMedicalDocument(
        documentText: String,
        documentType: String = "PRESCRIPTION",
        base64Image: String? = null
    ) {
        _isAnalyzingDocument.value = true
        viewModelScope.launch {
            val result = repository.analyzeMedicalDocument(documentText, documentType, base64Image)
            _documentAnalysisResult.value = result
            _isAnalyzingDocument.value = false
            showFeedback("Document analysis completed! Review findings below.")
        }
    }

    fun clearDocumentAnalysis() {
        _documentAnalysisResult.value = null
    }

    fun saveAnalysisToMedicalRecords(result: DocumentAnalysisResult) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val rxSummary = result.medicines.joinToString("\n") {
                "• ${it.name} (${it.dosage}, ${it.dosageForm}) - ${it.frequency} for ${it.duration}. Instructions: ${it.instructions}"
            }
            val labSummary = result.labItems.joinToString("\n") {
                "• ${it.testName}: ${it.resultValue} ${it.unit} [Range: ${it.referenceRange}] -> Status: ${it.status} (${it.explanation})"
            }

            val notes = buildString {
                append(result.clinicalSummary)
                if (result.keyWarnings.isNotBlank()) {
                    append("\n\n⚠️ Warnings: ")
                    append(result.keyWarnings)
                }
            }

            repository.insertMedicalRecord(
                MedicalRecordEntity(
                    userId = user.id,
                    title = result.documentTitle,
                    doctorName = result.doctorOrLabName.ifBlank { "Analyzed Healthcare Center" },
                    hospitalClinic = "Smart Health Diagnostic Suite",
                    recordDate = result.dateDetected.ifBlank {
                        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                    },
                    category = if (result.type == "PRESCRIPTION") "Prescription" else "Lab Report",
                    notes = notes,
                    prescriptionText = rxSummary,
                    labResults = labSummary,
                    fileAttachmentName = if (result.type == "PRESCRIPTION") "prescription_scan.pdf" else "diagnostic_lab_report.pdf"
                )
            )
            showFeedback("Document saved securely to your Electronic Health Records (EHR).")
        }
    }

    fun importExtractedMedicinesToSchedule(medicines: List<ExtractedMedicine>) {
        val user = _currentUser.value ?: return
        if (medicines.isEmpty()) return
        viewModelScope.launch {
            val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
            medicines.forEach { med ->
                val time = when {
                    med.frequency.contains("Twice", ignoreCase = true) -> "08:00 AM & 08:00 PM"
                    med.frequency.contains("Three", ignoreCase = true) -> "08:00 AM, 01:00 PM & 08:00 PM"
                    med.frequency.contains("Bedtime", ignoreCase = true) -> "09:00 PM"
                    else -> "08:00 AM"
                }
                repository.insertMedicine(
                    MedicineEntity(
                        userId = user.id,
                        name = med.name,
                        dosage = med.dosage,
                        frequency = med.frequency,
                        timeOfDay = time,
                        startDate = today,
                        endDate = "2026-08-30",
                        instructions = med.instructions,
                        notes = med.warnings
                    )
                )
            }
            showFeedback("Imported ${medicines.size} medications directly into your Daily Schedule!")
        }
    }


    // Blood Donation & Requests
    val bloodDonors: StateFlow<List<BloodDonorEntity>> = repository.getBloodDonors()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bloodRequests: StateFlow<List<BloodRequestEntity>> = repository.getBloodRequests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun registerAsBloodDonor(bloodGroup: String, phone: String, location: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.insertBloodDonor(
                BloodDonorEntity(
                    userId = user.id,
                    name = user.fullName,
                    bloodGroup = bloodGroup,
                    phone = phone.ifBlank { user.phone },
                    location = location.ifBlank { user.address },
                    isAvailable = true,
                    lastDonationDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                )
            )
            showFeedback("Registered as an active blood donor! Thank you for saving lives.")
        }
    }

    fun toggleDonorAvailability(donor: BloodDonorEntity) {
        viewModelScope.launch {
            val updated = donor.copy(isAvailable = !donor.isAvailable)
            repository.updateBloodDonor(updated)
            showFeedback(if (updated.isAvailable) "You are now marked as AVAILABLE for blood donation." else "Marked as temporarily unavailable.")
        }
    }

    fun createBloodRequest(
        patientName: String,
        bloodGroup: String,
        unitsRequired: Int,
        hospital: String,
        location: String,
        contactPhone: String,
        requiredDate: String,
        urgency: String,
        description: String
    ) {
        val user = _currentUser.value ?: return
        if (patientName.isBlank() || contactPhone.isBlank()) return
        viewModelScope.launch {
            repository.insertBloodRequest(
                BloodRequestEntity(
                    requesterId = user.id,
                    patientName = patientName.trim(),
                    bloodGroup = bloodGroup,
                    unitsRequired = unitsRequired,
                    hospital = hospital.trim(),
                    location = location.trim(),
                    contactPhone = contactPhone.trim(),
                    requiredDate = requiredDate,
                    urgency = urgency,
                    status = "ACTIVE",
                    description = description.trim()
                )
            )
            showFeedback("Urgent blood request broadcasted to the community.")
        }
    }

    fun updateBloodRequestStatus(request: BloodRequestEntity, newStatus: String) {
        viewModelScope.launch {
            val updated = request.copy(status = newStatus)
            repository.updateBloodRequest(updated)
            showFeedback("Blood request marked as $newStatus.")
        }
    }

    // Articles
    val articles: StateFlow<List<ArticleEntity>> = repository.getArticles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allArticlesAdmin: StateFlow<List<ArticleEntity>> = repository.getAllArticlesAdmin()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createArticle(title: String, excerpt: String, content: String, category: String, author: String) {
        if (title.isBlank() || content.isBlank()) return
        viewModelScope.launch {
            repository.insertArticle(
                ArticleEntity(
                    title = title.trim(),
                    excerpt = excerpt.trim(),
                    content = content.trim(),
                    category = category,
                    author = author.ifBlank { "Smart Health Editorial" },
                    date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                )
            )
            showFeedback("Health article published.")
        }
    }

    fun deleteArticle(article: ArticleEntity) {
        viewModelScope.launch {
            repository.deleteArticle(article)
            showFeedback("Article deleted.")
        }
    }

    // Emergency Services
    val emergencyServices: StateFlow<List<EmergencyServiceEntity>> = repository.getEmergencyServices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addEmergencyService(name: String, type: String, phone: String, address: String, description: String) {
        if (name.isBlank() || phone.isBlank()) return
        viewModelScope.launch {
            repository.insertEmergencyService(
                EmergencyServiceEntity(
                    name = name.trim(),
                    type = type,
                    phone = phone.trim(),
                    address = address.trim(),
                    description = description.trim()
                )
            )
            showFeedback("Emergency center added.")
        }
    }

    fun deleteEmergencyService(s: EmergencyServiceEntity) {
        viewModelScope.launch {
            repository.deleteEmergencyService(s)
            showFeedback("Emergency service removed.")
        }
    }

    // Notifications
    val notifications: StateFlow<List<NotificationEntity>> = _currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList()) else repository.getNotifications(user.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun markNotificationRead(n: NotificationEntity) {
        viewModelScope.launch {
            repository.updateNotification(n.copy(isRead = true))
        }
    }

    fun clearAllNotifications() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.clearNotifications(user.id)
            showFeedback("Notifications cleared.")
        }
    }

    // Admin Suite & Doctor Management
    val allUsers: StateFlow<List<UserEntity>> = repository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMedicalRecordsAdmin: StateFlow<List<MedicalRecordEntity>> = repository.getAllMedicalRecordsAdmin()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateUserRole(user: UserEntity, newRole: String) {
        viewModelScope.launch {
            val updated = user.copy(
                role = newRole,
                isVerifiedDoctor = if (newRole == "DOCTOR") true else (if (newRole == "USER") false else user.isVerifiedDoctor)
            )
            repository.updateUser(updated)
            if (newRole == "DOCTOR") {
                val existing = repository.getDoctor(user.id)
                if (existing == null) {
                    val newDoc = DoctorEntity(
                        id = user.id,
                        name = if (user.fullName.startsWith("Dr.", ignoreCase = true)) user.fullName else "Dr. ${user.fullName}",
                        specialty = "General Physician",
                        qualifications = "MBBS",
                        experienceYears = 5,
                        hospital = "Community Health Clinic",
                        location = user.address.ifBlank { "Metropolitan Health Center" },
                        consultationFee = 50.0,
                        rating = 5.0f,
                        reviewCount = 1,
                        bio = "Licensed medical practitioner dedicated to quality patient care.",
                        phone = user.phone,
                        email = user.email,
                        isVerified = true,
                        availableDays = "Mon - Fri",
                        availableSlots = "09:00 AM, 11:00 AM, 02:00 PM, 04:00 PM"
                    )
                    repository.insertDoctor(newDoc)
                }
            } else if (newRole == "USER") {
                val existing = repository.getDoctor(user.id)
                if (existing != null) {
                    repository.deleteDoctor(existing)
                }
            }
            showFeedback("User ${user.fullName} role updated to $newRole.")
        }
    }

    fun promoteUserToDoctor(
        user: UserEntity,
        specialty: String,
        qualifications: String,
        experienceYears: Int,
        hospital: String,
        location: String,
        fee: Double,
        bio: String
    ) {
        viewModelScope.launch {
            val updatedUser = user.copy(role = "DOCTOR", isVerifiedDoctor = true)
            repository.updateUser(updatedUser)

            val existingDoc = repository.getDoctor(user.id)
            if (existingDoc != null) {
                val updatedDoc = existingDoc.copy(
                    name = if (user.fullName.startsWith("Dr.", ignoreCase = true)) user.fullName else "Dr. ${user.fullName}",
                    specialty = specialty.ifBlank { existingDoc.specialty },
                    qualifications = qualifications.ifBlank { existingDoc.qualifications },
                    experienceYears = experienceYears,
                    hospital = hospital.ifBlank { existingDoc.hospital },
                    location = location.ifBlank { existingDoc.location },
                    consultationFee = fee,
                    bio = bio.ifBlank { existingDoc.bio },
                    isVerified = true
                )
                repository.updateDoctor(updatedDoc)
            } else {
                val newDoc = DoctorEntity(
                    id = user.id,
                    name = if (user.fullName.startsWith("Dr.", ignoreCase = true)) user.fullName else "Dr. ${user.fullName}",
                    specialty = specialty.ifBlank { "General Physician" },
                    qualifications = qualifications.ifBlank { "MBBS, MD" },
                    experienceYears = experienceYears,
                    hospital = hospital.ifBlank { "Community Health Clinic" },
                    location = location.ifBlank { user.address.ifBlank { "Metropolitan Clinic" } },
                    consultationFee = fee,
                    rating = 5.0f,
                    reviewCount = 1,
                    bio = bio.ifBlank { "Licensed medical practitioner dedicated to quality patient care." },
                    phone = user.phone,
                    email = user.email,
                    isVerified = true,
                    availableDays = "Mon - Fri",
                    availableSlots = "09:00 AM, 11:00 AM, 02:00 PM, 04:00 PM"
                )
                repository.insertDoctor(newDoc)
            }
            showFeedback("Doctor profile for ${user.fullName} successfully registered.")
        }
    }

    fun revokeDoctorRole(doctorUser: UserEntity) {
        viewModelScope.launch {
            val updatedUser = doctorUser.copy(role = "USER", isVerifiedDoctor = false)
            repository.updateUser(updatedUser)
            val existingDoc = repository.getDoctor(doctorUser.id)
            if (existingDoc != null) {
                repository.deleteDoctor(existingDoc)
            }
            showFeedback("Doctor role revoked for ${doctorUser.fullName}. Switched to normal user.")
        }
    }

    fun updateDoctorProfile(doctor: DoctorEntity) {
        viewModelScope.launch {
            repository.updateDoctor(doctor)
            showFeedback("Doctor profile for ${doctor.name} updated.")
        }
    }

    fun updateDoctorSchedule(doctorId: String, days: String, slots: String) {
        viewModelScope.launch {
            val existing = repository.getDoctor(doctorId)
            if (existing != null) {
                val updated = existing.copy(availableDays = days.trim(), availableSlots = slots.trim())
                repository.updateDoctor(updated)
                showFeedback("Your consultation schedule was updated successfully!")
            } else {
                val user = _currentUser.value
                if (user != null && user.id == doctorId) {
                    val newDoc = DoctorEntity(
                        id = user.id,
                        name = if (user.fullName.startsWith("Dr.", ignoreCase = true)) user.fullName else "Dr. ${user.fullName}",
                        specialty = "General Physician",
                        qualifications = "MBBS",
                        experienceYears = 5,
                        hospital = "Community Health Clinic",
                        location = user.address.ifBlank { "Clinic" },
                        consultationFee = 50.0,
                        rating = 5.0f,
                        reviewCount = 1,
                        bio = "Licensed medical practitioner.",
                        phone = user.phone,
                        email = user.email,
                        isVerified = true,
                        availableDays = days.trim(),
                        availableSlots = slots.trim()
                    )
                    repository.insertDoctor(newDoc)
                    showFeedback("Your consultation schedule was saved successfully!")
                }
            }
        }
    }

    fun toggleUserActive(user: UserEntity) {
        viewModelScope.launch {
            val updated = user.copy(isActive = !user.isActive)
            repository.updateUser(updated)
            showFeedback("Account ${user.fullName} ${if (updated.isActive) "activated" else "suspended"}.")
        }
    }

    fun verifyDoctor(doctor: DoctorEntity, isVerified: Boolean) {
        viewModelScope.launch {
            val updated = doctor.copy(isVerified = isVerified)
            repository.updateDoctor(updated)
            showFeedback("Doctor ${doctor.name} verification status: ${if (isVerified) "VERIFIED" else "UNVERIFIED"}.")
        }
    }
}

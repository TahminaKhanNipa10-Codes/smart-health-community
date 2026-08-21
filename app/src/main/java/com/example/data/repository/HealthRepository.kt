package com.example.data.repository

import com.example.data.ai.GeminiAiService
import com.example.data.firebase.FirebaseAuthService
import com.example.data.firebase.FirestoreService
import com.example.data.local.HealthDao
import com.example.data.model.*
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class HealthRepository(
    private val dao: HealthDao,
    private val aiService: GeminiAiService,
    private val firestoreService: FirestoreService = FirestoreService(),
    private val authService: FirebaseAuthService = FirebaseAuthService()
) {
    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedInitialDataIfNeeded()
            startFirestoreSync()
        }
    }

    private fun startFirestoreSync() {
        val scope = CoroutineScope(Dispatchers.IO)
        
        // Continuous Real-Time Users Sync: Firestore -> Room
        scope.launch {
            try {
                firestoreService.getAllUsersFlow().collect { users ->
                    if (users.isNotEmpty()) {
                        dao.insertUsers(users)
                    }
                }
            } catch (e: Exception) {
                // Offline fallback
            }
        }

        // Continuous Real-Time Doctors Sync: Firestore -> Room
        scope.launch {
            try {
                firestoreService.getAllDoctorsFlow().collect { doctors ->
                    if (doctors.isNotEmpty()) {
                        dao.insertDoctors(doctors)
                    }
                }
            } catch (e: Exception) {
                // Offline fallback
            }
        }

        // Continuous Real-Time Community Posts Sync: Firestore -> Room
        scope.launch {
            try {
                firestoreService.getCommunityPostsFlow().collect { posts ->
                    if (posts.isNotEmpty()) {
                        dao.insertPosts(posts)
                    }
                }
            } catch (e: Exception) {
                // Offline fallback
            }
        }

        // Continuous Real-Time Articles Sync: Firestore -> Room
        scope.launch {
            try {
                firestoreService.getAllArticlesAdminFlow().collect { articles ->
                    if (articles.isNotEmpty()) {
                        dao.insertArticles(articles)
                    }
                }
            } catch (e: Exception) {
                // Offline fallback
            }
        }

        // Continuous Real-Time Blood Donors Sync: Firestore -> Room
        scope.launch {
            try {
                firestoreService.getAllBloodDonorsFlow().collect { donors ->
                    if (donors.isNotEmpty()) {
                        dao.insertBloodDonors(donors)
                    }
                }
            } catch (e: Exception) {
                // Offline fallback
            }
        }

        // Continuous Real-Time Blood Requests Sync: Firestore -> Room
        scope.launch {
            try {
                firestoreService.getAllBloodRequestsFlow().collect { requests ->
                    if (requests.isNotEmpty()) {
                        dao.insertBloodRequests(requests)
                    }
                }
            } catch (e: Exception) {
                // Offline fallback
            }
        }

        // Continuous Real-Time Appointments Sync: Firestore -> Room
        scope.launch {
            try {
                firestoreService.getAllAppointmentsAdminFlow().collect { appointments ->
                    if (appointments.isNotEmpty()) {
                        appointments.forEach { dao.insertAppointment(it) }
                    }
                }
            } catch (e: Exception) {
                // Offline fallback
            }
        }

        // Continuous Real-Time Emergency Services Sync: Firestore -> Room
        scope.launch {
            try {
                firestoreService.getEmergencyServicesFlow().collect { services ->
                    if (services.isNotEmpty()) {
                        dao.insertEmergencyServices(services)
                    }
                }
            } catch (e: Exception) {
                // Offline fallback
            }
        }
    }

    private var userSyncJob: kotlinx.coroutines.Job? = null

    fun startUserSync(userId: String) {
        if (userId.isBlank()) return
        userSyncJob?.cancel()
        val scope = CoroutineScope(Dispatchers.IO)
        userSyncJob = scope.launch {
            // Real-Time Health Records Sync: Firestore -> Room
            launch {
                try {
                    firestoreService.getHealthRecordsFlow(userId).collect { records ->
                        if (records.isNotEmpty()) {
                            dao.insertHealthRecords(records)
                        }
                    }
                } catch (e: Exception) {
                    // Offline fallback
                }
            }

            // Real-Time Medicines Sync: Firestore -> Room
            launch {
                try {
                    firestoreService.getMedicinesFlow(userId).collect { medicines ->
                        if (medicines.isNotEmpty()) {
                            dao.insertMedicines(medicines)
                        }
                    }
                } catch (e: Exception) {
                    // Offline fallback
                }
            }

            // Real-Time Vaccinations Sync: Firestore -> Room
            launch {
                try {
                    firestoreService.getVaccinationsFlow(userId).collect { vaccinations ->
                        if (vaccinations.isNotEmpty()) {
                            dao.insertVaccinations(vaccinations)
                        }
                    }
                } catch (e: Exception) {
                    // Offline fallback
                }
            }

            // Real-Time Medical Records Sync: Firestore -> Room
            launch {
                try {
                    firestoreService.getMedicalRecordsFlow(userId).collect { medicalRecords ->
                        if (medicalRecords.isNotEmpty()) {
                            dao.insertMedicalRecords(medicalRecords)
                        }
                    }
                } catch (e: Exception) {
                    // Offline fallback
                }
            }

            // Real-Time Notifications Sync: Firestore -> Room
            launch {
                try {
                    firestoreService.getNotificationsFlow(userId).collect { notifications ->
                        if (notifications.isNotEmpty()) {
                            dao.insertNotifications(notifications)
                        }
                    }
                } catch (e: Exception) {
                    // Offline fallback
                }
            }
        }
    }

    fun stopUserSync() {
        userSyncJob?.cancel()
        userSyncJob = null
    }

    // ==========================================
    // AUTHENTICATION (Firebase Auth + Firestore)
    // ==========================================
    suspend fun registerWithFirebase(
        email: String,
        password: String
    ): Result<FirebaseUser> {
        return authService.registerUser(email, password)
    }

    suspend fun loginWithFirebase(
        email: String,
        password: String
    ): Result<FirebaseUser> {
        return authService.loginUser(email, password)
    }

    suspend fun signInWithGoogleCredential(idToken: String): Result<FirebaseUser> {
        return authService.signInWithGoogleCredential(idToken)
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return authService.sendPasswordReset(email)
    }

    fun logoutFirebaseAuth() {
        authService.logout()
    }

    fun getCurrentFirebaseUser(): FirebaseUser? {
        return authService.currentFirebaseUser
    }

    // ==========================================
    // USERS & PROFILES (users/{uid})
    // ==========================================
    suspend fun getUser(id: String): UserEntity? {
        return try {
            val remoteUser = firestoreService.getUser(id)
            if (remoteUser != null) {
                dao.insertUser(remoteUser)
                remoteUser
            } else {
                dao.getUserById(id)
            }
        } catch (e: Exception) {
            dao.getUserById(id)
        }
    }

    suspend fun getUserByEmail(email: String): UserEntity? {
        return try {
            val remoteUser = firestoreService.getUserByEmail(email)
            if (remoteUser != null) {
                dao.insertUser(remoteUser)
                remoteUser
            } else {
                dao.getUserByEmail(email)
            }
        } catch (e: Exception) {
            dao.getUserByEmail(email)
        }
    }

    fun getUserFlow(id: String): Flow<UserEntity?> {
        return dao.getUserFlow(id)
    }

    fun getAllUsers(): Flow<List<UserEntity>> {
        return dao.getAllUsers()
    }

    suspend fun insertUser(user: UserEntity) {
        dao.insertUser(user)
        try {
            firestoreService.saveUser(user)
        } catch (e: Exception) {
            // Offline/unauthenticated fallback
        }
    }

    suspend fun updateUser(user: UserEntity) {
        dao.updateUser(user)
        try {
            firestoreService.saveUser(user)
        } catch (e: Exception) {
            // Offline/unauthenticated fallback
        }
    }

    suspend fun deleteUser(user: UserEntity) {
        dao.deleteUser(user)
        try {
            firestoreService.deleteUser(user.id)
        } catch (e: Exception) {
            // Offline/unauthenticated fallback
        }
    }

    // ==========================================
    // HEALTH VITALS (healthRecords/{recordId})
    // ==========================================
    fun getHealthRecords(userId: String): Flow<List<HealthRecordEntity>> {
        return dao.getHealthRecords(userId)
    }

    fun getLatestHealthRecord(userId: String): Flow<HealthRecordEntity?> {
        return dao.getLatestHealthRecord(userId)
    }

    suspend fun insertHealthRecord(record: HealthRecordEntity) {
        dao.insertHealthRecord(record)
        try {
            firestoreService.saveHealthRecord(record)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    suspend fun updateHealthRecord(record: HealthRecordEntity) {
        dao.updateHealthRecord(record)
        try {
            firestoreService.saveHealthRecord(record)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    suspend fun deleteHealthRecord(record: HealthRecordEntity) {
        dao.deleteHealthRecord(record)
        try {
            firestoreService.deleteHealthRecord(record.id)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    // ==========================================
    // MEDICINES (medicines/{medicineId})
    // ==========================================
    fun getMedicines(userId: String): Flow<List<MedicineEntity>> {
        return dao.getMedicines(userId)
    }

    fun getActiveMedicines(userId: String): Flow<List<MedicineEntity>> {
        return dao.getActiveMedicines(userId)
    }

    suspend fun insertMedicine(medicine: MedicineEntity) {
        dao.insertMedicine(medicine)
        try {
            firestoreService.saveMedicine(medicine)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    suspend fun updateMedicine(medicine: MedicineEntity) {
        dao.updateMedicine(medicine)
        try {
            firestoreService.saveMedicine(medicine)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    suspend fun deleteMedicine(medicine: MedicineEntity) {
        dao.deleteMedicine(medicine)
        try {
            firestoreService.deleteMedicine(medicine.id)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    // ==========================================
    // VACCINATIONS (vaccinations/{vaccinationId})
    // ==========================================
    fun getVaccinations(userId: String): Flow<List<VaccinationEntity>> {
        return dao.getVaccinations(userId)
    }

    suspend fun insertVaccination(v: VaccinationEntity) {
        dao.insertVaccination(v)
        try {
            firestoreService.saveVaccination(v)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    suspend fun updateVaccination(v: VaccinationEntity) {
        dao.updateVaccination(v)
        try {
            firestoreService.saveVaccination(v)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    suspend fun deleteVaccination(v: VaccinationEntity) {
        dao.deleteVaccination(v)
        try {
            firestoreService.deleteVaccination(v.id)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    // ==========================================
    // MEDICAL RECORDS & LAB REPORTS (medicalRecords/{recordId})
    // ==========================================
    fun getMedicalRecords(userId: String): Flow<List<MedicalRecordEntity>> {
        return dao.getMedicalRecords(userId)
    }

    fun getAllMedicalRecordsAdmin(): Flow<List<MedicalRecordEntity>> {
        return dao.getAllMedicalRecordsAdmin()
    }

    suspend fun insertMedicalRecord(r: MedicalRecordEntity) {
        dao.insertMedicalRecord(r)
        try {
            firestoreService.saveMedicalRecord(r)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    suspend fun deleteMedicalRecord(r: MedicalRecordEntity) {
        dao.deleteMedicalRecord(r)
        try {
            firestoreService.deleteMedicalRecord(r.id)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    // ==========================================
    // DOCTORS (doctors/{doctorId})
    // ==========================================
    fun getAllDoctors(): Flow<List<DoctorEntity>> {
        return dao.getAllDoctors()
    }

    suspend fun getDoctor(id: String): DoctorEntity? {
        return try {
            val remote = firestoreService.getDoctor(id)
            if (remote != null) {
                dao.insertDoctor(remote)
                remote
            } else {
                dao.getDoctorById(id)
            }
        } catch (e: Exception) {
            dao.getDoctorById(id)
        }
    }

    suspend fun insertDoctor(d: DoctorEntity) {
        dao.insertDoctor(d)
        try {
            firestoreService.saveDoctor(d)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    suspend fun updateDoctor(d: DoctorEntity) {
        dao.updateDoctor(d)
        try {
            firestoreService.saveDoctor(d)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    suspend fun deleteDoctor(d: DoctorEntity) {
        dao.deleteDoctor(d)
        try {
            firestoreService.deleteDoctor(d.id)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    // ==========================================
    // APPOINTMENTS (appointments/{appointmentId})
    // ==========================================
    fun getAppointmentsForPatient(patientId: String): Flow<List<AppointmentEntity>> {
        return dao.getAppointmentsForPatient(patientId)
    }

    fun getAppointmentsForDoctor(doctorId: String): Flow<List<AppointmentEntity>> {
        return dao.getAppointmentsForDoctor(doctorId)
    }

    fun getAllAppointmentsAdmin(): Flow<List<AppointmentEntity>> {
        return dao.getAllAppointments()
    }

    suspend fun insertAppointment(a: AppointmentEntity) {
        dao.insertAppointment(a)
        try {
            firestoreService.saveAppointment(a)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    suspend fun updateAppointment(a: AppointmentEntity) {
        dao.updateAppointment(a)
        try {
            firestoreService.saveAppointment(a)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    suspend fun deleteAppointment(a: AppointmentEntity) {
        dao.deleteAppointment(a)
        try {
            firestoreService.deleteAppointment(a.id)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    // ==========================================
    // COMMUNITY POSTS & COMMENTS (communityPosts/{postId})
    // ==========================================
    fun getCommunityPosts(): Flow<List<CommunityPostEntity>> {
        return dao.getAllPosts()
    }

    fun getCommunityPostsAdmin(): Flow<List<CommunityPostEntity>> {
        return dao.getAllPostsAdmin()
    }

    suspend fun insertPost(p: CommunityPostEntity) {
        dao.insertPost(p)
        try {
            firestoreService.saveCommunityPost(p)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    suspend fun updatePost(p: CommunityPostEntity) {
        dao.updatePost(p)
        try {
            firestoreService.saveCommunityPost(p)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    suspend fun deletePost(p: CommunityPostEntity) {
        dao.deletePost(p)
        try {
            firestoreService.deleteCommunityPost(p.id)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    fun getComments(postId: Long): Flow<List<CommentEntity>> {
        return dao.getCommentsForPost(postId)
    }

    suspend fun insertComment(c: CommentEntity) {
        dao.insertComment(c)
        try {
            firestoreService.saveComment(c)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    suspend fun deleteComment(c: CommentEntity) {
        dao.deleteComment(c)
        try {
            firestoreService.deleteComment(c)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    // ==========================================
    // CHAT MESSAGES (chatMessages/{messageId})
    // ==========================================
    fun getChatMessages(channelId: String): Flow<List<ChatMessageEntity>> {
        return dao.getChatMessages(channelId)
    }

    suspend fun sendChatMessage(msg: ChatMessageEntity) {
        dao.insertChatMessage(msg)
        try {
            firestoreService.saveChatMessage(msg)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    suspend fun deleteChatMessage(msg: ChatMessageEntity) {
        dao.deleteChatMessage(msg)
        try {
            firestoreService.deleteChatMessage(msg.id)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    // ==========================================
    // BLOOD DONATION & REQUESTS (bloodDonors & bloodRequests)
    // ==========================================
    fun getBloodDonors(): Flow<List<BloodDonorEntity>> {
        return dao.getAllBloodDonors()
    }

    suspend fun insertBloodDonor(donor: BloodDonorEntity) {
        dao.insertBloodDonor(donor)
        try {
            firestoreService.saveBloodDonor(donor)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    suspend fun updateBloodDonor(donor: BloodDonorEntity) {
        dao.updateBloodDonor(donor)
        try {
            firestoreService.saveBloodDonor(donor)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    suspend fun deleteBloodDonor(donor: BloodDonorEntity) {
        dao.deleteBloodDonor(donor)
        try {
            firestoreService.deleteBloodDonor(donor.id)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    fun getBloodRequests(): Flow<List<BloodRequestEntity>> {
        return dao.getAllBloodRequests()
    }

    suspend fun insertBloodRequest(r: BloodRequestEntity) {
        dao.insertBloodRequest(r)
        try {
            firestoreService.saveBloodRequest(r)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    suspend fun updateBloodRequest(r: BloodRequestEntity) {
        dao.updateBloodRequest(r)
        try {
            firestoreService.saveBloodRequest(r)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    suspend fun deleteBloodRequest(r: BloodRequestEntity) {
        dao.deleteBloodRequest(r)
        try {
            firestoreService.deleteBloodRequest(r.id)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    // ==========================================
    // ARTICLES (articles/{articleId})
    // ==========================================
    fun getArticles(): Flow<List<ArticleEntity>> {
        return dao.getPublishedArticles()
    }

    fun getAllArticlesAdmin(): Flow<List<ArticleEntity>> {
        return dao.getAllArticlesAdmin()
    }

    suspend fun insertArticle(a: ArticleEntity) {
        dao.insertArticle(a)
        try {
            firestoreService.saveArticle(a)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    suspend fun updateArticle(a: ArticleEntity) {
        dao.updateArticle(a)
        try {
            firestoreService.saveArticle(a)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    suspend fun deleteArticle(a: ArticleEntity) {
        dao.deleteArticle(a)
        try {
            firestoreService.deleteArticle(a.id)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    // ==========================================
    // NOTIFICATIONS (notifications/{notificationId})
    // ==========================================
    fun getNotifications(userId: String): Flow<List<NotificationEntity>> {
        return dao.getNotifications(userId)
    }

    suspend fun insertNotification(n: NotificationEntity) {
        dao.insertNotification(n)
        try {
            firestoreService.saveNotification(n)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    suspend fun updateNotification(n: NotificationEntity) {
        dao.updateNotification(n)
        try {
            firestoreService.saveNotification(n)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    suspend fun clearNotifications(userId: String) {
        dao.clearNotifications(userId)
        try {
            firestoreService.clearNotifications(userId)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    // ==========================================
    // EMERGENCY SERVICES (emergencyServices/{serviceId})
    // ==========================================
    fun getEmergencyServices(): Flow<List<EmergencyServiceEntity>> {
        return dao.getEmergencyServices()
    }

    suspend fun insertEmergencyService(s: EmergencyServiceEntity) {
        dao.insertEmergencyService(s)
        try {
            firestoreService.saveEmergencyService(s)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    suspend fun updateEmergencyService(s: EmergencyServiceEntity) {
        dao.updateEmergencyService(s)
        try {
            firestoreService.saveEmergencyService(s)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    suspend fun deleteEmergencyService(s: EmergencyServiceEntity) {
        dao.deleteEmergencyService(s)
        try {
            firestoreService.deleteEmergencyService(s.id)
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    // ==========================================
    // AI SERVICES
    // ==========================================
    suspend fun askHealthAi(
        prompt: String,
        userProfile: UserEntity? = null,
        latestRecord: HealthRecordEntity? = null,
        activeMeds: List<MedicineEntity> = emptyList()
    ) = aiService.generateHealthAdvice(prompt, userProfile, latestRecord, activeMeds)

    suspend fun generateDietPlan(
        age: Int,
        gender: String,
        heightCm: Float,
        weightKg: Float,
        goal: String,
        activityLevel: String,
        dietPreference: String,
        allergies: String,
        medicalNotes: String
    ) = aiService.generateDietPlan(age, gender, heightCm, weightKg, goal, activityLevel, dietPreference, allergies, medicalNotes)

    suspend fun analyzeMedicalDocument(
        documentText: String,
        documentType: String = "PRESCRIPTION",
        base64Image: String? = null
    ) = aiService.analyzeMedicalDocument(documentText, documentType, base64Image)

    // ==========================================
    // INITIAL SEED DATA (For Immediate UI Demo)
    // ==========================================
    private suspend fun seedInitialDataIfNeeded() {
        val sampleUser = dao.getUserById("user_default")
        if (sampleUser == null) {
            dao.insertUser(
                UserEntity(
                    id = "user_default",
                    email = "member@smarthealth.org",
                    fullName = "Alex Johnson",
                    phone = "+1 (555) 234-5678",
                    dob = "1994-06-15",
                    gender = "Male",
                    bloodGroup = "O+",
                    address = "742 Evergreen Terrace, Springfield",
                    emergencyContact = "Emma Johnson (Spouse)",
                    emergencyPhone = "+1 (555) 987-6543",
                    medicalNotes = "Mild seasonal pollen allergies; No known drug allergies",
                    role = "USER"
                )
            )
            dao.insertUser(
                UserEntity(
                    id = "doc_sarah",
                    email = "dr.sarah@smarthealth.org",
                    fullName = "Dr. Sarah Mitchell, MD",
                    phone = "+1 (555) 345-6789",
                    dob = "1984-03-22",
                    gender = "Female",
                    bloodGroup = "A+",
                    address = "Metro Health Medical Tower, Suite 400",
                    emergencyContact = "Hospital Administration",
                    emergencyPhone = "+1 (555) 911-0000",
                    medicalNotes = "Board-certified Cardiologist & Internal Medicine Specialist",
                    role = "DOCTOR",
                    isVerifiedDoctor = true
                )
            )
            dao.insertUser(
                UserEntity(
                    id = "admin_master",
                    email = "admin@smarthealth.org",
                    fullName = "System Administrator",
                    phone = "+1 (555) 888-9999",
                    dob = "1990-01-01",
                    gender = "Not Specified",
                    bloodGroup = "AB+",
                    address = "Smart Health HQ, 100 Innovation Way",
                    emergencyContact = "IT Security Ops",
                    emergencyPhone = "+1 (555) 888-0000",
                    medicalNotes = "Platform Super Admin Access",
                    role = "ADMIN"
                )
            )

            // Seed Health Records for Alex Johnson
            dao.insertHealthRecord(
                HealthRecordEntity(
                    userId = "user_default",
                    dateString = "2026-08-15",
                    weightKg = 72.5f,
                    heightCm = 178f,
                    systolic = 118,
                    diastolic = 78,
                    heartRate = 70,
                    bloodSugarMg = 92f,
                    temperatureC = 36.6f,
                    waterMl = 2250,
                    sleepHours = 8.0f,
                    steps = 8420,
                    exerciseMinutes = 45,
                    mood = "Great",
                    notes = "Morning jog completed; resting pulse optimal."
                )
            )
            dao.insertHealthRecord(
                HealthRecordEntity(
                    userId = "user_default",
                    dateString = "2026-08-14",
                    weightKg = 72.8f,
                    heightCm = 178f,
                    systolic = 122,
                    diastolic = 80,
                    heartRate = 74,
                    bloodSugarMg = 98f,
                    temperatureC = 36.7f,
                    waterMl = 1750,
                    sleepHours = 7.0f,
                    steps = 6200,
                    exerciseMinutes = 30,
                    mood = "Good",
                    notes = "Regular workday, feeling energetic."
                )
            )

            // Seed Medicines for Alex Johnson
            dao.insertMedicine(
                MedicineEntity(
                    userId = "user_default",
                    name = "Vitamin D3 + K2",
                    dosage = "2000 IU",
                    frequency = "Once Daily",
                    timeOfDay = "08:00 AM",
                    startDate = "2026-08-01",
                    endDate = "2026-12-31",
                    instructions = "Take with morning breakfast",
                    notes = "Supports bone mineral density and immune balance.",
                    isTakenToday = true,
                    lastTakenTimestamp = System.currentTimeMillis()
                )
            )
            dao.insertMedicine(
                MedicineEntity(
                    userId = "user_default",
                    name = "Omega-3 EPA/DHA",
                    dosage = "1000 mg",
                    frequency = "Once Daily",
                    timeOfDay = "01:00 PM",
                    startDate = "2026-08-01",
                    endDate = "2026-11-30",
                    instructions = "Take after lunch with full glass of water",
                    notes = "Cardiovascular and lipid profile support.",
                    isTakenToday = false
                )
            )
            dao.insertMedicine(
                MedicineEntity(
                    userId = "user_default",
                    name = "Magnesium Glycinate",
                    dosage = "200 mg",
                    frequency = "Once Daily",
                    timeOfDay = "09:00 PM",
                    startDate = "2026-08-01",
                    endDate = "2026-10-31",
                    instructions = "Take 30 mins before bedtime",
                    notes = "Promotes deep sleep cycles and muscle recovery.",
                    isTakenToday = false
                )
            )

            // Seed Vaccinations for Alex Johnson
            dao.insertVaccination(
                VaccinationEntity(
                    userId = "user_default",
                    vaccineName = "Influenza Quadrivalent",
                    doseNumber = "Annual Dose",
                    dateReceived = "2025-10-14",
                    nextDoseDate = "2026-10-15",
                    doctorOrHospital = "Metro Urgent Care & Immunization Clinic",
                    status = "COMPLETED",
                    notes = "No adverse post-injection symptoms."
                )
            )
            dao.insertVaccination(
                VaccinationEntity(
                    userId = "user_default",
                    vaccineName = "Tetanus, Diphtheria, Pertussis (Tdap)",
                    doseNumber = "10-Year Booster",
                    dateReceived = "2022-04-18",
                    nextDoseDate = "2032-04-18",
                    doctorOrHospital = "City Central Health Pavilion",
                    status = "COMPLETED",
                    notes = "Immunity verified for next 10 years."
                )
            )
            dao.insertVaccination(
                VaccinationEntity(
                    userId = "user_default",
                    vaccineName = "Hepatitis B",
                    doseNumber = "Dose 3 of 3",
                    dateReceived = "2020-09-10",
                    nextDoseDate = "N/A (Fully Protected)",
                    doctorOrHospital = "University Medical Center",
                    status = "COMPLETED",
                    notes = "Full antibody titer confirmed."
                )
            )

            // Seed Medical Records for Alex Johnson
            dao.insertMedicalRecord(
                MedicalRecordEntity(
                    userId = "user_default",
                    title = "Annual Comprehensive Health & Metabolic Panel",
                    doctorName = "Dr. Sarah Mitchell, MD",
                    hospitalClinic = "Metro Health Diagnostic Laboratory",
                    recordDate = "2026-07-20",
                    category = "Lab Report",
                    notes = "HbA1c 5.3%, Total Cholesterol 182 mg/dL, Fasting Glucose 91 mg/dL. All metabolic biomarkers are within optimal physiological thresholds.",
                    prescriptionText = "",
                    labResults = "• Fasting Blood Sugar: 91 mg/dL (Normal: 70-99)\n• HbA1c: 5.3% (Normal < 5.7%)\n• Total Cholesterol: 182 mg/dL (< 200)\n• HDL Cholesterol: 56 mg/dL (> 40)\n• LDL Cholesterol: 104 mg/dL (< 100)\n• Triglycerides: 110 mg/dL (< 150)\n• Serum Creatinine: 0.95 mg/dL (0.7-1.3)",
                    fileAttachmentName = "annual_metabolic_panel_2026.pdf"
                )
            )
            dao.insertMedicalRecord(
                MedicalRecordEntity(
                    userId = "user_default",
                    title = "Cardiovascular Stress Test & Resting ECG",
                    doctorName = "Dr. Sarah Mitchell, MD",
                    hospitalClinic = "Metro Cardiology Heart Pavilion",
                    recordDate = "2026-03-12",
                    category = "Diagnostic Test",
                    notes = "Normal Sinus Rhythm at 68 bpm. No ST-segment deviations or ischemic anomalies during treadmill Bruce protocol stage 4.",
                    prescriptionText = "",
                    labResults = "• Resting ECG: Normal Sinus Rhythm (68 bpm)\n• Exercise Duration: 11 mins 40 secs (12.8 METs)\n• Max Heart Rate: 172 bpm (94% target)\n• Blood Pressure Response: Normal hemodynamic curve",
                    fileAttachmentName = "cardiac_stress_test_ecg.pdf"
                )
            )

            // Seed Doctors Catalog
            dao.insertDoctors(
                listOf(
                    DoctorEntity(
                        id = "doc_sarah",
                        name = "Dr. Sarah Mitchell",
                        specialty = "Cardiologist & Internal Medicine",
                        qualifications = "MD, FACC - Harvard Medical School",
                        experienceYears = 14,
                        hospital = "Metro Health Heart & Vascular Institute",
                        location = "Suite 400, 500 Medical Parkway, Springfield",
                        consultationFee = 85.0,
                        rating = 4.9f,
                        reviewCount = 210,
                        bio = "Specializing in preventive cardiology, hypertension optimization, lipid disorders, and non-invasive cardiovascular imaging.",
                        phone = "+1 (555) 345-6789",
                        email = "dr.sarah@smarthealth.org",
                        isVerified = true,
                        availableDays = "Mon, Tue, Wed, Thu, Fri",
                        availableSlots = "09:00 AM, 10:30 AM, 02:00 PM, 03:30 PM"
                    ),
                    DoctorEntity(
                        id = "doc_marcus",
                        name = "Dr. Marcus Vance",
                        specialty = "Endocrinologist & Diabetes Specialist",
                        qualifications = "MD, FACE - Johns Hopkins Medicine",
                        experienceYears = 11,
                        hospital = "Springfield Endocrinology Center",
                        location = "320 Wellness Way, Springfield",
                        consultationFee = 75.0,
                        rating = 4.8f,
                        reviewCount = 168,
                        bio = "Expert care in Type 1 & 2 Diabetes, thyroid nodule management, metabolic syndrome, and hormonal therapies.",
                        phone = "+1 (555) 456-7890",
                        email = "dr.marcus@smarthealth.org",
                        isVerified = true,
                        availableDays = "Mon, Wed, Fri",
                        availableSlots = "10:00 AM, 11:30 AM, 03:00 PM, 04:30 PM"
                    ),
                    DoctorEntity(
                        id = "doc_elena",
                        name = "Dr. Elena Rostova",
                        specialty = "Neurologist & Headache Specialist",
                        qualifications = "MD, PhD - Stanford University",
                        experienceYears = 16,
                        hospital = "Central Brain & Spine Institute",
                        location = "Suite 210, 800 Neuro Plaza, Springfield",
                        consultationFee = 95.0,
                        rating = 4.9f,
                        reviewCount = 195,
                        bio = "Dedicated to migraine management, neuropathic pain, memory disorders, and comprehensive neurological diagnostics.",
                        phone = "+1 (555) 567-8901",
                        email = "dr.elena@smarthealth.org",
                        isVerified = true,
                        availableDays = "Tue, Thu, Sat",
                        availableSlots = "09:30 AM, 11:00 AM, 01:30 PM, 04:00 PM"
                    ),
                    DoctorEntity(
                        id = "doc_david",
                        name = "Dr. David Chen",
                        specialty = "Pediatrician & Family Health",
                        qualifications = "MD, FAAP - Columbia University",
                        experienceYears = 9,
                        hospital = "Children's & Family Wellness Clinic",
                        location = "150 Sunshine Boulevard, Springfield",
                        consultationFee = 60.0,
                        rating = 4.9f,
                        reviewCount = 240,
                        bio = "Compassionate pediatric and adolescent healthcare, developmental tracking, routine vaccinations, and acute illness treatment.",
                        phone = "+1 (555) 678-9012",
                        email = "dr.david@smarthealth.org",
                        isVerified = true,
                        availableDays = "Mon - Sat",
                        availableSlots = "08:30 AM, 10:00 AM, 02:00 PM, 05:00 PM"
                    )
                )
            )

            // Seed Appointments
            dao.insertAppointment(
                AppointmentEntity(
                    patientId = "user_default",
                    patientName = "Alex Johnson",
                    patientPhone = "+1 (555) 234-5678",
                    doctorId = "doc_sarah",
                    doctorName = "Dr. Sarah Mitchell",
                    doctorSpecialty = "Cardiologist & Internal Medicine",
                    date = "2026-08-28",
                    timeSlot = "10:30 AM",
                    symptoms = "Routine semi-annual cardiovascular checkup & blood pressure evaluation.",
                    notes = "Please bring latest lab report results and current supplement log.",
                    status = "CONFIRMED"
                )
            )

            // Seed Community Posts
            dao.insertPost(
                CommunityPostEntity(
                    authorId = "doc_sarah",
                    authorName = "Dr. Sarah Mitchell",
                    authorRole = "DOCTOR",
                    title = "Understanding Blood Pressure: Why Systolic and Diastolic Numbers Both Matter",
                    content = "Many patients ask which number is more important. Systolic (the top number) measures arterial pressure when the heart contracts, while diastolic (bottom number) measures resting pressure between beats.\n\nFor adults under 50, isolated diastolic hypertension is common. After 50, systolic elevation becomes the primary marker for cardiovascular risk. Regular morning and evening monitoring with an upper-arm cuff yields the most accurate baseline.",
                    category = "Wellness Tips",
                    likesCount = 34,
                    commentsCount = 3
                )
            )
            dao.insertPost(
                CommunityPostEntity(
                    authorId = "user_default",
                    authorName = "Alex Johnson",
                    authorRole = "USER",
                    title = "How I lowered my resting heart rate from 84 to 68 bpm in 6 months",
                    content = "Six months ago, my resting heart rate was hovering around 84 bpm due to chronic work stress and poor sleep. By committing to 30 minutes of Zone 2 walking each morning, cutting caffeine after 1 PM, and drinking 2.5L of water daily, my fitness watch now averages 68 bpm resting.\n\nSmall daily habits compound immensely over time!",
                    category = "Experience",
                    likesCount = 28,
                    commentsCount = 2
                )
            )

            // Seed Blood Donors
            dao.insertBloodDonor(
                BloodDonorEntity(
                    userId = "user_default",
                    name = "Alex Johnson",
                    bloodGroup = "O+",
                    phone = "+1 (555) 234-5678",
                    location = "Springfield Central, West District",
                    isAvailable = true,
                    lastDonationDate = "2026-04-10"
                )
            )
            dao.insertBloodDonor(
                BloodDonorEntity(
                    userId = "donor_2",
                    name = "Rachel Adams",
                    bloodGroup = "A-",
                    phone = "+1 (555) 912-3456",
                    location = "North Springfield Medical Park",
                    isAvailable = true,
                    lastDonationDate = "2026-05-18"
                )
            )
            dao.insertBloodDonor(
                BloodDonorEntity(
                    userId = "donor_3",
                    name = "Marcus Wright",
                    bloodGroup = "O-",
                    phone = "+1 (555) 789-0123",
                    location = "Metro Plaza East, Springfield",
                    isAvailable = true,
                    lastDonationDate = "2026-06-01"
                )
            )

            // Seed Blood Requests
            dao.insertBloodRequest(
                BloodRequestEntity(
                    requesterId = "user_default",
                    patientName = "Emily Parker",
                    bloodGroup = "O-",
                    unitsRequired = 2,
                    hospital = "Springfield Regional Trauma Hospital",
                    location = "500 Medical Center Parkway, Springfield",
                    contactPhone = "+1 (555) 443-2211",
                    requiredDate = "2026-08-20",
                    urgency = "CRITICAL",
                    status = "ACTIVE",
                    description = "Urgent requirement for scheduled cardiac surgical procedure. Universal donors O- strongly requested."
                )
            )

            // Seed Articles
            dao.insertArticles(
                listOf(
                    ArticleEntity(
                        title = "Optimizing Sleep Architecture for Metabolic and Cardiovascular Health",
                        excerpt = "Learn how slow-wave deep sleep and REM cycles regulate insulin sensitivity, blood pressure, and cellular repair.",
                        content = "Quality sleep is one of the most potent biological regulators of cardiovascular resilience and metabolic homeostasis.\n\nDuring deep Stage 3 NREM sleep, heart rate drops, vascular resistance relaxes, and blood pressure dips by 10-20% (nocturnal dipping). Disrupted sleep impairs next-day glucose tolerance by up to 30%.\n\nKey Sleep Optimization Protocols:\n1. Maintain a consistent sleep-wake schedule (±30 minutes), even on weekends.\n2. Expose your eyes to natural sunlight within 30 minutes of waking to anchor the circadian clock.\n3. Keep the bedroom temperature between 65°F and 68°F (18-20°C).\n4. Eliminate blue-spectrum light emissions 90 minutes before bedtime.",
                        category = "Sleep Science",
                        author = "Dr. Elena Rostova, MD",
                        readTimeMinutes = 5,
                        date = "2026-08-10",
                        isPublished = true
                    ),
                    ArticleEntity(
                        title = "Preventive Health Screening Timelines: Essential Checkups for Adults",
                        excerpt = "A comprehensive checklist of medical screenings by age group to detect asymptomatic conditions early.",
                        content = "Preventive health screenings are essential because conditions like hypertension, dyslipidemia, and prediabetes develop silently over years without symptoms.\n\nRecommended screening frequencies:\n• Blood Pressure: Annually from age 18\n• Fasting Lipid Panel & HbA1c: Every 1-3 years starting at age 25 (earlier with risk factors)\n• Dermatology Skin Check: Annually for full-body mole mapping\n• Dental Prophylaxis: Every 6 months\n• Comprehensive Eye Exam: Every 2 years to check intraocular pressure and retinal vasculature.",
                        category = "Preventive Care",
                        author = "Dr. Marcus Vance, MD",
                        readTimeMinutes = 6,
                        date = "2026-07-28",
                        isPublished = true
                    ),
                    ArticleEntity(
                        title = "Managing Chronic Stress & Cortisol: The Mind-Body Connection",
                        excerpt = "Discover actionable physiological interventions to down-regulate sympathetic nervous system activation.",
                        content = "Chronic elevation of cortisol and adrenaline promotes systemic low-grade inflammation, insulin resistance, and endothelial dysfunction. Breaking this cycle requires stimulating the vagus nerve to trigger the parasympathetic 'rest and digest' response.\n\nEffective techniques:\n• Physiological Sigh: Two deep nasal inhales followed by one long, slow mouth exhalation (repeating 3 times resets carbon dioxide balance and lowers heart rate in real time)\n• Zone 2 Aerobic Movement: 30 minutes of conversational-pace exercise\n• Daily gratitude journaling and community engagement.",
                        category = "Mental Wellness",
                        author = "Dr. Elena Rostova, MD",
                        readTimeMinutes = 5,
                        date = "2026-07-15",
                        isPublished = true
                    )
                )
            )

            // Seed Emergency Services
            dao.insertEmergencyServices(
                listOf(
                    EmergencyServiceEntity(
                        name = "Springfield Regional Trauma & Emergency Hospital",
                        type = "HOSPITAL",
                        phone = "911",
                        address = "500 Medical Center Parkway, Springfield",
                        distanceKm = 1.4f,
                        is24Hours = true,
                        description = "Level 1 Trauma Center, 24/7 Cardiac & Stroke Emergency Unit, Pediatric ER"
                    ),
                    EmergencyServiceEntity(
                        name = "Metro Emergency Ambulance Dispatch",
                        type = "AMBULANCE",
                        phone = "911",
                        address = "Citywide Fast Response Fleet",
                        distanceKm = 0.8f,
                        is24Hours = true,
                        description = "Advanced Life Support (ALS) Ambulances equipped with defibrillators and paramedic units."
                    ),
                    EmergencyServiceEntity(
                        name = "National Poison Control Emergency Center",
                        type = "HOTLINE",
                        phone = "1-800-222-1222",
                        address = "National 24/7 Hotline",
                        distanceKm = 0.0f,
                        is24Hours = true,
                        description = "Immediate expert guidance for accidental poisoning, toxic ingestion, or chemical exposure."
                    ),
                    EmergencyServiceEntity(
                        name = "National Suicide & Crisis Lifeline",
                        type = "HOTLINE",
                        phone = "988",
                        address = "Confidential 24/7 Support",
                        distanceKm = 0.0f,
                        is24Hours = true,
                        description = "Free and confidential emotional support for people in suicidal crisis or mental health distress."
                    ),
                    EmergencyServiceEntity(
                        name = "St. Jude Emergency Urgent Care & Burn Center",
                        type = "TRAUMA",
                        phone = "+1 (555) 911-4400",
                        address = "820 Health Boulevard, Springfield",
                        distanceKm = 3.2f,
                        is24Hours = true,
                        description = "Specialized 24/7 burn treatment, orthopedics trauma, and acute wound care."
                    )
                )
            )

            // Seed Initial Chat Messages
            dao.insertChatMessage(
                ChatMessageEntity(
                    senderId = "doc_sarah",
                    senderName = "Dr. Sarah Mitchell",
                    senderRole = "DOCTOR",
                    channelId = "general",
                    message = "Welcome everyone to the Smart Health Community! Feel free to ask questions about daily wellness, share your health victories, and connect with fellow community members."
                )
            )
            dao.insertChatMessage(
                ChatMessageEntity(
                    senderId = "user_default",
                    senderName = "Alex Johnson",
                    senderRole = "USER",
                    channelId = "general",
                    message = "Hi Dr. Mitchell! Glad to be here. The health tracker and medicine reminders have already helped me stay consistent with my morning vitamins."
                )
            )

            // Seed Initial Notifications
            dao.insertNotification(
                NotificationEntity(
                    userId = "user_default",
                    title = "Upcoming Doctor Consultation",
                    message = "Your appointment with Dr. Sarah Mitchell is confirmed for Aug 28 at 10:30 AM.",
                    type = "APPOINTMENT"
                )
            )
            dao.insertNotification(
                NotificationEntity(
                    userId = "user_default",
                    title = "Daily Medicine Reminder",
                    message = "Time for your Vitamin D3 + K2 (2000 IU) dose with lunch.",
                    type = "MEDICINE"
                )
            )
            dao.insertNotification(
                NotificationEntity(
                    userId = "user_default",
                    title = "Urgent Blood Request in your area",
                    message = "Critical O- blood needed at St. Jude Trauma Center. Check the Blood tab to help.",
                    type = "BLOOD"
                )
            )
        }
    }
}

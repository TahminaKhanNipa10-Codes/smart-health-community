package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDao {
    // Users
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserFlow(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY fullName ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    // Health Records
    @Query("SELECT * FROM health_records WHERE userId = :userId ORDER BY timestamp DESC")
    fun getHealthRecords(userId: String): Flow<List<HealthRecordEntity>>

    @Query("SELECT * FROM health_records WHERE userId = :userId ORDER BY timestamp DESC LIMIT 1")
    fun getLatestHealthRecord(userId: String): Flow<HealthRecordEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthRecord(record: HealthRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthRecords(records: List<HealthRecordEntity>)

    @Update
    suspend fun updateHealthRecord(record: HealthRecordEntity)

    @Delete
    suspend fun deleteHealthRecord(record: HealthRecordEntity)

    // Medicines
    @Query("SELECT * FROM medicines WHERE userId = :userId ORDER BY id DESC")
    fun getMedicines(userId: String): Flow<List<MedicineEntity>>

    @Query("SELECT * FROM medicines WHERE userId = :userId AND isActive = 1")
    fun getActiveMedicines(userId: String): Flow<List<MedicineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: MedicineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicines(medicines: List<MedicineEntity>)

    @Update
    suspend fun updateMedicine(medicine: MedicineEntity)

    @Delete
    suspend fun deleteMedicine(medicine: MedicineEntity)

    // Vaccinations
    @Query("SELECT * FROM vaccinations WHERE userId = :userId ORDER BY id DESC")
    fun getVaccinations(userId: String): Flow<List<VaccinationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaccination(vaccination: VaccinationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaccinations(vaccinations: List<VaccinationEntity>)

    @Update
    suspend fun updateVaccination(vaccination: VaccinationEntity)

    @Delete
    suspend fun deleteVaccination(vaccination: VaccinationEntity)

    // Medical Records
    @Query("SELECT * FROM medical_records WHERE userId = :userId ORDER BY id DESC")
    fun getMedicalRecords(userId: String): Flow<List<MedicalRecordEntity>>

    @Query("SELECT * FROM medical_records ORDER BY id DESC")
    fun getAllMedicalRecordsAdmin(): Flow<List<MedicalRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicalRecord(record: MedicalRecordEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicalRecords(records: List<MedicalRecordEntity>)

    @Delete
    suspend fun deleteMedicalRecord(record: MedicalRecordEntity)

    // Doctors
    @Query("SELECT * FROM doctors ORDER BY name ASC")
    fun getAllDoctors(): Flow<List<DoctorEntity>>

    @Query("SELECT * FROM doctors WHERE id = :id LIMIT 1")
    suspend fun getDoctorById(id: String): DoctorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctor(doctor: DoctorEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctors(doctors: List<DoctorEntity>)

    @Update
    suspend fun updateDoctor(doctor: DoctorEntity)

    @Delete
    suspend fun deleteDoctor(doctor: DoctorEntity)

    // Appointments
    @Query("SELECT * FROM appointments WHERE patientId = :patientId ORDER BY id DESC")
    fun getAppointmentsForPatient(patientId: String): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE doctorId = :doctorId ORDER BY id DESC")
    fun getAppointmentsForDoctor(doctorId: String): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments ORDER BY id DESC")
    fun getAllAppointments(): Flow<List<AppointmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: AppointmentEntity)

    @Update
    suspend fun updateAppointment(appointment: AppointmentEntity)

    @Delete
    suspend fun deleteAppointment(appointment: AppointmentEntity)

    // Community Posts
    @Query("SELECT * FROM community_posts WHERE isReported = 0 ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<CommunityPostEntity>>

    @Query("SELECT * FROM community_posts ORDER BY timestamp DESC")
    fun getAllPostsAdmin(): Flow<List<CommunityPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: CommunityPostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<CommunityPostEntity>)

    @Update
    suspend fun updatePost(post: CommunityPostEntity)

    @Delete
    suspend fun deletePost(post: CommunityPostEntity)

    // Comments
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY timestamp ASC")
    fun getCommentsForPost(postId: Long): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    @Delete
    suspend fun deleteComment(comment: CommentEntity)

    // Chat Messages
    @Query("SELECT * FROM chat_messages WHERE channelId = :channelId ORDER BY timestamp ASC")
    fun getChatMessages(channelId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity)

    @Delete
    suspend fun deleteChatMessage(message: ChatMessageEntity)

    // Blood Donors
    @Query("SELECT * FROM blood_donors ORDER BY isAvailable DESC, id DESC")
    fun getAllBloodDonors(): Flow<List<BloodDonorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBloodDonor(donor: BloodDonorEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBloodDonors(donors: List<BloodDonorEntity>)

    @Update
    suspend fun updateBloodDonor(donor: BloodDonorEntity)

    @Delete
    suspend fun deleteBloodDonor(donor: BloodDonorEntity)

    // Blood Requests
    @Query("SELECT * FROM blood_requests ORDER BY id DESC")
    fun getAllBloodRequests(): Flow<List<BloodRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBloodRequest(request: BloodRequestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBloodRequests(requests: List<BloodRequestEntity>)

    @Update
    suspend fun updateBloodRequest(request: BloodRequestEntity)

    @Delete
    suspend fun deleteBloodRequest(request: BloodRequestEntity)

    // Articles
    @Query("SELECT * FROM articles WHERE isPublished = 1 ORDER BY id DESC")
    fun getPublishedArticles(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles ORDER BY id DESC")
    fun getAllArticlesAdmin(): Flow<List<ArticleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: ArticleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<ArticleEntity>)

    @Update
    suspend fun updateArticle(article: ArticleEntity)

    @Delete
    suspend fun deleteArticle(article: ArticleEntity)

    // Notifications
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotifications(userId: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Update
    suspend fun updateNotification(notification: NotificationEntity)

    @Delete
    suspend fun deleteNotification(notification: NotificationEntity)

    @Query("DELETE FROM notifications WHERE userId = :userId")
    suspend fun clearNotifications(userId: String)

    // Emergency Services
    @Query("SELECT * FROM emergency_services ORDER BY distanceKm ASC")
    fun getEmergencyServices(): Flow<List<EmergencyServiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmergencyService(service: EmergencyServiceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmergencyServices(services: List<EmergencyServiceEntity>)

    @Update
    suspend fun updateEmergencyService(service: EmergencyServiceEntity)

    @Delete
    suspend fun deleteEmergencyService(service: EmergencyServiceEntity)
}

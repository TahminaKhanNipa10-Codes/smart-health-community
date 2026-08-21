package com.example.data.firebase

import android.util.Log
import com.example.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreService {
    private val firestore: FirebaseFirestore by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e("FirestoreService", "Firestore init fallback: ${e.message}")
            FirebaseFirestore.getInstance()
        }
    }

    companion object {
        private const val TAG = "FirestoreService"
        const val COLLECTION_USERS = "users"
        const val COLLECTION_DOCTORS = "doctors"
        const val COLLECTION_APPOINTMENTS = "appointments"
        const val COLLECTION_MEDICAL_RECORDS = "medicalRecords"
        const val COLLECTION_MEDICINES = "medicines"
        const val COLLECTION_VACCINATIONS = "vaccinations"
        const val COLLECTION_HEALTH_RECORDS = "healthRecords"
        const val COLLECTION_COMMUNITY_POSTS = "communityPosts"
        const val COLLECTION_COMMENTS = "comments"
        const val COLLECTION_CHAT_MESSAGES = "chatMessages"
        const val COLLECTION_BLOOD_DONORS = "bloodDonors"
        const val COLLECTION_BLOOD_REQUESTS = "bloodRequests"
        const val COLLECTION_ARTICLES = "articles"
        const val COLLECTION_NOTIFICATIONS = "notifications"
        const val COLLECTION_EMERGENCY_SERVICES = "emergencyServices"
    }

    // ==========================================
    // USERS (users/{uid})
    // ==========================================
    suspend fun getUser(userId: String): UserEntity? {
        return try {
            val doc = firestore.collection(COLLECTION_USERS).document(userId).get().await()
            if (doc.exists()) {
                docToUserEntity(doc.id, doc.data ?: emptyMap())
            } else null
        } catch (e: Exception) {
            Log.d(TAG, "Notice: could not get user $userId: ${e.message}")
            null
        }
    }

    suspend fun getUserByEmail(email: String): UserEntity? {
        return try {
            val query = firestore.collection(COLLECTION_USERS)
                .whereEqualTo("email", email.trim())
                .limit(1)
                .get()
                .await()
            val doc = query.documents.firstOrNull()
            if (doc != null && doc.exists()) {
                docToUserEntity(doc.id, doc.data ?: emptyMap())
            } else null
        } catch (e: Exception) {
            Log.d(TAG, "Notice: could not get user by email $email: ${e.message}")
            null
        }
    }

    fun getAllUsersFlow(): Flow<List<UserEntity>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_USERS)
            .orderBy("fullName")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.d(TAG, "Notice listening to users: ${error.message}")
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    docToUserEntity(doc.id, doc.data ?: emptyMap())
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveUser(user: UserEntity): Boolean {
        return try {
            val map = userEntityToMap(user)
            firestore.collection(COLLECTION_USERS).document(user.id).set(map, SetOptions.merge()).await()
            true
        } catch (e: Exception) {
            Log.d(TAG, "Notice saving user ${user.id}: ${e.message}")
            false
        }
    }

    suspend fun deleteUser(userId: String): Boolean {
        return try {
            firestore.collection(COLLECTION_USERS).document(userId).delete().await()
            true
        } catch (e: Exception) {
            Log.d(TAG, "Notice deleting user $userId: ${e.message}")
            false
        }
    }

    // ==========================================
    // DOCTORS (doctors/{doctorId})
    // ==========================================
    fun getAllDoctorsFlow(): Flow<List<DoctorEntity>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_DOCTORS)
            .orderBy("name")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.d(TAG, "Notice listening to doctors: ${error.message}")
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    docToDoctorEntity(doc.id, doc.data ?: emptyMap())
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getDoctor(id: String): DoctorEntity? {
        return try {
            val doc = firestore.collection(COLLECTION_DOCTORS).document(id).get().await()
            if (doc.exists()) docToDoctorEntity(doc.id, doc.data ?: emptyMap()) else null
        } catch (e: Exception) {
            Log.d(TAG, "Notice getting doctor $id: ${e.message}")
            null
        }
    }

    suspend fun saveDoctor(doctor: DoctorEntity): Boolean {
        return try {
            val map = doctorEntityToMap(doctor)
            firestore.collection(COLLECTION_DOCTORS).document(doctor.id).set(map, SetOptions.merge()).await()
            true
        } catch (e: Exception) {
            Log.d(TAG, "Notice saving doctor ${doctor.id}: ${e.message}")
            false
        }
    }

    suspend fun deleteDoctor(doctorId: String): Boolean {
        return try {
            firestore.collection(COLLECTION_DOCTORS).document(doctorId).delete().await()
            true
        } catch (e: Exception) {
            Log.d(TAG, "Notice deleting doctor $doctorId: ${e.message}")
            false
        }
    }

    // ==========================================
    // APPOINTMENTS (appointments/{appointmentId})
    // ==========================================
    fun getAppointmentsForPatientFlow(patientId: String): Flow<List<AppointmentEntity>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_APPOINTMENTS)
            .whereEqualTo("patientId", patientId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.d(TAG, "Notice listening to patient appointments: ${error.message}")
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    docToAppointmentEntity(doc.id, doc.data ?: emptyMap())
                }?.sortedByDescending { it.id } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun getAppointmentsForDoctorFlow(doctorId: String): Flow<List<AppointmentEntity>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_APPOINTMENTS)
            .whereEqualTo("doctorId", doctorId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.d(TAG, "Notice listening to doctor appointments: ${error.message}")
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    docToAppointmentEntity(doc.id, doc.data ?: emptyMap())
                }?.sortedByDescending { it.id } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun getAllAppointmentsAdminFlow(): Flow<List<AppointmentEntity>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_APPOINTMENTS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.d(TAG, "Notice listening to all appointments: ${error.message}")
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    docToAppointmentEntity(doc.id, doc.data ?: emptyMap())
                }?.sortedByDescending { it.id } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveAppointment(appointment: AppointmentEntity): String {
        return try {
            val docId = if (appointment.id > 0) appointment.id.toString() else System.currentTimeMillis().toString()
            val map = appointmentEntityToMap(appointment.copy(id = docId.toLongOrNull() ?: appointment.id))
            firestore.collection(COLLECTION_APPOINTMENTS).document(docId).set(map, SetOptions.merge()).await()
            docId
        } catch (e: Exception) {
            Log.d(TAG, "Notice saving appointment: ${e.message}")
            ""
        }
    }

    suspend fun deleteAppointment(appointmentId: Long): Boolean {
        return try {
            firestore.collection(COLLECTION_APPOINTMENTS).document(appointmentId.toString()).delete().await()
            true
        } catch (e: Exception) {
            Log.d(TAG, "Notice deleting appointment $appointmentId: ${e.message}")
            false
        }
    }

    // ==========================================
    // MEDICAL RECORDS & LAB REPORTS (medicalRecords/{recordId})
    // ==========================================
    fun getMedicalRecordsFlow(userId: String): Flow<List<MedicalRecordEntity>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_MEDICAL_RECORDS)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.d(TAG, "Notice listening to medical records: ${error.message}")
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    docToMedicalRecordEntity(doc.id, doc.data ?: emptyMap())
                }?.sortedByDescending { it.id } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun getAllMedicalRecordsAdminFlow(): Flow<List<MedicalRecordEntity>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_MEDICAL_RECORDS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.d(TAG, "Notice listening to admin medical records: ${error.message}")
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    docToMedicalRecordEntity(doc.id, doc.data ?: emptyMap())
                }?.sortedByDescending { it.id } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveMedicalRecord(record: MedicalRecordEntity): String {
        return try {
            val docId = if (record.id > 0) record.id.toString() else System.currentTimeMillis().toString()
            val map = medicalRecordEntityToMap(record.copy(id = docId.toLongOrNull() ?: record.id))
            firestore.collection(COLLECTION_MEDICAL_RECORDS).document(docId).set(map, SetOptions.merge()).await()
            docId
        } catch (e: Exception) {
            Log.d(TAG, "Notice saving medical record: ${e.message}")
            ""
        }
    }

    suspend fun deleteMedicalRecord(recordId: Long): Boolean {
        return try {
            firestore.collection(COLLECTION_MEDICAL_RECORDS).document(recordId.toString()).delete().await()
            true
        } catch (e: Exception) {
            Log.d(TAG, "Notice deleting medical record $recordId: ${e.message}")
            false
        }
    }

    // ==========================================
    // MEDICINES (medicines/{medicineId})
    // ==========================================
    fun getMedicinesFlow(userId: String): Flow<List<MedicineEntity>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_MEDICINES)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.d(TAG, "Notice listening to medicines: ${error.message}")
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    docToMedicineEntity(doc.id, doc.data ?: emptyMap())
                }?.sortedByDescending { it.id } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveMedicine(medicine: MedicineEntity): String {
        return try {
            val docId = if (medicine.id > 0) medicine.id.toString() else System.currentTimeMillis().toString()
            val map = medicineEntityToMap(medicine.copy(id = docId.toLongOrNull() ?: medicine.id))
            firestore.collection(COLLECTION_MEDICINES).document(docId).set(map, SetOptions.merge()).await()
            docId
        } catch (e: Exception) {
            Log.d(TAG, "Notice saving medicine: ${e.message}")
            ""
        }
    }

    suspend fun deleteMedicine(medicineId: Long): Boolean {
        return try {
            firestore.collection(COLLECTION_MEDICINES).document(medicineId.toString()).delete().await()
            true
        } catch (e: Exception) {
            Log.d(TAG, "Notice deleting medicine $medicineId: ${e.message}")
            false
        }
    }

    // ==========================================
    // VACCINATIONS (vaccinations/{vaccinationId})
    // ==========================================
    fun getVaccinationsFlow(userId: String): Flow<List<VaccinationEntity>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_VACCINATIONS)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.d(TAG, "Notice listening to vaccinations: ${error.message}")
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    docToVaccinationEntity(doc.id, doc.data ?: emptyMap())
                }?.sortedByDescending { it.id } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveVaccination(vaccination: VaccinationEntity): String {
        return try {
            val docId = if (vaccination.id > 0) vaccination.id.toString() else System.currentTimeMillis().toString()
            val map = vaccinationEntityToMap(vaccination.copy(id = docId.toLongOrNull() ?: vaccination.id))
            firestore.collection(COLLECTION_VACCINATIONS).document(docId).set(map, SetOptions.merge()).await()
            docId
        } catch (e: Exception) {
            Log.d(TAG, "Notice saving vaccination: ${e.message}")
            ""
        }
    }

    suspend fun deleteVaccination(vaccinationId: Long): Boolean {
        return try {
            firestore.collection(COLLECTION_VACCINATIONS).document(vaccinationId.toString()).delete().await()
            true
        } catch (e: Exception) {
            Log.d(TAG, "Notice deleting vaccination $vaccinationId: ${e.message}")
            false
        }
    }

    // ==========================================
    // HEALTH VITALS (healthRecords/{healthRecordId})
    // ==========================================
    fun getHealthRecordsFlow(userId: String): Flow<List<HealthRecordEntity>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_HEALTH_RECORDS)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.d(TAG, "Notice listening to health records: ${error.message}")
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    docToHealthRecordEntity(doc.id, doc.data ?: emptyMap())
                }?.sortedByDescending { it.timestamp } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveHealthRecord(record: HealthRecordEntity): String {
        return try {
            val docId = if (record.id > 0) record.id.toString() else System.currentTimeMillis().toString()
            val map = healthRecordEntityToMap(record.copy(id = docId.toLongOrNull() ?: record.id))
            firestore.collection(COLLECTION_HEALTH_RECORDS).document(docId).set(map, SetOptions.merge()).await()
            docId
        } catch (e: Exception) {
            Log.d(TAG, "Notice saving health record: ${e.message}")
            ""
        }
    }

    suspend fun deleteHealthRecord(recordId: Long): Boolean {
        return try {
            firestore.collection(COLLECTION_HEALTH_RECORDS).document(recordId.toString()).delete().await()
            true
        } catch (e: Exception) {
            Log.d(TAG, "Notice deleting health record $recordId: ${e.message}")
            false
        }
    }

    // ==========================================
    // COMMUNITY POSTS & COMMENTS (communityPosts/{postId})
    // ==========================================
    fun getCommunityPostsFlow(): Flow<List<CommunityPostEntity>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_COMMUNITY_POSTS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.d(TAG, "Notice listening to posts: ${error.message}")
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    docToCommunityPostEntity(doc.id, doc.data ?: emptyMap())
                }?.filter { !it.isReported }?.sortedByDescending { it.timestamp } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun getCommunityPostsAdminFlow(): Flow<List<CommunityPostEntity>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_COMMUNITY_POSTS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.d(TAG, "Notice listening to admin posts: ${error.message}")
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    docToCommunityPostEntity(doc.id, doc.data ?: emptyMap())
                }?.sortedByDescending { it.timestamp } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveCommunityPost(post: CommunityPostEntity): String {
        return try {
            val docId = if (post.id > 0) post.id.toString() else System.currentTimeMillis().toString()
            val map = communityPostEntityToMap(post.copy(id = docId.toLongOrNull() ?: post.id))
            firestore.collection(COLLECTION_COMMUNITY_POSTS).document(docId).set(map, SetOptions.merge()).await()
            docId
        } catch (e: Exception) {
            Log.d(TAG, "Notice saving post: ${e.message}")
            ""
        }
    }

    suspend fun deleteCommunityPost(postId: Long): Boolean {
        return try {
            firestore.collection(COLLECTION_COMMUNITY_POSTS).document(postId.toString()).delete().await()
            true
        } catch (e: Exception) {
            Log.d(TAG, "Notice deleting post $postId: ${e.message}")
            false
        }
    }

    fun getCommentsForPostFlow(postId: Long): Flow<List<CommentEntity>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_COMMUNITY_POSTS)
            .document(postId.toString())
            .collection(COLLECTION_COMMENTS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.d(TAG, "Notice listening to comments: ${error.message}")
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    docToCommentEntity(doc.id, postId, doc.data ?: emptyMap())
                }?.sortedBy { it.timestamp } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveComment(comment: CommentEntity): String {
        return try {
            val docId = if (comment.id > 0) comment.id.toString() else System.currentTimeMillis().toString()
            val map = commentEntityToMap(comment.copy(id = docId.toLongOrNull() ?: comment.id))
            firestore.collection(COLLECTION_COMMUNITY_POSTS)
                .document(comment.postId.toString())
                .collection(COLLECTION_COMMENTS)
                .document(docId)
                .set(map, SetOptions.merge())
                .await()
            docId
        } catch (e: Exception) {
            Log.d(TAG, "Notice saving comment: ${e.message}")
            ""
        }
    }

    suspend fun deleteComment(comment: CommentEntity): Boolean {
        return try {
            firestore.collection(COLLECTION_COMMUNITY_POSTS)
                .document(comment.postId.toString())
                .collection(COLLECTION_COMMENTS)
                .document(comment.id.toString())
                .delete()
                .await()
            true
        } catch (e: Exception) {
            Log.d(TAG, "Notice deleting comment: ${e.message}")
            false
        }
    }

    // ==========================================
    // CHAT MESSAGES (chatMessages/{messageId})
    // ==========================================
    fun getChatMessagesFlow(channelId: String): Flow<List<ChatMessageEntity>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_CHAT_MESSAGES)
            .whereEqualTo("channelId", channelId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.d(TAG, "Notice listening to chat messages: ${error.message}")
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    docToChatMessageEntity(doc.id, doc.data ?: emptyMap())
                }?.sortedBy { it.timestamp } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveChatMessage(msg: ChatMessageEntity): String {
        return try {
            val docId = if (msg.id > 0) msg.id.toString() else System.currentTimeMillis().toString()
            val map = chatMessageEntityToMap(msg.copy(id = docId.toLongOrNull() ?: msg.id))
            firestore.collection(COLLECTION_CHAT_MESSAGES).document(docId).set(map, SetOptions.merge()).await()
            docId
        } catch (e: Exception) {
            Log.d(TAG, "Notice saving chat message: ${e.message}")
            ""
        }
    }

    suspend fun deleteChatMessage(msgId: Long): Boolean {
        return try {
            firestore.collection(COLLECTION_CHAT_MESSAGES).document(msgId.toString()).delete().await()
            true
        } catch (e: Exception) {
            Log.d(TAG, "Notice deleting chat msg: ${e.message}")
            false
        }
    }

    // ==========================================
    // BLOOD DONORS (bloodDonors/{donorId})
    // ==========================================
    fun getAllBloodDonorsFlow(): Flow<List<BloodDonorEntity>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_BLOOD_DONORS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.d(TAG, "Notice listening to blood donors: ${error.message}")
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    docToBloodDonorEntity(doc.id, doc.data ?: emptyMap())
                }?.sortedByDescending { it.id } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveBloodDonor(donor: BloodDonorEntity): String {
        return try {
            val docId = if (donor.id > 0) donor.id.toString() else System.currentTimeMillis().toString()
            val map = bloodDonorEntityToMap(donor.copy(id = docId.toLongOrNull() ?: donor.id))
            firestore.collection(COLLECTION_BLOOD_DONORS).document(docId).set(map, SetOptions.merge()).await()
            docId
        } catch (e: Exception) {
            Log.d(TAG, "Notice saving blood donor: ${e.message}")
            ""
        }
    }

    suspend fun deleteBloodDonor(donorId: Long): Boolean {
        return try {
            firestore.collection(COLLECTION_BLOOD_DONORS).document(donorId.toString()).delete().await()
            true
        } catch (e: Exception) {
            Log.d(TAG, "Notice deleting donor: ${e.message}")
            false
        }
    }

    // ==========================================
    // BLOOD REQUESTS (bloodRequests/{requestId})
    // ==========================================
    fun getAllBloodRequestsFlow(): Flow<List<BloodRequestEntity>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_BLOOD_REQUESTS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.d(TAG, "Notice listening to blood requests: ${error.message}")
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    docToBloodRequestEntity(doc.id, doc.data ?: emptyMap())
                }?.sortedByDescending { it.id } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveBloodRequest(request: BloodRequestEntity): String {
        return try {
            val docId = if (request.id > 0) request.id.toString() else System.currentTimeMillis().toString()
            val map = bloodRequestEntityToMap(request.copy(id = docId.toLongOrNull() ?: request.id))
            firestore.collection(COLLECTION_BLOOD_REQUESTS).document(docId).set(map, SetOptions.merge()).await()
            docId
        } catch (e: Exception) {
            Log.d(TAG, "Notice saving blood request: ${e.message}")
            ""
        }
    }

    suspend fun deleteBloodRequest(requestId: Long): Boolean {
        return try {
            firestore.collection(COLLECTION_BLOOD_REQUESTS).document(requestId.toString()).delete().await()
            true
        } catch (e: Exception) {
            Log.d(TAG, "Notice deleting blood request: ${e.message}")
            false
        }
    }

    // ==========================================
    // ARTICLES (articles/{articleId})
    // ==========================================
    fun getPublishedArticlesFlow(): Flow<List<ArticleEntity>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_ARTICLES)
            .whereEqualTo("isPublished", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.d(TAG, "Notice listening to articles: ${error.message}")
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    docToArticleEntity(doc.id, doc.data ?: emptyMap())
                }?.sortedByDescending { it.id } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun getAllArticlesAdminFlow(): Flow<List<ArticleEntity>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_ARTICLES)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.d(TAG, "Notice listening to admin articles: ${error.message}")
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    docToArticleEntity(doc.id, doc.data ?: emptyMap())
                }?.sortedByDescending { it.id } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveArticle(article: ArticleEntity): String {
        return try {
            val docId = if (article.id > 0) article.id.toString() else System.currentTimeMillis().toString()
            val map = articleEntityToMap(article.copy(id = docId.toLongOrNull() ?: article.id))
            firestore.collection(COLLECTION_ARTICLES).document(docId).set(map, SetOptions.merge()).await()
            docId
        } catch (e: Exception) {
            Log.d(TAG, "Notice saving article: ${e.message}")
            ""
        }
    }

    suspend fun deleteArticle(articleId: Long): Boolean {
        return try {
            firestore.collection(COLLECTION_ARTICLES).document(articleId.toString()).delete().await()
            true
        } catch (e: Exception) {
            Log.d(TAG, "Notice deleting article: ${e.message}")
            false
        }
    }

    // ==========================================
    // NOTIFICATIONS (notifications/{notificationId})
    // ==========================================
    fun getNotificationsFlow(userId: String): Flow<List<NotificationEntity>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_NOTIFICATIONS)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.d(TAG, "Notice listening to notifications: ${error.message}")
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    docToNotificationEntity(doc.id, doc.data ?: emptyMap())
                }?.sortedByDescending { it.timestamp } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveNotification(n: NotificationEntity): String {
        return try {
            val docId = if (n.id > 0) n.id.toString() else System.currentTimeMillis().toString()
            val map = notificationEntityToMap(n.copy(id = docId.toLongOrNull() ?: n.id))
            firestore.collection(COLLECTION_NOTIFICATIONS).document(docId).set(map, SetOptions.merge()).await()
            docId
        } catch (e: Exception) {
            Log.d(TAG, "Notice saving notification: ${e.message}")
            ""
        }
    }

    suspend fun clearNotifications(userId: String): Boolean {
        return try {
            val query = firestore.collection(COLLECTION_NOTIFICATIONS).whereEqualTo("userId", userId).get().await()
            for (doc in query.documents) {
                doc.reference.delete()
            }
            true
        } catch (e: Exception) {
            Log.d(TAG, "Notice clearing notifications: ${e.message}")
            false
        }
    }

    // ==========================================
    // EMERGENCY SERVICES (emergencyServices/{serviceId})
    // ==========================================
    fun getEmergencyServicesFlow(): Flow<List<EmergencyServiceEntity>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_EMERGENCY_SERVICES)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.d(TAG, "Notice listening to emergency services: ${error.message}")
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    docToEmergencyServiceEntity(doc.id, doc.data ?: emptyMap())
                }?.sortedBy { it.distanceKm } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveEmergencyService(service: EmergencyServiceEntity): String {
        return try {
            val docId = if (service.id > 0) service.id.toString() else System.currentTimeMillis().toString()
            val map = emergencyServiceEntityToMap(service.copy(id = docId.toLongOrNull() ?: service.id))
            firestore.collection(COLLECTION_EMERGENCY_SERVICES).document(docId).set(map, SetOptions.merge()).await()
            docId
        } catch (e: Exception) {
            Log.d(TAG, "Notice saving emergency service: ${e.message}")
            ""
        }
    }

    suspend fun deleteEmergencyService(serviceId: Long): Boolean {
        return try {
            firestore.collection(COLLECTION_EMERGENCY_SERVICES).document(serviceId.toString()).delete().await()
            true
        } catch (e: Exception) {
            Log.d(TAG, "Notice deleting emergency service: ${e.message}")
            false
        }
    }

    // ==========================================
    // MAPPERS: UserEntity
    // ==========================================
    private fun userEntityToMap(u: UserEntity): Map<String, Any?> = mapOf(
        "id" to u.id,
        "email" to u.email,
        "fullName" to u.fullName,
        "phone" to u.phone,
        "dob" to u.dob,
        "gender" to u.gender,
        "bloodGroup" to u.bloodGroup,
        "address" to u.address,
        "emergencyContact" to u.emergencyContact,
        "emergencyPhone" to u.emergencyPhone,
        "medicalNotes" to u.medicalNotes,
        "role" to u.role,
        "avatarUrl" to u.avatarUrl,
        "isVerifiedDoctor" to u.isVerifiedDoctor,
        "isActive" to u.isActive,
        "updatedAt" to System.currentTimeMillis()
    )

    private fun docToUserEntity(docId: String, d: Map<String, Any?>): UserEntity = UserEntity(
        id = docId,
        email = (d["email"] as? String) ?: "",
        fullName = (d["fullName"] as? String) ?: "User",
        phone = (d["phone"] as? String) ?: "",
        dob = (d["dob"] as? String) ?: "",
        gender = (d["gender"] as? String) ?: "Not Specified",
        bloodGroup = (d["bloodGroup"] as? String) ?: "O+",
        address = (d["address"] as? String) ?: "",
        emergencyContact = (d["emergencyContact"] as? String) ?: "",
        emergencyPhone = (d["emergencyPhone"] as? String) ?: "",
        medicalNotes = (d["medicalNotes"] as? String) ?: "",
        role = (d["role"] as? String) ?: "USER",
        avatarUrl = (d["avatarUrl"] as? String) ?: "",
        isVerifiedDoctor = (d["isVerifiedDoctor"] as? Boolean) ?: false,
        isActive = (d["isActive"] as? Boolean) ?: true
    )

    // ==========================================
    // MAPPERS: DoctorEntity
    // ==========================================
    private fun doctorEntityToMap(doc: DoctorEntity): Map<String, Any?> = mapOf(
        "id" to doc.id,
        "name" to doc.name,
        "specialty" to doc.specialty,
        "qualifications" to doc.qualifications,
        "experienceYears" to doc.experienceYears,
        "hospital" to doc.hospital,
        "location" to doc.location,
        "consultationFee" to doc.consultationFee,
        "rating" to doc.rating.toDouble(),
        "reviewCount" to doc.reviewCount,
        "bio" to doc.bio,
        "phone" to doc.phone,
        "email" to doc.email,
        "isVerified" to doc.isVerified,
        "availableDays" to doc.availableDays,
        "availableSlots" to doc.availableSlots,
        "avatarUrl" to doc.avatarUrl
    )

    private fun docToDoctorEntity(docId: String, d: Map<String, Any?>): DoctorEntity = DoctorEntity(
        id = docId,
        name = (d["name"] as? String) ?: "Doctor",
        specialty = (d["specialty"] as? String) ?: "General Physician",
        qualifications = (d["qualifications"] as? String) ?: "MBBS",
        experienceYears = ((d["experienceYears"] as? Number)?.toInt()) ?: 5,
        hospital = (d["hospital"] as? String) ?: "",
        location = (d["location"] as? String) ?: "",
        consultationFee = ((d["consultationFee"] as? Number)?.toDouble()) ?: 50.0,
        rating = ((d["rating"] as? Number)?.toFloat()) ?: 4.8f,
        reviewCount = ((d["reviewCount"] as? Number)?.toInt()) ?: 50,
        bio = (d["bio"] as? String) ?: "",
        phone = (d["phone"] as? String) ?: "",
        email = (d["email"] as? String) ?: "",
        isVerified = (d["isVerified"] as? Boolean) ?: true,
        availableDays = (d["availableDays"] as? String) ?: "Mon - Fri",
        availableSlots = (d["availableSlots"] as? String) ?: "09:00 AM, 02:00 PM",
        avatarUrl = (d["avatarUrl"] as? String) ?: ""
    )

    // ==========================================
    // MAPPERS: AppointmentEntity
    // ==========================================
    private fun appointmentEntityToMap(a: AppointmentEntity): Map<String, Any?> = mapOf(
        "id" to a.id,
        "patientId" to a.patientId,
        "patientName" to a.patientName,
        "patientPhone" to a.patientPhone,
        "doctorId" to a.doctorId,
        "doctorName" to a.doctorName,
        "doctorSpecialty" to a.doctorSpecialty,
        "date" to a.date,
        "timeSlot" to a.timeSlot,
        "symptoms" to a.symptoms,
        "notes" to a.notes,
        "status" to a.status,
        "doctorPrescription" to a.doctorPrescription,
        "consultationNotes" to a.consultationNotes,
        "createdAt" to a.createdAt
    )

    private fun docToAppointmentEntity(docId: String, d: Map<String, Any?>): AppointmentEntity = AppointmentEntity(
        id = docId.toLongOrNull() ?: ((d["id"] as? Number)?.toLong()) ?: 0L,
        patientId = (d["patientId"] as? String) ?: "",
        patientName = (d["patientName"] as? String) ?: "",
        patientPhone = (d["patientPhone"] as? String) ?: "",
        doctorId = (d["doctorId"] as? String) ?: "",
        doctorName = (d["doctorName"] as? String) ?: "",
        doctorSpecialty = (d["doctorSpecialty"] as? String) ?: "",
        date = (d["date"] as? String) ?: "",
        timeSlot = (d["timeSlot"] as? String) ?: "",
        symptoms = (d["symptoms"] as? String) ?: "",
        notes = (d["notes"] as? String) ?: "",
        status = (d["status"] as? String) ?: "CONFIRMED",
        doctorPrescription = (d["doctorPrescription"] as? String) ?: "",
        consultationNotes = (d["consultationNotes"] as? String) ?: "",
        createdAt = ((d["createdAt"] as? Number)?.toLong()) ?: System.currentTimeMillis()
    )

    // ==========================================
    // MAPPERS: MedicalRecordEntity
    // ==========================================
    private fun medicalRecordEntityToMap(m: MedicalRecordEntity): Map<String, Any?> = mapOf(
        "id" to m.id,
        "userId" to m.userId,
        "title" to m.title,
        "doctorName" to m.doctorName,
        "hospitalClinic" to m.hospitalClinic,
        "recordDate" to m.recordDate,
        "category" to m.category,
        "notes" to m.notes,
        "prescriptionText" to m.prescriptionText,
        "labResults" to m.labResults,
        "fileAttachmentName" to m.fileAttachmentName,
        "fileSizeBytes" to m.fileSizeBytes
    )

    private fun docToMedicalRecordEntity(docId: String, d: Map<String, Any?>): MedicalRecordEntity = MedicalRecordEntity(
        id = docId.toLongOrNull() ?: ((d["id"] as? Number)?.toLong()) ?: 0L,
        userId = (d["userId"] as? String) ?: "",
        title = (d["title"] as? String) ?: "Medical Record",
        doctorName = (d["doctorName"] as? String) ?: "",
        hospitalClinic = (d["hospitalClinic"] as? String) ?: "",
        recordDate = (d["recordDate"] as? String) ?: "",
        category = (d["category"] as? String) ?: "General",
        notes = (d["notes"] as? String) ?: "",
        prescriptionText = (d["prescriptionText"] as? String) ?: "",
        labResults = (d["labResults"] as? String) ?: "",
        fileAttachmentName = (d["fileAttachmentName"] as? String) ?: "report.pdf",
        fileSizeBytes = ((d["fileSizeBytes"] as? Number)?.toLong()) ?: (1024 * 200L)
    )

    // ==========================================
    // MAPPERS: MedicineEntity
    // ==========================================
    private fun medicineEntityToMap(m: MedicineEntity): Map<String, Any?> = mapOf(
        "id" to m.id,
        "userId" to m.userId,
        "name" to m.name,
        "dosage" to m.dosage,
        "frequency" to m.frequency,
        "timeOfDay" to m.timeOfDay,
        "startDate" to m.startDate,
        "endDate" to m.endDate,
        "instructions" to m.instructions,
        "notes" to m.notes,
        "isActive" to m.isActive,
        "isTakenToday" to m.isTakenToday,
        "lastTakenTimestamp" to m.lastTakenTimestamp
    )

    private fun docToMedicineEntity(docId: String, d: Map<String, Any?>): MedicineEntity = MedicineEntity(
        id = docId.toLongOrNull() ?: ((d["id"] as? Number)?.toLong()) ?: 0L,
        userId = (d["userId"] as? String) ?: "",
        name = (d["name"] as? String) ?: "",
        dosage = (d["dosage"] as? String) ?: "",
        frequency = (d["frequency"] as? String) ?: "Once Daily",
        timeOfDay = (d["timeOfDay"] as? String) ?: "08:00 AM",
        startDate = (d["startDate"] as? String) ?: "",
        endDate = (d["endDate"] as? String) ?: "",
        instructions = (d["instructions"] as? String) ?: "",
        notes = (d["notes"] as? String) ?: "",
        isActive = (d["isActive"] as? Boolean) ?: true,
        isTakenToday = (d["isTakenToday"] as? Boolean) ?: false,
        lastTakenTimestamp = ((d["lastTakenTimestamp"] as? Number)?.toLong()) ?: 0L
    )

    // ==========================================
    // MAPPERS: VaccinationEntity
    // ==========================================
    private fun vaccinationEntityToMap(v: VaccinationEntity): Map<String, Any?> = mapOf(
        "id" to v.id,
        "userId" to v.userId,
        "vaccineName" to v.vaccineName,
        "doseNumber" to v.doseNumber,
        "dateReceived" to v.dateReceived,
        "nextDoseDate" to v.nextDoseDate,
        "doctorOrHospital" to v.doctorOrHospital,
        "status" to v.status,
        "notes" to v.notes
    )

    private fun docToVaccinationEntity(docId: String, d: Map<String, Any?>): VaccinationEntity = VaccinationEntity(
        id = docId.toLongOrNull() ?: ((d["id"] as? Number)?.toLong()) ?: 0L,
        userId = (d["userId"] as? String) ?: "",
        vaccineName = (d["vaccineName"] as? String) ?: "",
        doseNumber = (d["doseNumber"] as? String) ?: "Dose 1",
        dateReceived = (d["dateReceived"] as? String) ?: "",
        nextDoseDate = (d["nextDoseDate"] as? String) ?: "",
        doctorOrHospital = (d["doctorOrHospital"] as? String) ?: "",
        status = (d["status"] as? String) ?: "COMPLETED",
        notes = (d["notes"] as? String) ?: ""
    )

    // ==========================================
    // MAPPERS: HealthRecordEntity
    // ==========================================
    private fun healthRecordEntityToMap(h: HealthRecordEntity): Map<String, Any?> = mapOf(
        "id" to h.id,
        "userId" to h.userId,
        "timestamp" to h.timestamp,
        "dateString" to h.dateString,
        "weightKg" to h.weightKg.toDouble(),
        "heightCm" to h.heightCm.toDouble(),
        "systolic" to h.systolic,
        "diastolic" to h.diastolic,
        "heartRate" to h.heartRate,
        "bloodSugarMg" to h.bloodSugarMg.toDouble(),
        "temperatureC" to h.temperatureC.toDouble(),
        "waterMl" to h.waterMl,
        "sleepHours" to h.sleepHours.toDouble(),
        "steps" to h.steps,
        "exerciseMinutes" to h.exerciseMinutes,
        "mood" to h.mood,
        "notes" to h.notes
    )

    private fun docToHealthRecordEntity(docId: String, d: Map<String, Any?>): HealthRecordEntity = HealthRecordEntity(
        id = docId.toLongOrNull() ?: ((d["id"] as? Number)?.toLong()) ?: 0L,
        userId = (d["userId"] as? String) ?: "",
        timestamp = ((d["timestamp"] as? Number)?.toLong()) ?: System.currentTimeMillis(),
        dateString = (d["dateString"] as? String) ?: "",
        weightKg = ((d["weightKg"] as? Number)?.toFloat()) ?: 0f,
        heightCm = ((d["heightCm"] as? Number)?.toFloat()) ?: 0f,
        systolic = ((d["systolic"] as? Number)?.toInt()) ?: 120,
        diastolic = ((d["diastolic"] as? Number)?.toInt()) ?: 80,
        heartRate = ((d["heartRate"] as? Number)?.toInt()) ?: 72,
        bloodSugarMg = ((d["bloodSugarMg"] as? Number)?.toFloat()) ?: 95f,
        temperatureC = ((d["temperatureC"] as? Number)?.toFloat()) ?: 36.6f,
        waterMl = ((d["waterMl"] as? Number)?.toInt()) ?: 1500,
        sleepHours = ((d["sleepHours"] as? Number)?.toFloat()) ?: 7.5f,
        steps = ((d["steps"] as? Number)?.toInt()) ?: 6000,
        exerciseMinutes = ((d["exerciseMinutes"] as? Number)?.toInt()) ?: 30,
        mood = (d["mood"] as? String) ?: "Good",
        notes = (d["notes"] as? String) ?: ""
    )

    // ==========================================
    // MAPPERS: CommunityPostEntity
    // ==========================================
    private fun communityPostEntityToMap(p: CommunityPostEntity): Map<String, Any?> = mapOf(
        "id" to p.id,
        "authorId" to p.authorId,
        "authorName" to p.authorName,
        "authorRole" to p.authorRole,
        "title" to p.title,
        "content" to p.content,
        "category" to p.category,
        "timestamp" to p.timestamp,
        "likesCount" to p.likesCount,
        "commentsCount" to p.commentsCount,
        "isLikedByMe" to p.isLikedByMe,
        "isReported" to p.isReported
    )

    private fun docToCommunityPostEntity(docId: String, d: Map<String, Any?>): CommunityPostEntity = CommunityPostEntity(
        id = docId.toLongOrNull() ?: ((d["id"] as? Number)?.toLong()) ?: 0L,
        authorId = (d["authorId"] as? String) ?: "",
        authorName = (d["authorName"] as? String) ?: "Member",
        authorRole = (d["authorRole"] as? String) ?: "USER",
        title = (d["title"] as? String) ?: "",
        content = (d["content"] as? String) ?: "",
        category = (d["category"] as? String) ?: "General",
        timestamp = ((d["timestamp"] as? Number)?.toLong()) ?: System.currentTimeMillis(),
        likesCount = ((d["likesCount"] as? Number)?.toInt()) ?: 0,
        commentsCount = ((d["commentsCount"] as? Number)?.toInt()) ?: 0,
        isLikedByMe = (d["isLikedByMe"] as? Boolean) ?: false,
        isReported = (d["isReported"] as? Boolean) ?: false
    )

    // ==========================================
    // MAPPERS: CommentEntity
    // ==========================================
    private fun commentEntityToMap(c: CommentEntity): Map<String, Any?> = mapOf(
        "id" to c.id,
        "postId" to c.postId,
        "authorId" to c.authorId,
        "authorName" to c.authorName,
        "authorRole" to c.authorRole,
        "content" to c.content,
        "timestamp" to c.timestamp
    )

    private fun docToCommentEntity(docId: String, postId: Long, d: Map<String, Any?>): CommentEntity = CommentEntity(
        id = docId.toLongOrNull() ?: ((d["id"] as? Number)?.toLong()) ?: 0L,
        postId = postId,
        authorId = (d["authorId"] as? String) ?: "",
        authorName = (d["authorName"] as? String) ?: "Member",
        authorRole = (d["authorRole"] as? String) ?: "USER",
        content = (d["content"] as? String) ?: "",
        timestamp = ((d["timestamp"] as? Number)?.toLong()) ?: System.currentTimeMillis()
    )

    // ==========================================
    // MAPPERS: ChatMessageEntity
    // ==========================================
    private fun chatMessageEntityToMap(m: ChatMessageEntity): Map<String, Any?> = mapOf(
        "id" to m.id,
        "senderId" to m.senderId,
        "senderName" to m.senderName,
        "senderRole" to m.senderRole,
        "channelId" to m.channelId,
        "message" to m.message,
        "timestamp" to m.timestamp
    )

    private fun docToChatMessageEntity(docId: String, d: Map<String, Any?>): ChatMessageEntity = ChatMessageEntity(
        id = docId.toLongOrNull() ?: ((d["id"] as? Number)?.toLong()) ?: 0L,
        senderId = (d["senderId"] as? String) ?: "",
        senderName = (d["senderName"] as? String) ?: "User",
        senderRole = (d["senderRole"] as? String) ?: "USER",
        channelId = (d["channelId"] as? String) ?: "general",
        message = (d["message"] as? String) ?: "",
        timestamp = ((d["timestamp"] as? Number)?.toLong()) ?: System.currentTimeMillis()
    )

    // ==========================================
    // MAPPERS: BloodDonorEntity
    // ==========================================
    private fun bloodDonorEntityToMap(b: BloodDonorEntity): Map<String, Any?> = mapOf(
        "id" to b.id,
        "userId" to b.userId,
        "name" to b.name,
        "bloodGroup" to b.bloodGroup,
        "phone" to b.phone,
        "location" to b.location,
        "isAvailable" to b.isAvailable,
        "lastDonationDate" to b.lastDonationDate
    )

    private fun docToBloodDonorEntity(docId: String, d: Map<String, Any?>): BloodDonorEntity = BloodDonorEntity(
        id = docId.toLongOrNull() ?: ((d["id"] as? Number)?.toLong()) ?: 0L,
        userId = (d["userId"] as? String) ?: "",
        name = (d["name"] as? String) ?: "Donor",
        bloodGroup = (d["bloodGroup"] as? String) ?: "O+",
        phone = (d["phone"] as? String) ?: "",
        location = (d["location"] as? String) ?: "",
        isAvailable = (d["isAvailable"] as? Boolean) ?: true,
        lastDonationDate = (d["lastDonationDate"] as? String) ?: "2026-04-10"
    )

    // ==========================================
    // MAPPERS: BloodRequestEntity
    // ==========================================
    private fun bloodRequestEntityToMap(r: BloodRequestEntity): Map<String, Any?> = mapOf(
        "id" to r.id,
        "requesterId" to r.requesterId,
        "patientName" to r.patientName,
        "bloodGroup" to r.bloodGroup,
        "unitsRequired" to r.unitsRequired,
        "hospital" to r.hospital,
        "location" to r.location,
        "contactPhone" to r.contactPhone,
        "requiredDate" to r.requiredDate,
        "urgency" to r.urgency,
        "status" to r.status,
        "description" to r.description
    )

    private fun docToBloodRequestEntity(docId: String, d: Map<String, Any?>): BloodRequestEntity = BloodRequestEntity(
        id = docId.toLongOrNull() ?: ((d["id"] as? Number)?.toLong()) ?: 0L,
        requesterId = (d["requesterId"] as? String) ?: "",
        patientName = (d["patientName"] as? String) ?: "",
        bloodGroup = (d["bloodGroup"] as? String) ?: "O+",
        unitsRequired = ((d["unitsRequired"] as? Number)?.toInt()) ?: 1,
        hospital = (d["hospital"] as? String) ?: "",
        location = (d["location"] as? String) ?: "",
        contactPhone = (d["contactPhone"] as? String) ?: "",
        requiredDate = (d["requiredDate"] as? String) ?: "",
        urgency = (d["urgency"] as? String) ?: "HIGH",
        status = (d["status"] as? String) ?: "ACTIVE",
        description = (d["description"] as? String) ?: ""
    )

    // ==========================================
    // MAPPERS: ArticleEntity
    // ==========================================
    private fun articleEntityToMap(a: ArticleEntity): Map<String, Any?> = mapOf(
        "id" to a.id,
        "title" to a.title,
        "excerpt" to a.excerpt,
        "content" to a.content,
        "category" to a.category,
        "author" to a.author,
        "readTimeMinutes" to a.readTimeMinutes,
        "date" to a.date,
        "imageUrl" to a.imageUrl,
        "isPublished" to a.isPublished
    )

    private fun docToArticleEntity(docId: String, d: Map<String, Any?>): ArticleEntity = ArticleEntity(
        id = docId.toLongOrNull() ?: ((d["id"] as? Number)?.toLong()) ?: 0L,
        title = (d["title"] as? String) ?: "",
        excerpt = (d["excerpt"] as? String) ?: "",
        content = (d["content"] as? String) ?: "",
        category = (d["category"] as? String) ?: "Health",
        author = (d["author"] as? String) ?: "Smart Health",
        readTimeMinutes = ((d["readTimeMinutes"] as? Number)?.toInt()) ?: 4,
        date = (d["date"] as? String) ?: "",
        imageUrl = (d["imageUrl"] as? String) ?: "",
        isPublished = (d["isPublished"] as? Boolean) ?: true
    )

    // ==========================================
    // MAPPERS: NotificationEntity
    // ==========================================
    private fun notificationEntityToMap(n: NotificationEntity): Map<String, Any?> = mapOf(
        "id" to n.id,
        "userId" to n.userId,
        "title" to n.title,
        "message" to n.message,
        "type" to n.type,
        "timestamp" to n.timestamp,
        "isRead" to n.isRead
    )

    private fun docToNotificationEntity(docId: String, d: Map<String, Any?>): NotificationEntity = NotificationEntity(
        id = docId.toLongOrNull() ?: ((d["id"] as? Number)?.toLong()) ?: 0L,
        userId = (d["userId"] as? String) ?: "",
        title = (d["title"] as? String) ?: "",
        message = (d["message"] as? String) ?: "",
        type = (d["type"] as? String) ?: "SYSTEM",
        timestamp = ((d["timestamp"] as? Number)?.toLong()) ?: System.currentTimeMillis(),
        isRead = (d["isRead"] as? Boolean) ?: false
    )

    // ==========================================
    // MAPPERS: EmergencyServiceEntity
    // ==========================================
    private fun emergencyServiceEntityToMap(s: EmergencyServiceEntity): Map<String, Any?> = mapOf(
        "id" to s.id,
        "name" to s.name,
        "type" to s.type,
        "phone" to s.phone,
        "address" to s.address,
        "distanceKm" to s.distanceKm.toDouble(),
        "is24Hours" to s.is24Hours,
        "description" to s.description
    )

    private fun docToEmergencyServiceEntity(docId: String, d: Map<String, Any?>): EmergencyServiceEntity = EmergencyServiceEntity(
        id = docId.toLongOrNull() ?: ((d["id"] as? Number)?.toLong()) ?: 0L,
        name = (d["name"] as? String) ?: "",
        type = (d["type"] as? String) ?: "HOSPITAL",
        phone = (d["phone"] as? String) ?: "",
        address = (d["address"] as? String) ?: "",
        distanceKm = ((d["distanceKm"] as? Number)?.toFloat()) ?: 1.2f,
        is24Hours = (d["is24Hours"] as? Boolean) ?: true,
        description = (d["description"] as? String) ?: ""
    )
}

package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    USER,
    DOCTOR,
    ADMIN
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val fullName: String,
    val phone: String = "",
    val dob: String = "",
    val gender: String = "Not Specified",
    val bloodGroup: String = "O+",
    val address: String = "",
    val emergencyContact: String = "",
    val emergencyPhone: String = "",
    val medicalNotes: String = "",
    val role: String = "USER",
    val avatarUrl: String = "",
    val isVerifiedDoctor: Boolean = false,
    val isActive: Boolean = true
)

@Entity(tableName = "health_records")
data class HealthRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String,
    val weightKg: Float = 0f,
    val heightCm: Float = 0f,
    val systolic: Int = 120,
    val diastolic: Int = 80,
    val heartRate: Int = 72,
    val bloodSugarMg: Float = 95f,
    val temperatureC: Float = 36.6f,
    val waterMl: Int = 1500,
    val sleepHours: Float = 7.5f,
    val steps: Int = 6000,
    val exerciseMinutes: Int = 30,
    val mood: String = "Good", // Great, Good, Calm, Tired, Stressed, Unwell
    val notes: String = ""
) {
    val bmi: Float
        get() = if (heightCm > 0) weightKg / ((heightCm / 100f) * (heightCm / 100f)) else 0f

    val bmiCategory: String
        get() = when {
            bmi <= 0f -> "Unknown"
            bmi < 18.5f -> "Underweight"
            bmi < 24.9f -> "Normal Weight"
            bmi < 29.9f -> "Overweight"
            else -> "Obese"
        }
}

@Entity(tableName = "medicines")
data class MedicineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val name: String,
    val dosage: String,
    val frequency: String, // "Once Daily", "Twice Daily", "Three Times Daily", "As Needed"
    val timeOfDay: String, // "08:00 AM", "01:00 PM", "08:00 PM"
    val startDate: String,
    val endDate: String,
    val instructions: String, // "After meal", "Before meal", "With water"
    val notes: String = "",
    val isActive: Boolean = true,
    val isTakenToday: Boolean = false,
    val lastTakenTimestamp: Long = 0L
)

@Entity(tableName = "vaccinations")
data class VaccinationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val vaccineName: String,
    val doseNumber: String, // "Dose 1", "Dose 2", "Booster"
    val dateReceived: String,
    val nextDoseDate: String = "",
    val doctorOrHospital: String,
    val status: String = "COMPLETED", // "COMPLETED", "UPCOMING", "OVERDUE"
    val notes: String = ""
)

@Entity(tableName = "medical_records")
data class MedicalRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val title: String,
    val doctorName: String,
    val hospitalClinic: String,
    val recordDate: String,
    val category: String, // "Prescription", "Lab Report", "Blood Test", "Radiology", "Doctor Notes", "Vaccination Certificate"
    val notes: String,
    val prescriptionText: String = "",
    val labResults: String = "",
    val fileAttachmentName: String = "report.pdf",
    val fileSizeBytes: Long = 1024 * 350
)

@Entity(tableName = "doctors")
data class DoctorEntity(
    @PrimaryKey val id: String,
    val name: String,
    val specialty: String,
    val qualifications: String,
    val experienceYears: Int,
    val hospital: String,
    val location: String,
    val consultationFee: Double,
    val rating: Float = 4.8f,
    val reviewCount: Int = 124,
    val bio: String,
    val phone: String,
    val email: String,
    val isVerified: Boolean = true,
    val availableDays: String = "Mon - Fri",
    val availableSlots: String = "09:00 AM, 10:30 AM, 02:00 PM, 04:30 PM",
    val avatarUrl: String = ""
)

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patientId: String,
    val patientName: String,
    val patientPhone: String = "",
    val doctorId: String,
    val doctorName: String,
    val doctorSpecialty: String,
    val date: String,
    val timeSlot: String,
    val symptoms: String,
    val notes: String = "",
    val status: String = "CONFIRMED", // "PENDING", "CONFIRMED", "COMPLETED", "CANCELLED", "REJECTED"
    val doctorPrescription: String = "",
    val consultationNotes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "community_posts")
data class CommunityPostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val authorId: String,
    val authorName: String,
    val authorRole: String = "USER",
    val title: String,
    val content: String,
    val category: String, // "Health Question", "Experience", "Wellness Tips", "Awareness", "Discussion"
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val isLikedByMe: Boolean = false,
    val isReported: Boolean = false
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val postId: Long,
    val authorId: String,
    val authorName: String,
    val authorRole: String = "USER",
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderId: String,
    val senderName: String,
    val senderRole: String = "USER",
    val channelId: String = "general", // "general", "chronic_care", "nutrition", "doctor_qa"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "blood_donors")
data class BloodDonorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val name: String,
    val bloodGroup: String, // "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
    val phone: String,
    val location: String,
    val isAvailable: Boolean = true,
    val lastDonationDate: String = "2026-04-10"
)

@Entity(tableName = "blood_requests")
data class BloodRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val requesterId: String,
    val patientName: String,
    val bloodGroup: String,
    val unitsRequired: Int,
    val hospital: String,
    val location: String,
    val contactPhone: String,
    val requiredDate: String,
    val urgency: String = "HIGH", // "CRITICAL", "HIGH", "NORMAL"
    val status: String = "ACTIVE", // "ACTIVE", "FULFILLED", "CANCELLED"
    val description: String = ""
)

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val excerpt: String,
    val content: String,
    val category: String, // "Nutrition & Diet", "Mental Wellness", "Heart Health", "Preventive Care", "Fitness & Mobility", "Sleep Science"
    val author: String,
    val readTimeMinutes: Int = 4,
    val date: String,
    val imageUrl: String = "",
    val isPublished: Boolean = true
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val title: String,
    val message: String,
    val type: String, // "APPOINTMENT", "MEDICINE", "VACCINE", "BLOOD", "COMMUNITY", "SYSTEM"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "emergency_services")
data class EmergencyServiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // "HOSPITAL", "AMBULANCE", "HOTLINE", "TRAUMA"
    val phone: String,
    val address: String,
    val distanceKm: Float = 1.2f,
    val is24Hours: Boolean = true,
    val description: String = ""
)

data class AiChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "user" or "ai"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isEmergency: Boolean = false
)

data class DietPlanResult(
    val goal: String,
    val targetCalories: Int,
    val targetWaterLiters: Float,
    val macros: String,
    val breakfast: String,
    val lunch: String,
    val dinner: String,
    val snacks: String,
    val hydrationTips: String,
    val nutritionalAdvice: String
)

data class ExtractedMedicine(
    val name: String,
    val dosage: String,
    val dosageForm: String = "Tablet", // Tablet, Capsule, Syrup, Drops, Inhaler, Ointment
    val frequency: String, // Once Daily, Twice Daily, Every 8 Hours, As Needed
    val duration: String = "7 Days",
    val instructions: String = "After meals",
    val warnings: String = ""
)

data class ExtractedLabItem(
    val testName: String,
    val resultValue: String,
    val unit: String,
    val referenceRange: String,
    val status: String = "NORMAL", // "NORMAL", "HIGH", "LOW", "ABNORMAL"
    val explanation: String
)

data class DocumentAnalysisResult(
    val type: String, // "PRESCRIPTION", "LAB_REPORT", "RADIOLOGY", "GENERAL_DOCUMENT"
    val documentTitle: String,
    val dateDetected: String = "2026-08-15",
    val doctorOrLabName: String = "",
    val patientName: String = "",
    val medicines: List<ExtractedMedicine> = emptyList(),
    val labItems: List<ExtractedLabItem> = emptyList(),
    val clinicalSummary: String = "",
    val keyWarnings: String = "",
    val rawExtractedText: String = "",
    val confidenceNote: String = "This AI-generated extraction is informational. Please verify with your prescribing physician or clinical laboratory."
)


package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        UserEntity::class,
        HealthRecordEntity::class,
        MedicineEntity::class,
        VaccinationEntity::class,
        MedicalRecordEntity::class,
        DoctorEntity::class,
        AppointmentEntity::class,
        CommunityPostEntity::class,
        CommentEntity::class,
        ChatMessageEntity::class,
        BloodDonorEntity::class,
        BloodRequestEntity::class,
        ArticleEntity::class,
        NotificationEntity::class,
        EmergencyServiceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun healthDao(): HealthDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_health_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

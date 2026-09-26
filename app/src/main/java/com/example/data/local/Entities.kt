package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey
    val studentId: String, // Unique school ID e.g. PSBS-1001
    val name: String,
    val rollNo: String,
    val className: String, // e.g. Class 10-A
    val phoneNumber: String,
    val email: String = "", // Filled during student activation
    val profilePhotoUri: String? = null,
    val isActivated: Boolean = false,
    val academicYearBs: String = "2081 BS",
    val status: String = "ACTIVE", // ACTIVE, DEACTIVATED, ARCHIVED
    val registeredDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "teachers")
data class TeacherEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fullName: String,
    val generatedCode: String, // Auto-generated code without special characters
    val subject: String = "General",
    val email: String = "",
    val phoneNumber: String = "",
    val isActivated: Boolean = false,
    val joinedDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "admins")
data class AdminEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val email: String,
    val role: String, // "SUPER_ADMIN" or "ADMIN"
    val status: String, // "APPROVED", "PENDING", "REJECTED"
    val requestTimestamp: Long = System.currentTimeMillis(),
    val approvedBy: String? = null
)

@Entity(tableName = "notices")
data class NoticeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val category: String, // "ACADEMIC", "EXAM", "CIRCULAR", "EVENT"
    val content: String,
    val dateBs: String, // e.g. "Ashwin 12, 2081 BS"
    val postedBy: String = "Administration",
    val isUrgent: Boolean = false
)

@Entity(tableName = "applications")
data class ApplicationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val studentId: String,
    val studentName: String,
    val studentClass: String,
    val studentEmail: String,
    val applicationType: String, // "LEAVE", "CHARACTER_CERTIFICATE", "TRANSFER_CERTIFICATE", "LIBRARY_CLEARANCE"
    val reason: String,
    val fromDate: String = "",
    val toDate: String = "",
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val submittedTimestamp: Long = System.currentTimeMillis(),
    val remarks: String? = null
)

@Entity(tableName = "student_progress")
data class StudentProgressEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val studentId: String,
    val subjectName: String,
    val completedUnits: Int,
    val totalUnits: Int,
    val scorePercentage: Int,
    val grade: String,
    val learningPace: String = "BALANCED", // "ACCELERATED", "BALANCED", "NEEDS_FOUNDATION"
    val lastStudyTopic: String = "",
    val updatedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "ai_chat_messages")
data class AiChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val studentId: String,
    val sender: String, // "STUDENT" or "AI"
    val message: String,
    val subjectContext: String = "General",
    val timestamp: Long = System.currentTimeMillis()
)

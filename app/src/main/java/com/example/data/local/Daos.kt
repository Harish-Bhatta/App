package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY registeredDate DESC")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE studentId = :studentId LIMIT 1")
    suspend fun getStudentById(studentId: String): StudentEntity?

    @Query("SELECT * FROM students WHERE studentId = :studentId LIMIT 1")
    fun getStudentFlow(studentId: String): Flow<StudentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<StudentEntity>)

    @Update
    suspend fun updateStudent(student: StudentEntity)

    // Account deleted automatically when admin removes student ID
    @Query("DELETE FROM students WHERE studentId = :studentId")
    suspend fun deleteStudentById(studentId: String)

    // Year-end deactivation per BS calendar
    @Query("UPDATE students SET status = 'DEACTIVATED' WHERE academicYearBs = :yearBs")
    suspend fun deactivateByYearBs(yearBs: String)

    @Query("SELECT COUNT(*) FROM students")
    suspend fun getStudentCount(): Int
}

@Dao
interface TeacherDao {
    @Query("SELECT * FROM teachers ORDER BY id ASC")
    fun getAllTeachers(): Flow<List<TeacherEntity>>

    @Query("SELECT * FROM teachers WHERE LOWER(TRIM(fullName)) = LOWER(TRIM(:name)) LIMIT 1")
    suspend fun getTeacherByName(name: String): TeacherEntity?

    @Query("SELECT * FROM teachers WHERE id = :id LIMIT 1")
    suspend fun getTeacherById(id: Int): TeacherEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: TeacherEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeachers(teachers: List<TeacherEntity>)

    @Update
    suspend fun updateTeacher(teacher: TeacherEntity)

    @Query("DELETE FROM teachers WHERE id = :id")
    suspend fun deleteTeacher(id: Int)

    @Query("SELECT COUNT(*) FROM teachers")
    suspend fun getTeacherCount(): Int
}

@Dao
interface AdminDao {
    @Query("SELECT * FROM admins ORDER BY requestTimestamp ASC")
    fun getAllAdmins(): Flow<List<AdminEntity>>

    @Query("SELECT * FROM admins WHERE role = 'SUPER_ADMIN' AND status = 'APPROVED' LIMIT 1")
    suspend fun getSuperAdmin(): AdminEntity?

    @Query("SELECT * FROM admins WHERE role = 'SUPER_ADMIN' LIMIT 1")
    fun getSuperAdminFlow(): Flow<AdminEntity?>

    @Query("SELECT * FROM admins WHERE LOWER(TRIM(email)) = LOWER(TRIM(:email)) LIMIT 1")
    suspend fun getAdminByEmail(email: String): AdminEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdmin(admin: AdminEntity): Long

    @Update
    suspend fun updateAdmin(admin: AdminEntity)

    @Query("UPDATE admins SET status = :status, approvedBy = :approvedBy WHERE id = :id")
    suspend fun updateAdminStatus(id: Int, status: String, approvedBy: String)

    @Query("DELETE FROM admins WHERE id = :id")
    suspend fun deleteAdmin(id: Int)

    @Query("SELECT COUNT(*) FROM admins WHERE status = 'PENDING'")
    fun getPendingRequestsCountFlow(): Flow<Int>
}

@Dao
interface NoticeDao {
    @Query("SELECT * FROM notices ORDER BY id DESC")
    fun getAllNotices(): Flow<List<NoticeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotice(notice: NoticeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotices(notices: List<NoticeEntity>)

    @Query("DELETE FROM notices WHERE id = :id")
    suspend fun deleteNotice(id: Int)
}

@Dao
interface ApplicationDao {
    @Query("SELECT * FROM applications WHERE studentId = :studentId ORDER BY submittedTimestamp DESC")
    fun getApplicationsByStudent(studentId: String): Flow<List<ApplicationEntity>>

    @Query("SELECT * FROM applications ORDER BY submittedTimestamp DESC")
    fun getAllApplications(): Flow<List<ApplicationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplication(application: ApplicationEntity)

    @Update
    suspend fun updateApplication(application: ApplicationEntity)

    @Query("UPDATE applications SET status = :status, remarks = :remarks WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String, remarks: String)

    @Query("DELETE FROM applications WHERE studentId = :studentId")
    suspend fun deleteApplicationsByStudent(studentId: String)
}

@Dao
interface StudentProgressDao {
    @Query("SELECT * FROM student_progress WHERE studentId = :studentId ORDER BY id ASC")
    fun getProgressByStudent(studentId: String): Flow<List<StudentProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: StudentProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgressList(list: List<StudentProgressEntity>)

    @Update
    suspend fun updateProgress(progress: StudentProgressEntity)

    @Query("DELETE FROM student_progress WHERE studentId = :studentId")
    suspend fun deleteProgressByStudent(studentId: String)

    @Query("UPDATE student_progress SET learningPace = :pace WHERE studentId = :studentId")
    suspend fun updateLearningPaceForStudent(studentId: String, pace: String)
}

@Dao
interface AiChatMessageDao {
    @Query("SELECT * FROM ai_chat_messages WHERE studentId = :studentId ORDER BY timestamp ASC")
    fun getChatMessages(studentId: String): Flow<List<AiChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: AiChatMessageEntity)

    @Query("DELETE FROM ai_chat_messages WHERE studentId = :studentId")
    suspend fun clearChatForStudent(studentId: String)
}

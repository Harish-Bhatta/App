package com.example.data.repository

import com.example.data.local.*
import com.example.util.FormulaUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SchoolRepository(private val db: AppDatabase) {

    private val studentDao = db.studentDao()
    private val teacherDao = db.teacherDao()
    private val adminDao = db.adminDao()
    private val noticeDao = db.noticeDao()
    private val applicationDao = db.applicationDao()
    private val progressDao = db.studentProgressDao()
    private val aiChatDao = db.aiChatMessageDao()

    // --- Students ---
    val allStudents: Flow<List<StudentEntity>> = studentDao.getAllStudents()

    suspend fun getStudentById(id: String): StudentEntity? = withContext(Dispatchers.IO) {
        studentDao.getStudentById(id.trim().uppercase())
    }

    fun getStudentFlow(id: String): Flow<StudentEntity?> = studentDao.getStudentFlow(id.trim().uppercase())

    suspend fun registerStudent(
        id: String,
        name: String,
        rollNo: String,
        className: String,
        phoneNumber: String
    ): StudentEntity = withContext(Dispatchers.IO) {
        val student = StudentEntity(
            studentId = id.trim().uppercase(),
            name = name.trim(),
            rollNo = rollNo.trim(),
            className = className.trim(),
            phoneNumber = phoneNumber.trim(),
            isActivated = false
        )
        studentDao.insertStudent(student)

        // Seed default subject progress
        val defaultSubjects = listOf("Mathematics", "Science", "Computer Science", "English")
        val progressList = defaultSubjects.map { subject ->
            StudentProgressEntity(
                studentId = student.studentId,
                subjectName = subject,
                completedUnits = 2,
                totalUnits = 10,
                scorePercentage = 75,
                grade = "B+",
                learningPace = "BALANCED",
                lastStudyTopic = "Introduction to $subject"
            )
        }
        progressDao.insertProgressList(progressList)
        student
    }

    suspend fun activateStudent(
        studentId: String,
        email: String,
        profilePhotoUri: String?
    ): Boolean = withContext(Dispatchers.IO) {
        val existing = studentDao.getStudentById(studentId.trim().uppercase()) ?: return@withContext false
        val updated = existing.copy(
            email = email.trim(),
            profilePhotoUri = profilePhotoUri,
            isActivated = true,
            status = "ACTIVE"
        )
        studentDao.updateStudent(updated)
        true
    }

    suspend fun deleteStudent(studentId: String) = withContext(Dispatchers.IO) {
        val cleanId = studentId.trim().uppercase()
        studentDao.deleteStudentById(cleanId)
        progressDao.deleteProgressByStudent(cleanId)
        applicationDao.deleteApplicationsByStudent(cleanId)
        aiChatDao.clearChatForStudent(cleanId)
    }

    suspend fun triggerBsYearEndDeactivation(yearBs: String) = withContext(Dispatchers.IO) {
        studentDao.deactivateByYearBs(yearBs)
    }

    // --- Teachers ---
    val allTeachers: Flow<List<TeacherEntity>> = teacherDao.getAllTeachers()

    suspend fun findTeacherByName(fullName: String): TeacherEntity? = withContext(Dispatchers.IO) {
        teacherDao.getTeacherByName(fullName.trim())
    }

    suspend fun verifyTeacherLogin(fullName: String, code: String): TeacherEntity? = withContext(Dispatchers.IO) {
        val teacher = teacherDao.getTeacherByName(fullName.trim()) ?: return@withContext null
        if (teacher.generatedCode.equals(code.trim(), ignoreCase = true)) {
            if (!teacher.isActivated) {
                teacherDao.updateTeacher(teacher.copy(isActivated = true))
            }
            teacher
        } else {
            null
        }
    }

    suspend fun registerTeacher(fullName: String, subject: String): TeacherEntity = withContext(Dispatchers.IO) {
        val code = FormulaUtils.generateTeacherCode(fullName)
        val teacher = TeacherEntity(
            fullName = fullName.trim(),
            generatedCode = code,
            subject = subject.trim().ifEmpty { "General Studies" },
            email = "${fullName.trim().lowercase().replace(Regex("[^a-z0-9]"), ".")}@psbs.edu.np",
            isActivated = false
        )
        val id = teacherDao.insertTeacher(teacher)
        teacher.copy(id = id.toInt())
    }

    suspend fun deleteTeacher(id: Int) = withContext(Dispatchers.IO) {
        teacherDao.deleteTeacher(id)
    }

    // --- Admins & Super Admin ---
    val allAdmins: Flow<List<AdminEntity>> = adminDao.getAllAdmins()
    val superAdminFlow: Flow<AdminEntity?> = adminDao.getSuperAdminFlow()

    suspend fun getSuperAdmin(): AdminEntity? = withContext(Dispatchers.IO) {
        adminDao.getSuperAdmin()
    }

    suspend fun setupSuperAdmin(name: String, email: String): AdminEntity = withContext(Dispatchers.IO) {
        val existing = adminDao.getSuperAdmin()
        if (existing != null) return@withContext existing

        val superAdmin = AdminEntity(
            name = name.trim(),
            email = email.trim(),
            role = "SUPER_ADMIN",
            status = "APPROVED",
            approvedBy = "SYSTEM_INITIALIZER"
        )
        val id = adminDao.insertAdmin(superAdmin)
        superAdmin.copy(id = id.toInt())
    }

    suspend fun requestAdminAccess(name: String, email: String): AdminEntity = withContext(Dispatchers.IO) {
        val existing = adminDao.getAdminByEmail(email.trim())
        if (existing != null) {
            return@withContext existing
        }
        val newAdmin = AdminEntity(
            name = name.trim(),
            email = email.trim(),
            role = "ADMIN",
            status = "PENDING"
        )
        val id = adminDao.insertAdmin(newAdmin)
        newAdmin.copy(id = id.toInt())
    }

    suspend fun checkAdminStatus(email: String): AdminEntity? = withContext(Dispatchers.IO) {
        adminDao.getAdminByEmail(email.trim())
    }

    suspend fun approveAdmin(id: Int, approverName: String) = withContext(Dispatchers.IO) {
        adminDao.updateAdminStatus(id, "APPROVED", approverName)
    }

    suspend fun rejectAdmin(id: Int, approverName: String) = withContext(Dispatchers.IO) {
        adminDao.updateAdminStatus(id, "REJECTED", approverName)
    }

    suspend fun deleteAdmin(id: Int) = withContext(Dispatchers.IO) {
        adminDao.deleteAdmin(id)
    }

    // --- Notices ---
    val allNotices: Flow<List<NoticeEntity>> = noticeDao.getAllNotices()

    suspend fun addNotice(
        title: String,
        category: String,
        content: String,
        dateBs: String,
        postedBy: String,
        isUrgent: Boolean
    ) = withContext(Dispatchers.IO) {
        noticeDao.insertNotice(
            NoticeEntity(
                title = title.trim(),
                category = category,
                content = content.trim(),
                dateBs = dateBs.ifBlank { FormulaUtils.getBsAcademicYearInfo().currentYear },
                postedBy = postedBy,
                isUrgent = isUrgent
            )
        )
    }

    suspend fun deleteNotice(id: Int) = withContext(Dispatchers.IO) {
        noticeDao.deleteNotice(id)
    }

    // --- Applications ---
    fun getApplicationsForStudent(studentId: String): Flow<List<ApplicationEntity>> =
        applicationDao.getApplicationsByStudent(studentId.trim().uppercase())

    val allApplications: Flow<List<ApplicationEntity>> = applicationDao.getAllApplications()

    suspend fun submitApplication(
        studentId: String,
        studentName: String,
        studentClass: String,
        email: String,
        type: String,
        reason: String,
        fromDate: String,
        toDate: String
    ) = withContext(Dispatchers.IO) {
        val app = ApplicationEntity(
            studentId = studentId.trim().uppercase(),
            studentName = studentName,
            studentClass = studentClass,
            studentEmail = email,
            applicationType = type,
            reason = reason.trim(),
            fromDate = fromDate,
            toDate = toDate,
            status = "PENDING"
        )
        applicationDao.insertApplication(app)
    }

    suspend fun updateApplicationStatus(id: Int, status: String, remarks: String) = withContext(Dispatchers.IO) {
        applicationDao.updateStatus(id, status, remarks)
    }

    // --- Progress Tracking ---
    fun getProgressForStudent(studentId: String): Flow<List<StudentProgressEntity>> =
        progressDao.getProgressByStudent(studentId.trim().uppercase())

    suspend fun updateLearningPace(studentId: String, pace: String) = withContext(Dispatchers.IO) {
        progressDao.updateLearningPaceForStudent(studentId.trim().uppercase(), pace)
    }

    suspend fun updateSubjectProgress(progress: StudentProgressEntity) = withContext(Dispatchers.IO) {
        progressDao.updateProgress(progress)
    }

    // --- AI Chat Messages ---
    fun getChatMessages(studentId: String): Flow<List<AiChatMessageEntity>> =
        aiChatDao.getChatMessages(studentId.trim().uppercase())

    suspend fun saveChatMessage(studentId: String, sender: String, message: String, context: String) =
        withContext(Dispatchers.IO) {
            aiChatDao.insertMessage(
                AiChatMessageEntity(
                    studentId = studentId.trim().uppercase(),
                    sender = sender,
                    message = message,
                    subjectContext = context
                )
            )
        }

    suspend fun clearChat(studentId: String) = withContext(Dispatchers.IO) {
        aiChatDao.clearChatForStudent(studentId.trim().uppercase())
    }
}

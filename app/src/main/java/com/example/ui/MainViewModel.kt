package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiTutoringService
import com.example.data.local.*
import com.example.data.repository.SchoolRepository
import com.example.util.FormulaUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class Screen {
    PUBLIC_PORTAL,
    ROLE_SELECTOR,
    STUDENT_LOGIN,
    TEACHER_LOGIN,
    ADMIN_LOGIN,
    STUDENT_WORKSPACE,
    TEACHER_WORKSPACE,
    ADMIN_WORKSPACE,
    SUPER_ADMIN_WORKSPACE
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = SchoolRepository(db)
    private val aiService = AiTutoringService()

    // Navigation and theme
    val currentScreen = MutableStateFlow(Screen.PUBLIC_PORTAL)
    val isDarkMode = MutableStateFlow(true) // Modern glass looks striking in dark mode by default

    // Logged in entities
    val loggedStudent = MutableStateFlow<StudentEntity?>(null)
    val loggedTeacher = MutableStateFlow<TeacherEntity?>(null)
    val loggedAdmin = MutableStateFlow<AdminEntity?>(null)

    // Data from DB
    val allStudents: StateFlow<List<StudentEntity>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTeachers: StateFlow<List<TeacherEntity>> = repository.allTeachers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAdmins: StateFlow<List<AdminEntity>> = repository.allAdmins
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val superAdmin: StateFlow<AdminEntity?> = repository.superAdminFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allNotices: StateFlow<List<NoticeEntity>> = repository.allNotices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allApplications: StateFlow<List<ApplicationEntity>> = repository.allApplications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active student data flows
    val studentProgress = loggedStudent
        .flatMapLatest { student ->
            if (student != null) repository.getProgressForStudent(student.studentId)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studentApplications = loggedStudent
        .flatMapLatest { student ->
            if (student != null) repository.getApplicationsForStudent(student.studentId)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val aiChatMessages = loggedStudent
        .flatMapLatest { student ->
            if (student != null) repository.getChatMessages(student.studentId)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Student Login flow state
    val studentIdInput = MutableStateFlow("")
    val studentStep = MutableStateFlow(1) // 1 = ID entry, 2 = locked verification & email input
    val studentCandidate = MutableStateFlow<StudentEntity?>(null)
    val studentEmailInput = MutableStateFlow("")
    val studentAvatarChoice = MutableStateFlow("avatar_1")
    val studentLoginError = MutableStateFlow<String?>(null)

    // Teacher Login flow state
    val teacherNameInput = MutableStateFlow("")
    val teacherCodeInput = MutableStateFlow("")
    val teacherStep = MutableStateFlow(1) // 1 = Name check, 2 = Code entry
    val teacherCandidate = MutableStateFlow<TeacherEntity?>(null)
    val teacherLoginError = MutableStateFlow<String?>(null)

    // Admin Login flow state
    val adminNameInput = MutableStateFlow("")
    val adminEmailInput = MutableStateFlow("")
    val adminStatusNotice = MutableStateFlow<String?>(null)

    // AI Tutor state
    val aiQueryInput = MutableStateFlow("")
    val isAiThinking = MutableStateFlow(false)
    val selectedSubject = MutableStateFlow("Mathematics")
    val selectedLearningPace = MutableStateFlow("BALANCED") // "ACCELERATED", "BALANCED", "NEEDS_FOUNDATION"

    // Application dialog state
    val appTypeInput = MutableStateFlow("LEAVE")
    val appReasonInput = MutableStateFlow("")
    val appFromDateInput = MutableStateFlow("")
    val appToDateInput = MutableStateFlow("")

    // Admin management dialog inputs
    val newStudentId = MutableStateFlow("")
    val newStudentName = MutableStateFlow("")
    val newStudentRoll = MutableStateFlow("")
    val newStudentClass = MutableStateFlow("Class 10-A")
    val newStudentPhone = MutableStateFlow("")

    val newTeacherName = MutableStateFlow("")
    val newTeacherSubject = MutableStateFlow("")

    val newNoticeTitle = MutableStateFlow("")
    val newNoticeCategory = MutableStateFlow("ACADEMIC")
    val newNoticeContent = MutableStateFlow("")
    val newNoticeUrgent = MutableStateFlow(false)

    init {
        // Monitor student account validity: If admin removes student from DB, logout immediately
        viewModelScope.launch {
            loggedStudent.collect { student ->
                if (student != null) {
                    val exists = repository.getStudentById(student.studentId)
                    if (exists == null) {
                        loggedStudent.value = null
                        if (currentScreen.value == Screen.STUDENT_WORKSPACE) {
                            currentScreen.value = Screen.PUBLIC_PORTAL
                        }
                    }
                }
            }
        }
    }

    // --- Student Auth Actions ---
    fun verifyStudentId() {
        val id = studentIdInput.value.trim().uppercase()
        if (id.isBlank()) {
            studentLoginError.value = "Please enter your Student ID"
            return
        }
        viewModelScope.launch {
            val student = repository.getStudentById(id)
            if (student == null) {
                studentLoginError.value = "Student ID not found in school database. Please contact school admin."
            } else if (student.status == "DEACTIVATED") {
                studentLoginError.value = "Account deactivated per BS Academic Year-End policy. Please contact administration."
            } else {
                studentCandidate.value = student
                studentEmailInput.value = student.email
                studentStep.value = 2
                studentLoginError.value = null
            }
        }
    }

    fun completeStudentActivation() {
        val student = studentCandidate.value ?: return
        val email = studentEmailInput.value.trim()
        if (email.isBlank() || !email.contains("@")) {
            studentLoginError.value = "Valid email address is compulsory for registration & applications"
            return
        }
        viewModelScope.launch {
            repository.activateStudent(
                studentId = student.studentId,
                email = email,
                profilePhotoUri = studentAvatarChoice.value
            )
            val updated = repository.getStudentById(student.studentId)
            loggedStudent.value = updated
            studentLoginError.value = null
            studentStep.value = 1
            studentCandidate.value = null
            studentIdInput.value = ""
            currentScreen.value = Screen.STUDENT_WORKSPACE
        }
    }

    fun resetStudentLogin() {
        studentStep.value = 1
        studentCandidate.value = null
        studentLoginError.value = null
        studentIdInput.value = ""
        studentEmailInput.value = ""
    }

    // --- Teacher Auth Actions ---
    fun verifyTeacherName() {
        val name = teacherNameInput.value.trim()
        if (name.isBlank()) {
            teacherLoginError.value = "Please enter your full name as registered"
            return
        }
        viewModelScope.launch {
            val teacher = repository.findTeacherByName(name)
            if (teacher == null) {
                teacherLoginError.value = "Teacher name not found in school database. Admin must register you first."
            } else {
                teacherCandidate.value = teacher
                teacherStep.value = 2
                teacherLoginError.value = null
            }
        }
    }

    fun verifyTeacherCode() {
        val candidate = teacherCandidate.value ?: return
        val code = teacherCodeInput.value.trim()
        if (code.isBlank()) {
            teacherLoginError.value = "Please enter your formula security code"
            return
        }
        viewModelScope.launch {
            val verified = repository.verifyTeacherLogin(candidate.fullName, code)
            if (verified != null) {
                loggedTeacher.value = verified
                teacherLoginError.value = null
                teacherStep.value = 1
                teacherCandidate.value = null
                teacherNameInput.value = ""
                teacherCodeInput.value = ""
                currentScreen.value = Screen.TEACHER_WORKSPACE
            } else {
                teacherLoginError.value = "Incorrect security code. Verify with administration or use your formula code."
            }
        }
    }

    fun resetTeacherLogin() {
        teacherStep.value = 1
        teacherCandidate.value = null
        teacherLoginError.value = null
        teacherNameInput.value = ""
        teacherCodeInput.value = ""
    }

    // --- Admin & Super Admin Auth Actions ---
    fun processAdminLogin() {
        val name = adminNameInput.value.trim()
        val email = adminEmailInput.value.trim()

        if (name.isBlank() || email.isBlank() || !email.contains("@")) {
            adminStatusNotice.value = "Please enter both valid Name and Email address."
            return
        }

        viewModelScope.launch {
            val currentSuperAdmin = repository.getSuperAdmin()
            if (currentSuperAdmin == null) {
                // One-time Super Admin Lifeline Setup!
                val superAdmin = repository.setupSuperAdmin(name, email)
                loggedAdmin.value = superAdmin
                adminStatusNotice.value = null
                adminNameInput.value = ""
                adminEmailInput.value = ""
                currentScreen.value = Screen.SUPER_ADMIN_WORKSPACE
            } else {
                // If it's the super admin logging in with their email
                if (currentSuperAdmin.email.equals(email, ignoreCase = true)) {
                    loggedAdmin.value = currentSuperAdmin
                    adminStatusNotice.value = null
                    adminNameInput.value = ""
                    adminEmailInput.value = ""
                    currentScreen.value = Screen.SUPER_ADMIN_WORKSPACE
                    return@launch
                }

                // Check other admins
                val existing = repository.checkAdminStatus(email)
                if (existing == null) {
                    // Send access request to Super Admin & Admins
                    repository.requestAdminAccess(name, email)
                    adminStatusNotice.value = "Request submitted! Access pending approval by Super Admin or active Administrator."
                } else {
                    when (existing.status) {
                        "APPROVED" -> {
                            loggedAdmin.value = existing
                            adminStatusNotice.value = null
                            adminNameInput.value = ""
                            adminEmailInput.value = ""
                            currentScreen.value = Screen.ADMIN_WORKSPACE
                        }
                        "PENDING" -> {
                            adminStatusNotice.value = "Your admin request is pending review by the Super Admin."
                        }
                        "REJECTED" -> {
                            adminStatusNotice.value = "Your request was declined. Contact the Super Admin for clearance."
                        }
                    }
                }
            }
        }
    }

    fun approveAdminRequest(adminId: Int) {
        val approver = loggedAdmin.value?.name ?: "Super Admin"
        viewModelScope.launch {
            repository.approveAdmin(adminId, approver)
        }
    }

    fun rejectAdminRequest(adminId: Int) {
        val approver = loggedAdmin.value?.name ?: "Super Admin"
        viewModelScope.launch {
            repository.rejectAdmin(adminId, approver)
        }
    }

    fun deleteAdminRecord(adminId: Int) {
        viewModelScope.launch {
            repository.deleteAdmin(adminId)
        }
    }

    // --- Student Workspace Actions ---
    fun updatePace(pace: String) {
        selectedLearningPace.value = pace
        val student = loggedStudent.value ?: return
        viewModelScope.launch {
            repository.updateLearningPace(student.studentId, pace)
        }
    }

    fun sendAiQuestion() {
        val question = aiQueryInput.value.trim()
        if (question.isBlank()) return
        val student = loggedStudent.value ?: return

        aiQueryInput.value = ""
        isAiThinking.value = true

        viewModelScope.launch {
            // Save student message in DB
            repository.saveChatMessage(
                studentId = student.studentId,
                sender = "STUDENT",
                message = question,
                context = selectedSubject.value
            )

            // Get AI response
            val response = aiService.askTutor(
                studentName = student.name,
                className = student.className,
                learningPace = selectedLearningPace.value,
                subject = selectedSubject.value,
                question = question
            )

            // Save AI reply in DB
            repository.saveChatMessage(
                studentId = student.studentId,
                sender = "AI",
                message = response,
                context = selectedSubject.value
            )

            isAiThinking.value = false
        }
    }

    fun clearAiChat() {
        val student = loggedStudent.value ?: return
        viewModelScope.launch {
            repository.clearChat(student.studentId)
        }
    }

    fun submitStudentApplication() {
        val student = loggedStudent.value ?: return
        val reason = appReasonInput.value.trim()
        if (reason.isBlank()) return

        viewModelScope.launch {
            repository.submitApplication(
                studentId = student.studentId,
                studentName = student.name,
                studentClass = student.className,
                email = student.email,
                type = appTypeInput.value,
                reason = reason,
                fromDate = appFromDateInput.value.ifBlank { "Kartik 10, 2081 BS" },
                toDate = appToDateInput.value.ifBlank { "Kartik 12, 2081 BS" }
            )
            appReasonInput.value = ""
            appFromDateInput.value = ""
            appToDateInput.value = ""
        }
    }

    // --- Admin Management Actions ---
    fun adminAddNewStudent() {
        val id = newStudentId.value.trim().uppercase()
        val name = newStudentName.value.trim()
        val roll = newStudentRoll.value.trim()
        val className = newStudentClass.value.trim()
        val phone = newStudentPhone.value.trim()

        if (id.isBlank() || name.isBlank() || roll.isBlank()) return

        viewModelScope.launch {
            repository.registerStudent(id, name, roll, className, phone)
            newStudentId.value = ""
            newStudentName.value = ""
            newStudentRoll.value = ""
            newStudentPhone.value = ""
        }
    }

    fun adminDeleteStudent(studentId: String) {
        viewModelScope.launch {
            repository.deleteStudent(studentId)
            // If the deleted student happens to be currently logged in, log them out
            if (loggedStudent.value?.studentId.equals(studentId, ignoreCase = true)) {
                loggedStudent.value = null
                currentScreen.value = Screen.PUBLIC_PORTAL
            }
        }
    }

    fun adminAddNewTeacher() {
        val name = newTeacherName.value.trim()
        val subject = newTeacherSubject.value.trim()
        if (name.isBlank()) return

        viewModelScope.launch {
            repository.registerTeacher(name, subject)
            newTeacherName.value = ""
            newTeacherSubject.value = ""
        }
    }

    fun adminDeleteTeacher(id: Int) {
        viewModelScope.launch {
            repository.deleteTeacher(id)
            if (loggedTeacher.value?.id == id) {
                loggedTeacher.value = null
                currentScreen.value = Screen.PUBLIC_PORTAL
            }
        }
    }

    fun adminAddNotice() {
        val title = newNoticeTitle.value.trim()
        val content = newNoticeContent.value.trim()
        if (title.isBlank() || content.isBlank()) return

        val poster = loggedAdmin.value?.name ?: loggedTeacher.value?.fullName ?: "Administration"
        viewModelScope.launch {
            repository.addNotice(
                title = title,
                category = newNoticeCategory.value,
                content = content,
                dateBs = "Ashwin 15, 2081 BS",
                postedBy = poster,
                isUrgent = newNoticeUrgent.value
            )
            newNoticeTitle.value = ""
            newNoticeContent.value = ""
            newNoticeUrgent.value = false
        }
    }

    fun adminDeleteNotice(id: Int) {
        viewModelScope.launch {
            repository.deleteNotice(id)
        }
    }

    fun updateApplicationStatus(id: Int, status: String, remarks: String) {
        viewModelScope.launch {
            repository.updateApplicationStatus(id, status, remarks)
        }
    }

    fun triggerYearEndRollover() {
        viewModelScope.launch {
            repository.triggerBsYearEndDeactivation("2081 BS")
        }
    }

    fun logout() {
        loggedStudent.value = null
        loggedTeacher.value = null
        loggedAdmin.value = null
        currentScreen.value = Screen.PUBLIC_PORTAL
    }

    fun navigateTo(screen: Screen) {
        currentScreen.value = screen
    }
}

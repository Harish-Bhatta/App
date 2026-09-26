package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.util.FormulaUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        StudentEntity::class,
        TeacherEntity::class,
        AdminEntity::class,
        NoticeEntity::class,
        ApplicationEntity::class,
        StudentProgressEntity::class,
        AiChatMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun studentDao(): StudentDao
    abstract fun teacherDao(): TeacherDao
    abstract fun adminDao(): AdminDao
    abstract fun noticeDao(): NoticeDao
    abstract fun applicationDao(): ApplicationDao
    abstract fun studentProgressDao(): StudentProgressDao
    abstract fun aiChatMessageDao(): AiChatMessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "psbs_learning_db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate starter school records in background
                            CoroutineScope(Dispatchers.IO).launch {
                                populateInitialData(getInstance(context))
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            // Seed sample students ready for activation & testing
            val initialStudents = listOf(
                StudentEntity(
                    studentId = "PSBS-1001",
                    name = "Aarav Sharma",
                    rollNo = "1001",
                    className = "Class 10-A",
                    phoneNumber = "+977 9841234567",
                    email = "", // pending activation
                    profilePhotoUri = null,
                    isActivated = false,
                    academicYearBs = "2081 BS",
                    status = "ACTIVE"
                ),
                StudentEntity(
                    studentId = "PSBS-1002",
                    name = "Pooja Shrestha",
                    rollNo = "1002",
                    className = "Class 10-A",
                    phoneNumber = "+977 9851098765",
                    email = "pooja.shrestha@psbs.edu.np",
                    profilePhotoUri = null,
                    isActivated = true,
                    academicYearBs = "2081 BS",
                    status = "ACTIVE"
                ),
                StudentEntity(
                    studentId = "PSBS-1003",
                    name = "Rohan Karki",
                    rollNo = "1003",
                    className = "Class 10-B",
                    phoneNumber = "+977 9813245678",
                    email = "",
                    profilePhotoUri = null,
                    isActivated = false,
                    academicYearBs = "2081 BS",
                    status = "ACTIVE"
                )
            )
            db.studentDao().insertStudents(initialStudents)

            // Seed sample teachers with auto-generated formula code
            val teacherNames = listOf(
                Pair("Dr. Kiran Adhikari", "Mathematics & Physics"),
                Pair("Sunita Thapa", "Computer Science"),
                Pair("Bikash Pokharel", "English Literature")
            )
            val teacherEntities = teacherNames.map { (name, subject) ->
                TeacherEntity(
                    fullName = name,
                    generatedCode = FormulaUtils.generateTeacherCode(name),
                    subject = subject,
                    email = "${name.lowercase().replace(" ", ".")}@psbs.edu.np",
                    isActivated = true
                )
            }
            db.teacherDao().insertTeachers(teacherEntities)

            // Seed school notices
            val sampleNotices = listOf(
                NoticeEntity(
                    title = "Mid-Term Examination Schedule - 2081 BS",
                    category = "EXAM",
                    content = "The second terminal examinations for classes 9 and 10 will commence from Kartik 18, 2081 BS. Practical tests will take place earlier.",
                    dateBs = "Ashwin 12, 2081 BS",
                    postedBy = "Academic Council",
                    isUrgent = true
                ),
                NoticeEntity(
                    title = "Annual Science & STEM Robotics Exhibition",
                    category = "EVENT",
                    content = "Students participating in AI & Robotics club are requested to submit project prototypes to the Department of Computer Science by Friday.",
                    dateBs = "Ashwin 10, 2081 BS",
                    postedBy = "STEM Club",
                    isUrgent = false
                ),
                NoticeEntity(
                    title = "Notice regarding Dashain & Tihar Vacation",
                    category = "CIRCULAR",
                    content = "School will observe autumn break starting from Ghatasthapana. Classes will resume after Chhath Puja.",
                    dateBs = "Ashwin 08, 2081 BS",
                    postedBy = "Principal Office",
                    isUrgent = false
                )
            )
            db.noticeDao().insertNotices(sampleNotices)

            // Seed initial student progress for PSBS-1001 & PSBS-1002
            val progressList = listOf(
                StudentProgressEntity(
                    studentId = "PSBS-1001",
                    subjectName = "Mathematics",
                    completedUnits = 7,
                    totalUnits = 10,
                    scorePercentage = 88,
                    grade = "A",
                    learningPace = "ACCELERATED",
                    lastStudyTopic = "Trigonometry & Quadratic Equations"
                ),
                StudentProgressEntity(
                    studentId = "PSBS-1001",
                    subjectName = "Science",
                    completedUnits = 6,
                    totalUnits = 9,
                    scorePercentage = 84,
                    grade = "A",
                    learningPace = "BALANCED",
                    lastStudyTopic = "Chemical Reactions & Heredity"
                ),
                StudentProgressEntity(
                    studentId = "PSBS-1001",
                    subjectName = "Computer Science",
                    completedUnits = 9,
                    totalUnits = 10,
                    scorePercentage = 95,
                    grade = "A+",
                    learningPace = "ACCELERATED",
                    lastStudyTopic = "Python Programming & Databases"
                ),
                StudentProgressEntity(
                    studentId = "PSBS-1001",
                    subjectName = "English",
                    completedUnits = 8,
                    totalUnits = 10,
                    scorePercentage = 82,
                    grade = "A",
                    learningPace = "BALANCED",
                    lastStudyTopic = "Formal Essay & Critical Analysis"
                )
            )
            db.studentProgressDao().insertProgressList(progressList)
        }
    }
}

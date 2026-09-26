package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.FormulaUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun testAppNameString() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("PSBS Learning", appName)
    }

    @Test
    fun testTeacherCodeFormulaHasNoSpecialCharacters() {
        val nameWithSpecialChars = "Dr. Ram-Prasad, Sharma + M.Sc."
        val code = FormulaUtils.generateTeacherCode(nameWithSpecialChars)

        // Must start with TCH
        assertTrue("Teacher code must start with TCH", code.startsWith("TCH"))

        // Strictly alphanumeric, NO special characters like . , + -
        val containsSpecialChars = code.any { !it.isLetterOrDigit() }
        assertFalse("Teacher code must NOT contain special characters", containsSpecialChars)

        // Must be deterministic
        val codeAgain = FormulaUtils.generateTeacherCode(nameWithSpecialChars)
        assertEquals(code, codeAgain)
    }

    @Test
    fun testBsAcademicYearInfo() {
        val bsInfo = FormulaUtils.getBsAcademicYearInfo()
        assertEquals("2081 BS", bsInfo.currentYear)
        assertTrue(bsInfo.isYearActive)
        assertEquals("Chaitra 30, 2081 BS", bsInfo.lastDay)
    }
}

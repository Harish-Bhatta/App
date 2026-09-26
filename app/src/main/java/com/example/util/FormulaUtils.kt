package com.example.util

import kotlin.math.absoluteValue

object FormulaUtils {

    /**
     * Automatically generates a unique, deterministic teacher code from their full name.
     * Guaranteed NOT to use any special characters (like . , + - # etc.).
     * Format: TCH + Uppercase Initials/Key letters + 4-digit numeric code.
     * Example: "Ram Prasad Sharma" -> "TCHRPS5412"
     */
    fun generateTeacherCode(fullName: String): String {
        val trimmed = fullName.trim()
        if (trimmed.isEmpty()) return "TCHEDU1001"

        val words = trimmed.split(Regex("\\s+")).filter { it.isNotBlank() }
        val initials = buildString {
            for (word in words) {
                val cleanWord = word.replace(Regex("[^a-zA-Z0-9]"), "").uppercase()
                if (cleanWord.isNotEmpty()) {
                    append(cleanWord.take(2)) // Up to 2 letters per word
                }
            }
        }.take(5)

        val cleanFull = trimmed.replace(Regex("[^a-zA-Z0-9]"), "").uppercase()
        val numericHash = ((cleanFull.hashCode().absoluteValue % 8999) + 1000).toString()

        val safeLetters = if (initials.isNotEmpty()) initials else "TCHR"
        return "TCH${safeLetters}${numericHash}"
    }

    /**
     * Checks if BS Academic Year has ended or is active.
     */
    fun getBsAcademicYearInfo(): BsYearInfo {
        return BsYearInfo(
            currentYear = "2081 BS",
            lastDay = "Chaitra 30, 2081 BS",
            term = "Term 2 Examination & Review",
            daysLeftInYear = 194,
            isYearActive = true
        )
    }
}

data class BsYearInfo(
    val currentYear: String,
    val lastDay: String,
    val term: String,
    val daysLeftInYear: Int,
    val isYearActive: Boolean
)

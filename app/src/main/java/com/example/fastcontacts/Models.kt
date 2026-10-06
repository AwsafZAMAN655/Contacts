package com.example.fastcontacts

private val WHITESPACE = Regex("\\s+")

internal fun normalizeText(s: String): String =
    s.trim().replace(WHITESPACE, " ").lowercase()

data class PhoneNumber(val number: String, val typeLabel: String)

class Contact(
    val id: Long,
    val lookupKey: String,
    val displayName: String,
    val firstName: String,
    val lastName: String,
    fullName: String,
    val phones: List<PhoneNumber>,
    val isStarred: Boolean = false
) {
    val firstLc: String = normalizeText(firstName)
    val lastLc: String = normalizeText(lastName)
    val fullLc: String = normalizeText(fullName)
    val phoneDigits: List<String> = phones.map { p -> p.number.filter { it.isDigit() } }
    val initial: String =
        displayName.trim().firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString() ?: "#"
}

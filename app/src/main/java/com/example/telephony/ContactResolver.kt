package com.example.telephony

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ContactInfo(
    val name: String?,
    val photoUri: String? = null,
    val normalizedNumber: String
)

object ContactResolver {

    /**
     * Normalizes a phone number for consistent grouping and comparison.
     */
    fun normalizePhoneNumber(number: String): String {
        val trimmed = number.trim()
        if (trimmed.isEmpty()) return ""
        // If it contains letters (business alphanumeric sender like "GOOGLE", "VK-SBI", "HDFCBK", "AMAZON"), preserve it as-is
        if (trimmed.any { it.isLetter() }) {
            return trimmed
        }
        // Numeric phone number
        val hasPlus = trimmed.startsWith("+")
        val digitsOnly = trimmed.filter { it.isDigit() }
        if (digitsOnly.isEmpty()) return trimmed
        return if (hasPlus) "+$digitsOnly" else digitsOnly
    }

    /**
     * Resolves contact name and avatar from Android ContactsContract.
     */
    suspend fun resolveContact(context: Context, rawNumber: String): ContactInfo = withContext(Dispatchers.IO) {
        val normalized = normalizePhoneNumber(rawNumber)
        if (normalized.isEmpty()) {
            return@withContext ContactInfo(name = null, photoUri = null, normalizedNumber = rawNumber)
        }

        // If it is an alphanumeric business name, use it directly as the display name if no contact exists
        val isBusinessOrSenderId = rawNumber.any { it.isLetter() }

        try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(rawNumber)
            )
            val projection = arrayOf(
                ContactsContract.PhoneLookup.DISPLAY_NAME,
                ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI
            )

            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                    val photoIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI)
                    val name = if (nameIndex >= 0) cursor.getString(nameIndex) else null
                    val photo = if (photoIndex >= 0) cursor.getString(photoIndex) else null
                    return@withContext ContactInfo(name = name ?: (if (isBusinessOrSenderId) rawNumber else null), photoUri = photo, normalizedNumber = normalized)
                }
            }
        } catch (_: SecurityException) {
            // Contacts permission not granted
        } catch (_: Exception) {
            // Ignore failure and fallback
        }

        return@withContext ContactInfo(
            name = if (isBusinessOrSenderId) rawNumber else null,
            photoUri = null,
            normalizedNumber = normalized
        )
    }

    /**
     * Searches all contacts on the device for the Compose New screen.
     */
    suspend fun searchContacts(context: Context, query: String): List<ContactInfo> = withContext(Dispatchers.IO) {
        val results = mutableListOf<ContactInfo>()
        try {
            val projection = arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI
            )
            val selection = if (query.isNotBlank()) {
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ? OR ${ContactsContract.CommonDataKinds.Phone.NUMBER} LIKE ?"
            } else null
            val selectionArgs = if (query.isNotBlank()) {
                arrayOf("%$query%", "%$query%")
            } else null

            context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC LIMIT 50"
            )?.use { cursor ->
                val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val photoIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)

                val seenNumbers = mutableSetOf<String>()
                while (cursor.moveToNext()) {
                    val name = if (nameIdx >= 0) cursor.getString(nameIdx) else null
                    val number = if (numberIdx >= 0) cursor.getString(numberIdx) else ""
                    val photo = if (photoIdx >= 0) cursor.getString(photoIdx) else null
                    val normalized = normalizePhoneNumber(number)
                    if (normalized.isNotBlank() && seenNumbers.add(normalized)) {
                        results.add(ContactInfo(name = name, photoUri = photo, normalizedNumber = normalized))
                    }
                }
            }
        } catch (_: Exception) {
            // Contacts permission not granted or query failed
        }
        results
    }
}

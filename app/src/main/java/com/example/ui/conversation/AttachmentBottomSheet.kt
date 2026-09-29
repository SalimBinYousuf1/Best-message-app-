package com.example.ui.conversation

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.local.AttachmentStorageManager
import com.example.data.local.entity.AttachmentType
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.liquidGlass
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentBottomSheet(
    onDismiss: () -> Unit,
    onAttachmentSelected: (String, AttachmentType, String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    // Android Zero-permission Photo Picker with automatic durable internal persistence
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val persistentPath = AttachmentStorageManager.persistAttachment(context, uri, "jpg")
                val target = persistentPath ?: uri.toString()
                onAttachmentSelected(target, AttachmentType.IMAGE, "Photo")
                onDismiss()
            }
        }
    }

    // Document Picker with durable internal persistence
    val docPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val persistentPath = AttachmentStorageManager.persistAttachment(context, uri, "bin")
                val target = persistentPath ?: uri.toString()
                onAttachmentSelected(target, AttachmentType.DOCUMENT, "Document")
                onDismiss()
            }
        }
    }

    // Contact Picker
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { contactUri: Uri? ->
        if (contactUri != null) {
            coroutineScope.launch {
                try {
                    var contactName: String? = null
                    var contactNumber: String? = null

                    context.contentResolver.query(
                        contactUri,
                        arrayOf(
                            ContactsContract.Contacts._ID,
                            ContactsContract.Contacts.DISPLAY_NAME,
                            ContactsContract.Contacts.HAS_PHONE_NUMBER
                        ),
                        null,
                        null,
                        null
                    )?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val idIdx = cursor.getColumnIndex(ContactsContract.Contacts._ID)
                            val nameIdx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                            val hasPhoneIdx = cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)

                            val contactId = if (idIdx >= 0) cursor.getString(idIdx) else null
                            contactName = if (nameIdx >= 0) cursor.getString(nameIdx) else null
                            val hasPhone = if (hasPhoneIdx >= 0) cursor.getInt(hasPhoneIdx) else 0

                            if (hasPhone > 0 && contactId != null) {
                                context.contentResolver.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                                    "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                                    arrayOf(contactId),
                                    null
                                )?.use { phoneCursor ->
                                    if (phoneCursor.moveToFirst()) {
                                        val numIdx = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                        if (numIdx >= 0) contactNumber = phoneCursor.getString(numIdx)
                                    }
                                }
                            }
                        }
                    }

                    val finalName = contactName ?: "Contact"
                    val finalNumber = contactNumber ?: ""
                    val contactPayload = if (finalNumber.isNotBlank()) {
                        "$finalName: $finalNumber"
                    } else {
                        finalName
                    }
                    onAttachmentSelected(contactUri.toString(), AttachmentType.CONTACT, contactPayload)
                    onDismiss()
                } catch (e: Exception) {
                    Toast.makeText(context, "Could not read contact info", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Location fetching helper
    val fetchLocationAndSend = {
        Toast.makeText(context, "Fetching exact location...", Toast.LENGTH_SHORT).show()
        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()

            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        val mapUrl = "https://maps.google.com/?q=${loc.latitude},${loc.longitude}"
                        val locLabel = "Location: ${loc.latitude.toString().take(8)}, ${loc.longitude.toString().take(8)}"
                        onAttachmentSelected(mapUrl, AttachmentType.LOCATION, locLabel)
                        onDismiss()
                    } else {
                        // Fallback to last known location
                        fusedClient.lastLocation.addOnSuccessListener { lastLoc ->
                            val lat = lastLoc?.latitude ?: 37.7749
                            val lng = lastLoc?.longitude ?: -122.4194
                            val mapUrl = "https://maps.google.com/?q=$lat,$lng"
                            val locLabel = "Location: ${lat.toString().take(8)}, ${lng.toString().take(8)}"
                            onAttachmentSelected(mapUrl, AttachmentType.LOCATION, locLabel)
                            onDismiss()
                        }.addOnFailureListener {
                            Toast.makeText(context, "Location unavailable", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to get location", Toast.LENGTH_SHORT).show()
                }
        } catch (_: SecurityException) {
            Toast.makeText(context, "Location permission required", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Location error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Location permission request launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            fetchLocationAndSend()
        } else {
            Toast.makeText(context, "Location permission is required to share your exact location", Toast.LENGTH_LONG).show()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Share & Attach",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AttachmentOptionItem(
                    icon = Icons.Default.Image,
                    label = "Photos",
                    color = SalimBlue,
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )

                AttachmentOptionItem(
                    icon = Icons.Default.Description,
                    label = "Files",
                    color = Color(0xFF10B981),
                    onClick = {
                        docPickerLauncher.launch("*/*")
                    }
                )

                AttachmentOptionItem(
                    icon = Icons.Default.Person,
                    label = "Contact",
                    color = Color(0xFFF59E0B),
                    onClick = {
                        contactPickerLauncher.launch(null)
                    }
                )

                AttachmentOptionItem(
                    icon = Icons.Default.LocationOn,
                    label = "Location",
                    color = Color(0xFFEF4444),
                    onClick = {
                        val finePerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                        val coarsePerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                        if (finePerm == PackageManager.PERMISSION_GRANTED || coarsePerm == PackageManager.PERMISSION_GRANTED) {
                            fetchLocationAndSend()
                        } else {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun AttachmentOptionItem(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .liquidGlass(shape = CircleShape, elevation = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

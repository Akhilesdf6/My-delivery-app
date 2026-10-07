package com.mydelivery.manager.utils

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class ScannedLabel(
    val rawAddressText: String,
    val possiblePhone: String?,
    val possiblePincode: String?,
    val possibleName: String?
)

/**
 * Offline aayi image-il ninnu text edukkunna function.
 */
suspend fun extractTextFromImage(context: Context, uri: Uri): ScannedLabel = suspendCancellableCoroutine { continuation ->
    try {
        val image = InputImage.fromFilePath(context, uri)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val fullText = visionText.text
                
                // Indian phone number (10 digits starting with 6-9) kandu pidikkan
                val phoneRegex = Regex("\\b[6-9]\\d{9}\\b")
                val phone = phoneRegex.find(fullText)?.value
                
                // 6 digit Pincode kandu pidikkan
                val pinRegex = Regex("\\b\\d{6}\\b")
                val pincode = pinRegex.find(fullText)?.value
                
                // Aadyathe line chilappol customer name aakam
                val name = visionText.textBlocks.firstOrNull()?.lines?.firstOrNull()?.text
                
                continuation.resume(ScannedLabel(fullText, phone, pincode, name))
            }
            .addOnFailureListener { e ->
                continuation.resumeWithException(e)
            }
    } catch (e: Exception) {
        continuation.resumeWithException(e)
    }
}

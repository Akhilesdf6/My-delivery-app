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
    val possibleName: String?,
    val possibleShipmentId: String?,
    val possibleOrderNo: String?,
    val possibleCodAmount: String?
)

/**
 * Purplle / courier label-il ninnu OCR text extract cheythu
 * common shipment fields identify cheyyunnu.
 *
 * Unknown information blank aayirikkum.
 * OCR invent cheyyilla.
 */
suspend fun extractTextFromImage(
    context: Context,
    uri: Uri
): ScannedLabel = suspendCancellableCoroutine { continuation ->

    try {
        val image = InputImage.fromFilePath(context, uri)
        val recognizer =
            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->

                val fullText = visionText.text
                val lines = fullText
                    .lines()
                    .map { it.trim() }
                    .filter { it.isNotBlank() }

                // -----------------------------
                // Phone
                // -----------------------------
                val phoneRegex = Regex("""(?<!\d)[6-9]\d{9}(?!\d)""")
                val phone = phoneRegex
                    .find(fullText)
                    ?.value

                // -----------------------------
                // Pincode
                // -----------------------------
                val pinRegex = Regex("""(?<!\d)\d{6}(?!\d)""")
                val pincode = pinRegex
                    .find(fullText)
                    ?.value

                // -----------------------------
                // Order No.
                // Example:
                // Order No.: 20261006118460293
                // -----------------------------
                val orderRegex = Regex(
                    """(?i)order\s*(?:no|number|id)?\s*[:\-]?\s*(\d{8,25})"""
                )

                val orderNo = orderRegex
                    .find(fullText)
                    ?.groupValues
                    ?.getOrNull(1)

                // -----------------------------
                // Shipment / tracking ID
                // Example:
                // PRK2373454
                // -----------------------------
                val shipmentRegex = Regex(
                    """\b[A-Z]{2,6}\d{6,15}\b"""
                )

                val shipmentId = shipmentRegex
                    .find(fullText.uppercase())
                    ?.value

                // -----------------------------
                // COD amount
                // Example:
                // Payment: COD / ₹ 521
                // -----------------------------
                val codRegex = Regex(
                    """(?i)(?:payment|cod)[^\d]{0,30}(?:₹|rs\.?|inr)?\s*([\d,]+(?:\.\d{1,2})?)"""
                )

                val codAmount = codRegex
                    .find(fullText)
                    ?.groupValues
                    ?.getOrNull(1)
                    ?.replace(",", "")

                // -----------------------------
                // Customer name
                //
                // Look for:
                // Ship To: Name
                // -----------------------------
                val shipToIndex = lines.indexOfFirst {
                    it.contains("ship to", ignoreCase = true)
                }

                var name: String? = null

                if (shipToIndex >= 0) {
                    val shipToLine = lines[shipToIndex]

                    val afterColon = shipToLine
                        .substringAfter(":", "")
                        .trim()

                    if (afterColon.isNotBlank()) {
                        name = afterColon
                    } else {
                        val next = lines
                            .getOrNull(shipToIndex + 1)
                            ?.trim()

                        if (!next.isNullOrBlank()) {
                            name = next
                        }
                    }
                }

                // Avoid using obvious labels as customer name.
                if (
                    name != null &&
                    (
                        name.contains("order", ignoreCase = true) ||
                        name.contains("logistics", ignoreCase = true) ||
                        name.contains("payment", ignoreCase = true) ||
                        name.contains("ship from", ignoreCase = true)
                    )
                ) {
                    name = null
                }

                // -----------------------------
                // Address
                //
                // Collect text between Ship To
                // and Ship From / Logistics.
                // -----------------------------
                val addressParts = mutableListOf<String>()

                if (shipToIndex >= 0) {
                    for (i in (shipToIndex + 1) until lines.size) {
                        val line = lines[i]

                        if (
                            line.contains("ship from", ignoreCase = true) ||
                            line.contains("logistics", ignoreCase = true)
                        ) {
                            break
                        }

                        if (line != name &&
                            !line.contains("tel", ignoreCase = true)
                        ) {
                            addressParts.add(line)
                        }
                    }
                }

                val address = addressParts
                    .joinToString(", ")
                    .trim()
                    .takeIf { it.isNotBlank() }
                    ?: fullText

                continuation.resume(
                    ScannedLabel(
                        rawAddressText = address,
                        possiblePhone = phone,
                        possiblePincode = pincode,
                        possibleName = name,
                        possibleShipmentId = shipmentId,
                        possibleOrderNo = orderNo,
                        possibleCodAmount = codAmount
                    )
                )
            }
            .addOnFailureListener { e ->
                continuation.resumeWithException(e)
            }

    } catch (e: Exception) {
        continuation.resumeWithException(e)
    }
}

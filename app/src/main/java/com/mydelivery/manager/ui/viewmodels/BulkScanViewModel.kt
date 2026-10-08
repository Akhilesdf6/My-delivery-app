package com.mydelivery.manager.ui.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mydelivery.manager.utils.ScannedLabel
import com.mydelivery.manager.utils.extractTextFromImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class ScannedShipmentState(
    val id: String = UUID.randomUUID().toString(),
    val imageUri: Uri,
    val parsedLabel: ScannedLabel,
    val isProcessing: Boolean = false,
    val isError: Boolean = false
)

class BulkScanViewModel : ViewModel() {
    private val _scannedShipments = MutableStateFlow<List<ScannedShipmentState>>(emptyList())
    val scannedShipments: StateFlow<List<ScannedShipmentState>> = _scannedShipments.asStateFlow()

    private val _isProcessingBatch = MutableStateFlow(false)
    val isProcessingBatch: StateFlow<Boolean> = _isProcessingBatch.asStateFlow()

    fun processImagesSequentially(context: Context, uris: List<Uri>) {
        viewModelScope.launch {
            _isProcessingBatch.value = true
            
            val initialList = uris.map { uri ->
                ScannedShipmentState(
                    imageUri = uri,
                    parsedLabel = ScannedLabel("", null, null, null, null, null, null),
                    isProcessing = true
                )
            }
            _scannedShipments.value = initialList

            uris.forEachIndexed { index, uri ->
                try {
                    val result = extractTextFromImage(context, uri)
                    _scannedShipments.update { currentList ->
                        val mutableList = currentList.toMutableList()
                        mutableList[index] = mutableList[index].copy(
                            parsedLabel = result,
                            isProcessing = false
                        )
                        mutableList
                    }
                } catch (e: Exception) {
                    _scannedShipments.update { currentList ->
                        val mutableList = currentList.toMutableList()
                        mutableList[index] = mutableList[index].copy(
                            isProcessing = false,
                            isError = true
                        )
                        mutableList
                    }
                }
            }
            
            _isProcessingBatch.value = false
        }
    }
}

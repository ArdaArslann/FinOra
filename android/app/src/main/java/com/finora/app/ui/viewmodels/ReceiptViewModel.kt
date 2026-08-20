package com.finora.app.ui.viewmodels

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finora.app.data.network.ConfirmReceiptRequest
import com.finora.app.data.network.ReceiptApi
import com.finora.app.data.network.ReceiptDto
import com.finora.app.domain.model.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class ReceiptViewModel @Inject constructor(
    private val receiptApi: ReceiptApi,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uploadState = MutableStateFlow<Resource<ReceiptDto?>>(Resource.Success(null))
    val uploadState: StateFlow<Resource<ReceiptDto?>> = _uploadState.asStateFlow()

    private val _confirmState = MutableStateFlow<Resource<Unit>>(Resource.Success(Unit))
    val confirmState: StateFlow<Resource<Unit>> = _confirmState.asStateFlow()

    fun resetState() {
        _uploadState.value = Resource.Success(null)
    }

    fun uploadReceipt(uri: Uri) {
        viewModelScope.launch {
            _uploadState.value = Resource.Loading()
            try {
                val file = getFileFromUri(context, uri)
                if (file == null) {
                    _uploadState.value = Resource.Error("Could not read file")
                    return@launch
                }

                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                val response = receiptApi.uploadReceipt(body)
                if (response.isSuccessful && response.body()?.success == true && response.body()?.data != null) {
                    _uploadState.value = Resource.Success(response.body()!!.data!!)
                } else {
                    _uploadState.value = Resource.Error(response.body()?.error?.message ?: "Upload failed")
                }
            } catch (e: Exception) {
                _uploadState.value = Resource.Error(e.localizedMessage ?: "Connection error")
            }
        }
    }

    fun confirmReceipt(id: String, request: ConfirmReceiptRequest) {
        viewModelScope.launch {
            _confirmState.value = Resource.Loading()
            try {
                val response = receiptApi.confirmReceipt(id, request)
                if (response.isSuccessful && response.body()?.success == true) {
                    _confirmState.value = Resource.Success(Unit)
                } else {
                    _confirmState.value = Resource.Error(response.body()?.error?.message ?: "Confirmation failed")
                }
            } catch (e: Exception) {
                _confirmState.value = Resource.Error(e.localizedMessage ?: "Connection error")
            }
        }
    }

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val contentResolver = context.contentResolver
            val fileName = getFileName(context, uri) ?: "temp_receipt.jpg"
            val tempFile = File(context.cacheDir, fileName)
            
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            tempFile
        } catch (e: Exception) {
            null
        }
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        result = cursor.getString(index)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result
    }
}

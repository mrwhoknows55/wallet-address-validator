package com.mrwhoknows.wallet.address.validator.ui

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment.DIRECTORY_PICTURES
import android.os.Environment.getExternalStoragePublicDirectory
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.mrwhoknows.wallet.address.validator.BTCAddressValidator
import com.mrwhoknows.wallet.address.validator.ETHAddressValidator
import com.mrwhoknows.wallet.address.validator.ui.ValidationFragment.Companion.IMAGE_MIME
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class ValidationViewModel : ViewModel() {
    private var addressType: String = ""
    private val _qrCodeBmp = MutableLiveData<Bitmap>()
    val qrCodeBmp: LiveData<Bitmap> = _qrCodeBmp

    private val _isValidAddress = MutableLiveData<Boolean>()
    val isValidAddress: LiveData<Boolean> = _isValidAddress

    private val _addressValue = MutableLiveData<String>()
    val addressValue: LiveData<String> = _addressValue

    private val _errorMsg = MutableLiveData<String?>()
    val errorMsg: LiveData<String?> = _errorMsg

    private val _imageUri = MutableLiveData<Uri>()
    val imageUri: LiveData<Uri> = _imageUri

    fun validateAddress() {
        val validator =
            if (addressType == ValidationFragment.ETH) ETHAddressValidator() else BTCAddressValidator()
        _isValidAddress.postValue(validator.validateAddress(addressValue.value ?: ""))
    }

    fun createQRCodeBitmap(address: String, width: Int = 500, height: Int = 500) {
        setAddressValue(address)
        try {
            val multiFormatWriter = MultiFormatWriter()
            val bitMatrix = multiFormatWriter.encode(address, BarcodeFormat.QR_CODE, width, height)
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.createBitmap(bitMatrix)
            _qrCodeBmp.postValue(bitmap)
        } catch (e: Exception) {
            _errorMsg.postValue(e.localizedMessage)
            e.printStackTrace()
        }
    }

    private fun setAddressValue(address: String) {
        _addressValue.postValue(address)
    }

    fun setAddressType(type: String) {
        addressType = type
    }


    fun getBitmapFromView(resolver: ContentResolver, dirName: String) {
        val fileName = "${addressValue.value}.png"
        viewModelScope.launch(Dispatchers.IO) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                resolver.saveImageAndPostUri(fileName, dirName)
            } else {
                saveImageAndPostUri(fileName, dirName)
            }
        }
    }

    private fun saveImageAndPostUri(fileName: String, dirName: String) {
        try {
            val dir = File(getExternalStoragePublicDirectory(DIRECTORY_PICTURES), "/$dirName")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "/$fileName")
            val bytes = ByteArrayOutputStream()
            qrCodeBmp.value?.compress(Bitmap.CompressFormat.PNG, 90, bytes)
            FileOutputStream(file).write(bytes.toByteArray())
            bytes.flush()
            bytes.close()
            _imageUri.postValue(Uri.fromFile(file))
        } catch (e: Exception) {
            _errorMsg.postValue(e.localizedMessage)
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun ContentResolver.saveImageAndPostUri(fileName: String, dirName: String) {
        try {
            val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$dirName")
                put(MediaStore.Images.Media.MIME_TYPE, IMAGE_MIME)
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val uri = this.insert(collection, values)
            uri?.let {
                this.openOutputStream(it)?.use { outputStream ->
                    val bytes = ByteArrayOutputStream()
                    qrCodeBmp.value?.compress(Bitmap.CompressFormat.PNG, 90, bytes)
                    outputStream.write(bytes.toByteArray())
                    outputStream.close()
                }
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                this.update(it, values, null, null)
                _imageUri.postValue(it)
            } ?: throw RuntimeException("mediaStore failed")
        } catch (e: Exception) {
            _errorMsg.postValue(e.localizedMessage)
            e.printStackTrace()
        }
    }
}
package com.mrwhoknows.wallet.address.validator.ui

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.mrwhoknows.wallet.address.validator.BTCAddressValidator
import com.mrwhoknows.wallet.address.validator.ETHAddressValidator

class ValidationViewModel : ViewModel() {
    private val _qrCodeBmp = MutableLiveData<Bitmap>()
    val qrCodeBmp: LiveData<Bitmap> = _qrCodeBmp

    private val _isValidAddress = MutableLiveData(false)
    val isValidAddress: LiveData<Boolean> = _isValidAddress

    private val _addressValue = MutableLiveData<String>()
    val addressValue: LiveData<String> = _addressValue

    private var addressType: String = ""

    fun validateAddress() {
        val validator =
            if (addressType == ValidationFragment.ETH) ETHAddressValidator() else BTCAddressValidator()
        _isValidAddress.postValue(validator.validateAddress(addressValue.value ?: ""))
    }

    fun createQRCodeBitmap(address: String, width: Int = 500, height: Int = 500) {
        setAddressValue(address)
        try {
            val multiFormatWriter = MultiFormatWriter()
            val bitMatrix =
                multiFormatWriter.encode(address, BarcodeFormat.QR_CODE, width, height)
            val barcodeEncoder = BarcodeEncoder()
            val bitmap = barcodeEncoder.createBitmap(bitMatrix)
            _qrCodeBmp.postValue(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setAddressValue(address: String) {
        _addressValue.postValue(address)
    }

    fun setAddressType(type: String) {
        addressType = type
    }
}
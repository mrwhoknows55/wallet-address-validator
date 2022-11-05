package com.mrwhoknows.wallet.address.validator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.mrwhoknows.wallet.address.validator.databinding.FragmentValidationBinding

class ValidationFragment : Fragment() {
    private lateinit var binding: FragmentValidationBinding
    private val addressType: String by lazy { requireArguments().getString(ADDRESS_TYPE, BTC) }
    private val viewModel: ValidationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentValidationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setAddressType(addressType)
        qrCodeScannerLauncher.launch(getQrScanOptions())
        setupObservables()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnValidate.setOnClickListener {
            viewModel.validateAddress()
        }

        binding.btnShare.setOnClickListener {
            Toast.makeText(requireContext(), "on share clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupObservables() {
        viewModel.qrCodeBmp.observe(viewLifecycleOwner) { bmp ->
            bmp?.let {
                binding.ivQRCode.setImageBitmap(it)
            }
        }

        viewModel.addressValue.observe(viewLifecycleOwner) { address ->
            address?.let {
                binding.tvAddress.text = it
            }
        }

        viewModel.isValidAddress.observe(viewLifecycleOwner) { isValid ->
            if (isValid) {
                binding.btnShare.visibility = View.VISIBLE
            } else {
                binding.btnShare.visibility = View.GONE
            }
        }
    }

    private fun getQrScanOptions() = ScanOptions().apply {
        setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        setCameraId(0)
        setBeepEnabled(false)
        setOrientationLocked(true)
        setBarcodeImageEnabled(true)
        setPrompt("Scan a ${addressType.uppercase()} address QR Code")
    }

    private val qrCodeScannerLauncher = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->
        result.contents?.let {
            viewModel.createQRCodeBitmap(result.contents)
        } ?: kotlin.run {
            findNavController().navigateUp()
        }
    }

    companion object {
        const val ADDRESS_TYPE = "addressType"
        const val BTC = "btc"
        const val ETH = "eth"
    }
}
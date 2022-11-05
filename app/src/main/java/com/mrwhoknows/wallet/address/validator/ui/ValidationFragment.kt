package com.mrwhoknows.wallet.address.validator.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.mrwhoknows.wallet.address.validator.R
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
        setupObservables()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        checkAndAskCameraPermission()
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
            navigateBack()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            qrCodeScannerLauncher.launch(getQrScanOptions())
        }
    }

    private fun checkAndAskCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                qrCodeScannerLauncher.launch(getQrScanOptions())
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(), Manifest.permission.CAMERA
            ) -> {
                showPermissionRationaleDialog(false)
            }

            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_DENIED -> {
                showPermissionRationaleDialog(true)
            }

            else -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.CAMERA
                )
            }
        }
    }

    private fun showPermissionRationaleDialog(deniedAlways: Boolean) {
        MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.permission_required)
            .setMessage(R.string.rationale_msg).setCancelable(false)
            .setPositiveButton(R.string.ok) { _, _ ->
                if (!deniedAlways) {
                    requestPermissionLauncher.launch(
                        Manifest.permission.CAMERA
                    )
                } else {
                    Intent(
                        ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:${requireActivity().packageName}")
                    ).apply {
                        addCategory(Intent.CATEGORY_DEFAULT)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(this)
                    }
                }
            }.setNegativeButton(R.string.return_to_home) { _, _ ->
                navigateBack()
            }.show()
    }

    private fun navigateBack() {
        findNavController().navigateUp()
    }

    companion object {
        const val ADDRESS_TYPE = "addressType"
        const val BTC = "btc"
        const val ETH = "eth"
    }
}
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
        checkAndAskCameraPermission()
        setupObservables()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnValidate.setOnClickListener {
            viewModel.validateAddress()
        }

        binding.btnShare.setOnClickListener {
            checkAndAskExternalPermission()
        }
    }

    private fun onShareQrCodeImage() {
        viewModel.getBitmapFromView(requireContext().contentResolver, QR_DIR)
    }

    private fun setupObservables() {
        viewModel.qrCodeBmp.observe(viewLifecycleOwner) { bmp ->
            bmp?.let {
                binding.ivQRCode.setImageBitmap(it)
            }
        }

        viewModel.addressValue.observe(viewLifecycleOwner) { address ->
            address?.let {
                binding.tvAddress.text = getString(R.string.scanned_address, it)
            } ?: kotlin.run {
                binding.tvValidInvalid.visibility = View.GONE
            }
        }

        viewModel.isValidAddress.observe(viewLifecycleOwner) { isValid ->
            updateValidTextUi(isValid)
        }

        viewModel.errorMsg.observe(viewLifecycleOwner) { err ->
            err?.let {
                showLongToast(it)
                navigateBack()
            }
        }

        viewModel.imageUri.observe(viewLifecycleOwner) {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = IMAGE_MIME
            intent.putExtra(Intent.EXTRA_STREAM, it)
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            startActivity(Intent.createChooser(intent, getString(R.string.share_qr_description)))
        }
    }

    private fun updateValidTextUi(isValid: Boolean) {
        if (isValid) {
            val green = ContextCompat.getColor(requireContext(), R.color.green)
            binding.btnShare.visibility = View.VISIBLE
            binding.tvAddress.setTextColor(green)
            binding.tvValidInvalid.apply {
                text = getString(R.string.valid_address)
                visibility = View.VISIBLE
                setTextColor(green)
            }
        } else {
            val red = ContextCompat.getColor(requireContext(), R.color.red)
            binding.btnShare.visibility = View.GONE
            binding.tvAddress.setTextColor(red)
            binding.tvValidInvalid.apply {
                text = getString(R.string.invalid_address)
                visibility = View.VISIBLE
                setTextColor(red)
            }
        }
    }

    private fun getQrScanOptions() = ScanOptions().apply {
        setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        setCameraId(0)
        setBeepEnabled(false)
        setOrientationLocked(true)
        setBarcodeImageEnabled(true)
        setPrompt("Scan ${addressType.uppercase()} address QR Code")
    }

    private val qrCodeScannerLauncher = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->
        result.contents?.let {
            viewModel.createQRCodeBitmap(it)
        } ?: kotlin.run {
            navigateBack()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            qrCodeScannerLauncher.launch(getQrScanOptions())
        } else {
            checkAndAskCameraPermission()
        }
    }

    private val storageRequestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            onShareQrCodeImage()
        } else {
            checkAndAskExternalPermission()
        }
    }

    private fun checkAndAskExternalPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                onShareQrCodeImage()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) -> {
                showPermissionRationaleDialog(getString(R.string.storage_rationale_msg))
            }

            else -> {
                storageRequestPermissionLauncher.launch(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
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
                showPermissionRationaleDialog(getString(R.string.camera_rationale_msg))
            }

            else -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.CAMERA,
                )
            }
        }
    }

    private fun showPermissionRationaleDialog(rationaleMsg: String) {
        MaterialAlertDialogBuilder(requireContext()).setTitle(R.string.permission_required)
            .setMessage(rationaleMsg).setCancelable(false).setPositiveButton(R.string.ok) { _, _ ->
                Intent(
                    ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:${requireActivity().packageName}")
                ).apply {
                    addCategory(Intent.CATEGORY_DEFAULT)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(this)
                }
            }.setNegativeButton(R.string.return_to_home) { _, _ ->
                navigateBack()
            }.show()
    }

    private fun navigateBack() {
        findNavController().navigateUp()
    }

    companion object {
        const val IMAGE_MIME = "image/*"
        const val ADDRESS_TYPE = "addressType"
        const val QR_DIR = "WalletAddressValidator"
        const val BTC = "btc"
        const val ETH = "eth"
    }
}

fun Fragment.showLongToast(msg: String) {
    Toast.makeText(this.requireContext(), msg, Toast.LENGTH_LONG).show()
}
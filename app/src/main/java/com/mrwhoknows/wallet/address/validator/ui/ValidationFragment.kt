package com.mrwhoknows.wallet.address.validator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mrwhoknows.wallet.address.validator.databinding.FragmentValidationBinding

class ValidationFragment : Fragment() {
    private lateinit var binding: FragmentValidationBinding
    private val addressType: String by lazy { requireArguments().getString(ADDRESS_TYPE, BTC) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentValidationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.text.text = "Address Type: $addressType"
    }

    companion object {
        const val ADDRESS_TYPE = "addressType"
        const val BTC = "btc"
        const val ETH = "eth"
    }
}
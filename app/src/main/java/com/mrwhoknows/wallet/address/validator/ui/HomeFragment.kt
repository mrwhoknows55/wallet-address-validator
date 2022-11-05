package com.mrwhoknows.wallet.address.validator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mrwhoknows.wallet.address.validator.databinding.FragmentHomeBinding
import com.mrwhoknows.wallet.address.validator.ui.ValidationFragment.Companion.BTC
import com.mrwhoknows.wallet.address.validator.ui.ValidationFragment.Companion.ETH

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnBtc.setOnClickListener {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToValidationFragment(
                    BTC
                )
            )
        }

        binding.btnEth.setOnClickListener {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToValidationFragment(
                    ETH
                )
            )
        }
    }
}
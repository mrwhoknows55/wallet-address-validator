package com.mrwhoknows.wallet.address.validator.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.mrwhoknows.wallet.address.validator.R
import com.mrwhoknows.wallet.address.validator.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        val navController = supportFragmentManager.findFragmentById(R.id.navHostFragmentContainer)!!
            .findNavController()
        binding.toolbar.setupWithNavController(navController)
    }
}
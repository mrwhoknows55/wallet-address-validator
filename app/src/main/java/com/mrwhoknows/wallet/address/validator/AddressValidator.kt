package com.mrwhoknows.wallet.address.validator

interface AddressValidator {
    fun validateAddress(address: String): Boolean
}

class BTCAddressValidator : AddressValidator {
    override fun validateAddress(address: String): Boolean {
        val pattern = "1[a-km-zA-HJ-NP-Z1-9]{25,34}".toRegex()
        return pattern.matches(address)
    }
}

class ETHAddressValidator : AddressValidator {
    override fun validateAddress(address: String): Boolean {
        return "0x[a-fA-F0-9]{40}".toRegex().matches(address)
    }
}
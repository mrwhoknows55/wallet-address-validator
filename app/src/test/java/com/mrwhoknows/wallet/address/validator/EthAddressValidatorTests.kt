package com.mrwhoknows.wallet.address.validator

import org.junit.Assert.assertEquals
import org.junit.Test

class EthAddressValidatorTests {
    private val ethAddressValidator = ETHAddressValidator()
    private val validEthAddress = "0x3d3B9e0fEA995b5716559De5f9448775C058351b"
    private val invalidEthAddress = "0x3a3B9x0fEA995b5716559De5f944w775C058351b"

    @Test
    fun validEthAddress_isValid() = assert(ethAddressValidator.validateAddress(validEthAddress))

    @Test
    fun invalidEthAddress_isInvalid() =
        assertEquals(ethAddressValidator.validateAddress(invalidEthAddress), false)

}
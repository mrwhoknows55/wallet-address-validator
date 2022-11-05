package com.mrwhoknows.wallet.address.validator

import org.junit.Assert.assertEquals
import org.junit.Test

class BtcAddressValidatorTests {
    private val btcAddressValidator = BTCAddressValidator()
    private val validBtcAddress = "1BvBMSEYstWetqTFn5Au4m4GFg7xJaNVN2"
    private val invalidBtcAddress = "1OvBMSEIstWetqTFn5Au4m4GFg70JaNVN2"

    @Test
    fun validBtcAddress_isValid() = assert(btcAddressValidator.validateAddress(validBtcAddress))

    @Test
    fun invalidBtcAddress_isInvalid() =
        assertEquals(btcAddressValidator.validateAddress(invalidBtcAddress), false)

}
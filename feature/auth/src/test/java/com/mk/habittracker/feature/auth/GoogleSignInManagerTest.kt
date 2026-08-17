package com.mk.habittracker.feature.auth

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import android.util.Base64
import org.json.JSONObject

@RunWith(RobolectricTestRunner::class)
class GoogleSignInManagerTest {

    @Test
    fun `generateSecureRandomNonce returns non-empty string`() {
        val nonce = generateSecureRandomNonce()
        assertThat(nonce).isNotEmpty()
    }

    @Test
    fun `generateSecureRandomNonce returns different values`() {
        val nonce1 = generateSecureRandomNonce()
        val nonce2 = generateSecureRandomNonce()
        assertThat(nonce1).isNotEqualTo(nonce2)
    }

    @Test
    fun `extractNonceFromIdToken extracts nonce correctly`() {
        val nonce = "test-nonce"
        val header = Base64.encodeToString("{}".toByteArray(), Base64.NO_WRAP)
        val payload = Base64.encodeToString(
            JSONObject().put("nonce", nonce).toString().toByteArray(),
            Base64.NO_WRAP
        )
        val idToken = "$header.$payload.signature"

        val extracted = extractNonceFromIdToken(idToken)
        assertThat(extracted).isEqualTo(nonce)
    }

    @Test
    fun `extractNonceFromIdToken returns null for invalid token`() {
        assertThat(extractNonceFromIdToken("invalid")).isNull()
        assertThat(extractNonceFromIdToken("a.b")).isNull()
    }
}

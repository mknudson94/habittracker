package com.mk.habittracker.feature.auth

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.ClearCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import java.security.SecureRandom
import javax.inject.Inject


class GoogleSignInManager @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val credentialManager = CredentialManager.create(context)

    suspend fun requestGoogleCredential(
        context: Context,
        filterByAuthorizedAccounts: Boolean,
        autoSelect: Boolean = false,
    ): String {
        val nonce = generateSecureRandomNonce()
        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
            .setServerClientId("343615861912-t7f2fdp3ruego4f17o1pq1j51t8vi24l.apps.googleusercontent.com")
            .setAutoSelectEnabled(autoSelect)
            .setNonce(nonce)
            .build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
        val credential =
            credentialManager.getCredential(context = context, request = request).credential
        val token = GoogleIdTokenCredential.createFrom(credential.data).idToken
        check(nonce == extractNonceFromIdToken(token))
        return token
    }

    suspend fun clearCredentials() {
        try {
            val clearRequest = ClearCredentialStateRequest()
            credentialManager.clearCredentialState(clearRequest)
        } catch (e: ClearCredentialException) {
            Log.e("Sign In", "Couldn't clear user credentials: ${e.localizedMessage}")
        }
    }
}

fun generateSecureRandomNonce(byteLength: Int = 32): String {
    val randomBytes = ByteArray(byteLength)
    SecureRandom().nextBytes(randomBytes)
    return Base64.encodeToString(
        randomBytes,
        Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING
    )
}

fun extractNonceFromIdToken(idToken: String): String? {
    return try {
        val payloadSegment = idToken.split(".")[1]
        val decodedBytes =
            Base64.decode(payloadSegment, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        JSONObject(String(decodedBytes)).optString("nonce", null)
    } catch (e: Exception) {
        Log.e("Sign In", "Failed to parse ID token nonce: $e")
        null
    }
}

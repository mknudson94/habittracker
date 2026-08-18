package com.mk.habittracker.feature.auth

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LoginViewModel
    @Inject
    constructor(
        private val auth: FirebaseAuth,
        val googleSignInManager: GoogleSignInManager,
    ) : ViewModel() {
        private var _authState = MutableStateFlow(auth.currentUser)
        val authState = _authState.asStateFlow()

        init {
            auth.addAuthStateListener {
                _authState.tryEmit(it.currentUser)
            }
        }

        fun signIn(
            email: String,
            password: String,
        ) {
            viewModelScope.launch {
                try {
                    val result = auth.signInWithEmailAndPassword(email, password).await()
                    _authState.value = result.user
                } catch (e: FirebaseAuthException) {
                    Log.e("login", "failed to log in: $e")
                }
            }
        }

        fun signInWithGoogle(activityContext: Context) {
            viewModelScope.launch {
                val googleIdToken =
                    try {
                        googleSignInManager.requestGoogleCredential(
                            context = activityContext,
                            filterByAuthorizedAccounts = false,
                            autoSelect = false,
                        )
                    } catch (e: GetCredentialException) {
                        Log.e("Sign In", "${e.javaClass.simpleName}: ${e.message}")
                        Toast
                            .makeText(
                                activityContext,
                                "Something went wrong, please try again",
                                Toast.LENGTH_SHORT,
                            ).show()
                        return@launch
                    }
                try {
                    val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val result = auth.signInWithCredential(firebaseCredential).await()
                    _authState.value = result.user
                } catch (e: FirebaseAuthException) {
                    Log.e("login", "failed to log in: $e")
                }
            }
        }

        fun signUp(
            email: String,
            password: String,
        ) {
            viewModelScope.launch {
                try {
                    val result = auth.createUserWithEmailAndPassword(email, password).await()
                    _authState.value = result.user
                } catch (e: FirebaseAuthException) {
                    Log.e("login", "failed to sign up: $e")
                }
            }
        }

        /**
         * When a user signs out, clear the current user credential state from all credential providers.
         */
        fun signOut() {
            viewModelScope.launch {
                authState.value?.let { auth.signOut() }
                googleSignInManager.clearCredentials()
            }
        }
    }

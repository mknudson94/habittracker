package com.mk.habittracker.feature.auth

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
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
        val googleSignInManager: GoogleSignInManager,
    ) : ViewModel() {
        private val auth = Firebase.auth
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
            auth
                .signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    _authState.value = it.user
                }.addOnFailureListener {
                    Log.e("login", "failed to log in: $it")
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
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                auth
                    .signInWithCredential(firebaseCredential)
                    .addOnSuccessListener {
                        _authState.value = it.user
                    }.addOnFailureListener {
                        Log.e("login", "failed to log in: $it")
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
                } catch (e: Exception) {
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

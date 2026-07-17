package com.mk.habittracker.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {
    private val auth = Firebase.auth
    private var _authState = MutableStateFlow(auth.currentUser)
    val authState = _authState.asStateFlow()

    init {
        auth.addAuthStateListener {
            _authState.tryEmit(it.currentUser)
        }
    }

    fun signIn(email: String, password: String) {
        auth
            .signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                _authState.value = it.user
            }
            .addOnFailureListener {
                Log.e("login", "failed to log in: $it")
            }
    }

    fun signUp(email: String, password: String) {
        Log.d("signup", "in signup fun")
        viewModelScope.launch {
            Log.d("signup", "in signup launch block")
            auth
                .createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Log.d("signup", "in success callback")
                    _authState.value = it.user
                }
                .addOnFailureListener {
                    Log.e("login", "failed to log in: $it")
                    throw it
                }
                .await()
        }
    }

    fun signOut() {
        authState.value?.let {
            auth.signOut()
        }
    }
}

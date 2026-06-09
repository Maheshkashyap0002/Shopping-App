package com.shoppingappmahesh.ui.screens.auth.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.shoppingappmahesh.domain.model.User
import com.shoppingappmahesh.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

sealed class AuthNavigationEvent {
    object NavigateToHome : AuthNavigationEvent()
    object NavigateToProfileSetup : AuthNavigationEvent()
    data class NavigateToOtp(val phoneNumber: String) : AuthNavigationEvent()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<AuthNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _verificationId = MutableStateFlow("")
    val verificationId = _verificationId.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _isCodeSent = MutableStateFlow(false)
    val isCodeSent = _isCodeSent.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(repository.isUserLoggedIn())
    val isLoggedIn = _isLoggedIn.asStateFlow()

    private val _isProfileComplete = MutableStateFlow(false)
    val isProfileComplete = _isProfileComplete.asStateFlow()

    private val _resendTimer = MutableStateFlow(0)
    val resendTimer = _resendTimer.asStateFlow()

    private var timerJob: kotlinx.coroutines.Job? = null

    init {
        // Listen for Auth state changes
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            val currentlyLogged = user != null
            _isLoggedIn.value = currentlyLogged
            
            if (currentlyLogged) {
                checkProfileStatus()
            } else {
                _isProfileComplete.value = false
                _isLoading.value = false
            }
        }
    }

    private fun checkProfileStatus() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val isComplete = repository.isProfileComplete(uid)
                _isProfileComplete.value = isComplete
            } catch (e: Exception) {
                _isProfileComplete.value = false
            }
        }
    }

    fun updateProfile(name: String, email: String, photoUrl: String) {
        val uid = auth.currentUser?.uid ?: return
        _isLoading.value = true
        viewModelScope.launch {
            val user = User(
                uid = uid,
                name = name,
                email = email,
                phone = auth.currentUser?.phoneNumber ?: "",
                photoUrl = photoUrl,
                createdAt = System.currentTimeMillis()
            )
            repository.createUserProfile(user).onSuccess {
                _isProfileComplete.value = true
                _isLoading.value = false
                _navigationEvent.emit(AuthNavigationEvent.NavigateToHome)
            }.onFailure {
                _error.value = it.message
                _isLoading.value = false
            }
        }
    }

    fun sendOtp(phoneNumber: String, activity: Activity) {
        if (_resendTimer.value > 0) return
        
        _isLoading.value = true
        _error.value = null
        _isCodeSent.value = false

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    _isLoading.value = false
                    _error.value = e.message
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    _isLoading.value = false
                    _verificationId.value = verificationId
                    _isCodeSent.value = true
                    viewModelScope.launch {
                        _navigationEvent.emit(AuthNavigationEvent.NavigateToOtp(phoneNumber))
                    }
                    startTimer()
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun resendOtp(phoneNumber: String, activity: Activity) {
        sendOtp(phoneNumber, activity)
    }

    private fun startTimer() {
        timerJob?.cancel()
        _resendTimer.value = 60
        timerJob = viewModelScope.launch {
            while (_resendTimer.value > 0) {
                kotlinx.coroutines.delay(1000)
                _resendTimer.value -= 1
            }
        }
    }

    fun verifyOtp(otp: String) {
        if (otp.length < 6) {
            _error.value = "Please enter a valid 6-digit OTP"
            return
        }
        if (_verificationId.value.isEmpty()) {
            _error.value = "Verification ID missing. Please resend OTP."
            return
        }
        val credential = PhoneAuthProvider.getCredential(_verificationId.value, otp)
        signInWithCredential(credential)
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        _isLoading.value = true
        _error.value = null
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    if (firebaseUser != null) {
                        viewModelScope.launch {
                            try {
                                val isComplete = repository.isProfileComplete(firebaseUser.uid)
                                _isProfileComplete.value = isComplete
                                _isLoggedIn.value = true
                                _isLoading.value = false
                                if (isComplete) {
                                    _navigationEvent.emit(AuthNavigationEvent.NavigateToHome)
                                } else {
                                    _navigationEvent.emit(AuthNavigationEvent.NavigateToProfileSetup)
                                }
                            } catch (e: Exception) {
                                _isLoading.value = false
                                _isLoggedIn.value = true
                                _navigationEvent.emit(AuthNavigationEvent.NavigateToProfileSetup)
                            }
                        }
                    } else {
                        _isLoading.value = false
                        _error.value = "Authentication failed"
                    }
                } else {
                    _isLoading.value = false
                    _error.value = task.exception?.message ?: "Sign in failed"
                }
            }
    }

    fun resetState() {
        _isCodeSent.value = false
        _error.value = null
        _isLoading.value = false
    }
}
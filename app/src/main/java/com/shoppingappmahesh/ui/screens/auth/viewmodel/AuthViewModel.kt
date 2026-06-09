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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

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
        checkProfileStatus()
    }

    private fun checkProfileStatus() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _isProfileComplete.value = repository.isProfileComplete(uid)
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
        if (_verificationId.value.isEmpty()) {
            _error.value = "Verification ID missing. Please resend OTP."
            return
        }
        val credential = PhoneAuthProvider.getCredential(_verificationId.value, otp)
        signInWithCredential(credential)
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        _isLoading.value = true
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    if (firebaseUser != null) {
                        viewModelScope.launch {
                            val isComplete = repository.isProfileComplete(firebaseUser.uid)
                            _isProfileComplete.value = isComplete
                            _isLoggedIn.value = true
                            _isLoading.value = false
                        }
                    }
                } else {
                    _isLoading.value = false
                    _error.value = task.exception?.message
                }
            }
    }
}
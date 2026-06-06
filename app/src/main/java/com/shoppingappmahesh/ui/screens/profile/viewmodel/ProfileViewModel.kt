package com.shoppingappmahesh.ui.screens.profile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shoppingappmahesh.domain.model.User
import com.shoppingappmahesh.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess = _updateSuccess.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val uid = repository.getCurrentUserId() ?: return
        _isLoading.value = true
        repository.getUserDetails(uid).onEach {
            _user.value = it
            _isLoading.value = false
        }.launchIn(viewModelScope)
    }

    fun updateProfile(name: String, email: String) {
        val currentUser = _user.value ?: return
        _isLoading.value = true
        viewModelScope.launch {
            val updatedUser = currentUser.copy(name = name, email = email)
            repository.createUserProfile(updatedUser).onSuccess {
                _updateSuccess.value = true
                _isLoading.value = false
            }.onFailure {
                _isLoading.value = false
            }
        }
    }

    fun resetUpdateState() {
        _updateSuccess.value = false
    }

    fun logout() {
        viewModelScope.launch {
            repository.signOut()
        }
    }
}
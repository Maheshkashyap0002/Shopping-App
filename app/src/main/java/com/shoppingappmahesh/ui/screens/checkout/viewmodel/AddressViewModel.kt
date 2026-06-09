package com.shoppingappmahesh.ui.screens.checkout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shoppingappmahesh.domain.model.Address
import com.shoppingappmahesh.domain.repository.AddressRepository
import com.shoppingappmahesh.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddressViewModel @Inject constructor(
    private val addressRepository: AddressRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val userId: String = authRepository.getCurrentUserId() ?: ""

    val addresses: StateFlow<List<Address>> = addressRepository.getAddresses(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow<AddressUiState>(AddressUiState.Idle)
    val uiState: StateFlow<AddressUiState> = _uiState.asStateFlow()

    fun addAddress(address: Address) {
        viewModelScope.launch {
            _uiState.value = AddressUiState.Loading
            val result = addressRepository.addAddress(address.copy(userId = userId))
            _uiState.value = result.fold(
                onSuccess = { AddressUiState.Success("Address added successfully") },
                onFailure = { AddressUiState.Error(it.message ?: "Failed to add address") }
            )
        }
    }

    fun updateAddress(address: Address) {
        viewModelScope.launch {
            _uiState.value = AddressUiState.Loading
            val result = addressRepository.updateAddress(address)
            _uiState.value = result.fold(
                onSuccess = { AddressUiState.Success("Address updated successfully") },
                onFailure = { AddressUiState.Error(it.message ?: "Failed to update address") }
            )
        }
    }

    fun deleteAddress(addressId: String) {
        viewModelScope.launch {
            addressRepository.deleteAddress(userId, addressId)
        }
    }

    fun setDefaultAddress(addressId: String) {
        viewModelScope.launch {
            addressRepository.setDefaultAddress(userId, addressId)
        }
    }

    fun resetState() {
        _uiState.value = AddressUiState.Idle
    }
}

sealed class AddressUiState {
    object Idle : AddressUiState()
    object Loading : AddressUiState()
    data class Success(val message: String) : AddressUiState()
    data class Error(val message: String) : AddressUiState()
}
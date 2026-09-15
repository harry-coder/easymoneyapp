package com.tech.easymoney.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tech.easymoney.data.model.OnboardingRequest
import com.tech.easymoney.data.network.ApiService
import com.tech.easymoney.utils.AuthPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthStep {
    LOGIN,
    OTP,
    ONBOARDING,
    AUTHENTICATED
}

data class AuthUiState(
    val step: AuthStep = AuthStep.LOGIN,
    val mobile: String = "",
    val otp: String = "",
    val name: String = "",
    val city: String = "",
    val company: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false,
    val isOnboarded: Boolean = false
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val authPrefs = AuthPreferences(application)
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val loggedIn = authPrefs.isLoggedIn.first()
            val onboarded = authPrefs.isOnboarded.first()
            _uiState.update { it.copy(
                isLoggedIn = loggedIn,
                isOnboarded = onboarded,
                step = if (loggedIn) AuthStep.AUTHENTICATED else AuthStep.LOGIN
            ) }
        }
    }

    fun updateMobile(value: String) = _uiState.update { it.copy(mobile = value) }
    fun updateOtp(value: String) = _uiState.update { it.copy(otp = value) }
    fun updateName(value: String) = _uiState.update { it.copy(name = value) }
    fun updateCity(value: String) = _uiState.update { it.copy(city = value) }
    fun updateCompany(value: String) = _uiState.update { it.copy(company = value) }

    fun sendOtp() {
        if (_uiState.value.mobile.length != 10) {
            _uiState.update { it.copy(errorMessage = "Invalid mobile number") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val response = ApiService.sendOtp(_uiState.value.mobile)
            if (response.success) {
                _uiState.update { it.copy(isLoading = false, step = AuthStep.OTP) }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = response.message) }
            }
        }
    }

    fun verifyOtp() {
        if (_uiState.value.otp.length < 4) {
            _uiState.update { it.copy(errorMessage = "Invalid OTP") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val response = ApiService.verifyOtp(_uiState.value.mobile, _uiState.value.otp)
            if (response.success) {
                val mobile = _uiState.value.mobile
                val wasOnboarded = authPrefs.isOnboarded.first()

                // Save auth data, using a dummy token if the API doesn't provide one
                authPrefs.saveAuthData(response.token ?: "verified", mobile, wasOnboarded)

                if (wasOnboarded) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        step = AuthStep.AUTHENTICATED,
                        isLoggedIn = true,
                        isOnboarded = true
                    ) }
                } else {
                    _uiState.update { it.copy(
                        isLoading = false,
                        step = AuthStep.ONBOARDING,
                        isLoggedIn = true
                    ) }
                }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = response.message) }
            }
        }
    }

    fun submitOnboarding() {
        val s = _uiState.value
        if (s.name.isBlank() || s.city.isBlank() || s.company.isBlank()) {
            _uiState.update { it.copy(errorMessage = "All fields are required") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val success = ApiService.submitOnboarding(
                OnboardingRequest(s.name, s.city, s.company, s.mobile)
            )
            if (success) {
                authPrefs.setOnboarded(true)
                _uiState.update { it.copy(isLoading = false, step = AuthStep.AUTHENTICATED, isOnboarded = true) }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to update profile") }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authPrefs.clearAuthData()
            _uiState.update { AuthUiState(
                step = AuthStep.LOGIN,
                isOnboarded = it.isOnboarded // Keep onboarding status as requested
            ) }
        }
    }
}

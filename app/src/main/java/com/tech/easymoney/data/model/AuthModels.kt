package com.tech.easymoney.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OtpRequest(
    @SerialName("phone_number")
    val mobile: String
)

@Serializable
data class VerifyOtpRequest(
    @SerialName("phone_number")
    val mobile: String,
    val otp: String
)

@Serializable
data class AuthResponse(
    val success: Boolean,
    val message: String,
    val token: String? = null,
    val isOnboarded: Boolean = false
)

@Serializable
data class OnboardingRequest(
    val name: String,
    val city: String,
    val company: String,
    val mobile: String
)

@Serializable
data class HistoryItem(
    val pan: String,
    val name: String,
    val status: String,
    val loanNumber: String? = null
)

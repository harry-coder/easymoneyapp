package com.tech.easymoney.data.network

import android.util.Log
import com.tech.easymoney.data.model.*
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.logging.HttpLoggingInterceptor

object ApiService {
    private val client = HttpClient(OkHttp) {
        engine {
            addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
        install(DefaultRequest) {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            header(HttpHeaders.Accept, ContentType.Application.Json)
        }
    }

    private const val BASE_URL = "https://api.easymoneys.in"

    suspend fun sendOtp(mobile: String): AuthResponse {
        return try {
            val jsonBody = Json.encodeToString(OtpRequest(mobile))
            val response = client.post("$BASE_URL/api/v1/send-otp") {
                contentType(ContentType.Application.Json)
                setBody(jsonBody)
            }
            response.body<AuthResponse>()
        } catch (e: Exception) {
            Log.e("ApiService", "Error sending OTP", e)
            AuthResponse(success = false, message = "Connection error. Please try again.")
        }
    }

    suspend fun verifyOtp(mobile: String, otp: String): AuthResponse {
        return try {
            val jsonBody = Json.encodeToString(VerifyOtpRequest(mobile, otp))
            val response = client.post("$BASE_URL/api/v1/verify-otp") {
                contentType(ContentType.Application.Json)
                setBody(jsonBody)
            }
            response.body<AuthResponse>()
        } catch (e: Exception) {
            Log.e("ApiService", "Error verifying OTP", e)
            AuthResponse(success = false, message = "Connection error. Please try again.")
        }
    }

    suspend fun submitOnboarding(request: OnboardingRequest): Boolean {
        return try {
            val jsonBody = Json.encodeToString(request)
            val response = client.post("$BASE_URL/api/v1/user-signup") {
                contentType(ContentType.Application.Json)
                setBody(jsonBody)
            }
            response.status.isSuccess()
        } catch (e: Exception) {
            Log.e("ApiService", "Error submitting onboarding", e)
            false
        }
    }

    suspend fun fetchHistoryByPan(pan: String): List<HistoryItem> {
        return try {
            val response = client.get("$BASE_URL/api/v1/user-info-by-pan") {
                parameter("pan", pan)
            }
            if (response.status.isSuccess()) {
                response.body<List<HistoryItem>>()
            } else emptyList()
        } catch (e: Exception) {
            Log.e("ApiService", "Error fetching history", e)
            emptyList()
        }
    }

    suspend fun submitLoanApplication(request: LoanApplicationRequest): Boolean {
        return try {
            val jsonBody = Json.encodeToString(request)
            val response = client.post("$BASE_URL/api/v1/loan-applications") {
                contentType(ContentType.Application.Json)
                setBody(jsonBody)
            }
            Log.d("ApiService", "Response: ${response.status}")
            response.status.isSuccess()
        } catch (e: Exception) {
            Log.e("ApiService", "Error submitting loan application", e)
            false
        }
    }
}

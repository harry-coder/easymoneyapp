package com.tech.easymoney.data.network

import android.util.Log
import com.tech.easymoney.data.model.LoanApplicationRequest
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object ApiService {
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }

    private const val BASE_URL = "https://api.easymoneys.in"

    suspend fun submitLoanApplication(request: LoanApplicationRequest): Boolean {
        return try {
            val response = client.post("$BASE_URL/api/v1/loan-applications") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            Log.d("ApiService", "Response: ${response.status}")
            Log.d("ApiService", "Response: ${response.toString()}")
            response.status.isSuccess()
        } catch (e: Exception) {
            Log.e("ApiService", "Error submitting loan application", e)
            false
        }
    }
}

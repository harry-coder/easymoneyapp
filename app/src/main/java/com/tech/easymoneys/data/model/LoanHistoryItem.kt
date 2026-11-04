package com.tech.easymoneys.data.model


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import androidx.annotation.Keep

@Keep
@Serializable
data class LoanHistoryItem(
    @SerialName("data")
    val `data`: List<Data> = listOf(),
    @SerialName("success")
    val success: Boolean = false
) {
    @Keep
    @Serializable
    data class Data(
        @SerialName("aadhar")
        val aadhar: String? = "",
        @SerialName("address")
        val address: String? = "",
        @SerialName("amount")
        val amount: String? = "",
        @SerialName("call_remarks")
        val callRemarks: String? = "",
        @SerialName("call_status")
        val callStatus: String? = "",
        @SerialName("city")
        val city: String? = "",
        @SerialName("comp_address")
        val compAddress: String? = "",
        @SerialName("dob")
        val dob: String? = "",
        @SerialName("duration")
        val duration: String? = "",
        @SerialName("email")
        val email: String? = "",
        @SerialName("emp_type")
        val empType: String? = "",
        @SerialName("first_name")
        val firstName: String? = "",
        @SerialName("id")
        val id: String? = "",
        @SerialName("income")
        val income: String? = "",
        @SerialName("last_name")
        val lastName: String? = "",
        @SerialName("lead_date")
        val leadDate: String? = "",
        @SerialName("loan_number")
        val loanNumber: String? = "",
        @SerialName("martial_status")
        val martialStatus: String? = "",
        @SerialName("mobile")
        val mobile: String? = "",
        @SerialName("pan")
        val pan: String? = "",
        @SerialName("pincode")
        val pincode: String? = "",
        @SerialName("purpose")
        val purpose: String? = "",
        @SerialName("ref_mobile_1")
        val refMobile1: String? = "",
        @SerialName("ref_mobile_2")
        val refMobile2: String? = "",
        @SerialName("ref_mobile_3")
        val refMobile3: String? = "",
        @SerialName("ref_mobile_4")
        val refMobile4: String? = "",
        @SerialName("reference_name")
        val referenceName: String? = "",
        @SerialName("reference_name_2")
        val referenceName2: String? = "",
        @SerialName("reference_name_3")
        val referenceName3: String? = "",
        @SerialName("rejection_reason")
        val rejectionReason: String? = "",
        @SerialName("rejection_remarks")
        val rejectionRemarks: String? = "",
        @SerialName("resi_address")
        val resiAddress: String? = "",
        @SerialName("state")
        val state: String? = "",
        @SerialName("status")
        val status: Int? = 0
    )
}
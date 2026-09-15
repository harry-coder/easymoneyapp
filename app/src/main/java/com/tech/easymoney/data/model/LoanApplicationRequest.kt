package com.tech.easymoney.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LoanApplicationRequest(
    val employmentType: String,
    val purposeOfLoan: String,
    val loanAmount: String,
    val aadharNumber: String,
    val panNumber: String,
    val firstName: String,
    val lastName: String,
    val dob: String,
    val maritalStatus: String,
    val email: String,
    val mobileNumber: String,
    val presentAddress: String,
    val state: String,
    val city: String,
    val zipcode: String,
    val monthlyIncome: String,
    val userLocation: String,
    val contacts: List<ContactInfo>,
    val panImageUrl: String? = null,
    val bankStatementUrl: String? = null,
    val salarySlip1Url: String? = null,
    val salarySlip2Url: String? = null,
    val salarySlip3Url: String? = null
)

@Serializable
data class ContactInfo(
    val name: String,
    val contact: String
)

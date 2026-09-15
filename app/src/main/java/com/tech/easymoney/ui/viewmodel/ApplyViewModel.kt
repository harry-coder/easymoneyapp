package com.tech.easymoney.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.FirebaseStorage
import com.tech.easymoney.data.model.ContactInfo
import com.tech.easymoney.data.model.LoanApplicationRequest
import com.tech.easymoney.data.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

enum class ApplyStep {
    PERSONAL,
    LOAN_DETAILS,
    KYC,
    SUCCESS
}

data class ApplyUiState(
    val currentStep: ApplyStep = ApplyStep.PERSONAL,
    // Personal Details
    val firstName: String = "",
    val lastName: String = "",
    val dob: String = "",
    val maritalStatus: String = "",
    val mobile: String = "",
    val email: String = "",
    val presentAddress: String = "",
    val state: String = "",
    val city: String = "",
    val zipcode: String = "",
    // Loan & Employment Details
    val employmentType: String = "",
    val purposeOfLoan: String = "",
    val loanAmount: String = "",
    val monthlyIncome: String = "",
    // KYC
    val panNumber: String = "",
    val aadhaarNumber: String = "",
    // Documents
    val panUri: Uri? = null,
    val bankStatementUri: Uri? = null,
    val salarySlips: List<Uri> = emptyList(),
    // App State
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)

class ApplyViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ApplyUiState())
    val uiState: StateFlow<ApplyUiState> = _uiState.asStateFlow()

    fun updateFirstName(value: String) = _uiState.update { it.copy(firstName = value) }
    fun updateLastName(value: String) = _uiState.update { it.copy(lastName = value) }
    fun updateDob(value: String) = _uiState.update { it.copy(dob = value) }
    fun updateMaritalStatus(value: String) = _uiState.update { it.copy(maritalStatus = value) }
    fun updateMobile(value: String) = _uiState.update { it.copy(mobile = value) }
    fun updateEmail(value: String) = _uiState.update { it.copy(email = value) }
    fun updatePresentAddress(value: String) = _uiState.update { it.copy(presentAddress = value) }
    fun updateState(value: String) = _uiState.update { it.copy(state = value) }
    fun updateCity(value: String) = _uiState.update { it.copy(city = value) }
    fun updateZipcode(value: String) = _uiState.update { it.copy(zipcode = value) }
    fun updateEmploymentType(value: String) = _uiState.update { it.copy(employmentType = value) }
    fun updatePurposeOfLoan(value: String) = _uiState.update { it.copy(purposeOfLoan = value) }
    fun updateLoanAmount(value: String) = _uiState.update { it.copy(loanAmount = value) }
    fun updateMonthlyIncome(value: String) = _uiState.update { it.copy(monthlyIncome = value) }
    fun updatePanNumber(value: String) = _uiState.update { it.copy(panNumber = value.uppercase()) }
    fun updateAadhaarNumber(value: String) = _uiState.update { it.copy(aadhaarNumber = value) }

    fun updatePanUri(uri: Uri?) = _uiState.update { it.copy(panUri = uri) }
    fun updateBankStatementUri(uri: Uri?) = _uiState.update { it.copy(bankStatementUri = uri) }
    fun addSalarySlip(uri: Uri) = _uiState.update { 
        if (it.salarySlips.size < 3) it.copy(salarySlips = it.salarySlips + uri) else it 
    }
    fun removeSalarySlip(index: Int) = _uiState.update { 
        it.copy(salarySlips = it.salarySlips.toMutableList().apply { removeAt(index) }) 
    }

    fun nextStep(userLocation: String = "Unknown", contacts: List<ContactInfo> = emptyList()) {
        val currentState = _uiState.value
        when (currentState.currentStep) {
            ApplyStep.PERSONAL -> {
                if (validatePersonal()) {
                    _uiState.update { it.copy(currentStep = ApplyStep.LOAN_DETAILS, errorMessage = null) }
                }
            }
            ApplyStep.LOAN_DETAILS -> {
                if (validateLoanDetails()) {
                    _uiState.update { it.copy(currentStep = ApplyStep.KYC, errorMessage = null) }
                }
            }
            ApplyStep.KYC -> {
                if (validateKyc()) {
                    submitApplication(userLocation, contacts)
                }
            }
            ApplyStep.SUCCESS -> {}
        }
    }

    private fun submitApplication(userLocation: String, contacts: List<ContactInfo>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = "Uploading documents...") }
            
            val s = _uiState.value
            val panRoot = s.panNumber.lowercase()
            
            // Upload Files
            val panUrl = s.panUri?.let { uploadFile(it, "$panRoot/pan/pan.jpg") }
            val bankUrl = s.bankStatementUri?.let { uploadFile(it, "$panRoot/bank/statement.pdf") }
            val slip1 = s.salarySlips.getOrNull(0)?.let { uploadFile(it, "$panRoot/salary/slip1.jpg") }
            val slip2 = s.salarySlips.getOrNull(1)?.let { uploadFile(it, "$panRoot/salary/slip2.jpg") }
            val slip3 = s.salarySlips.getOrNull(2)?.let { uploadFile(it, "$panRoot/salary/slip3.jpg") }

            val request = LoanApplicationRequest(
                employmentType = s.employmentType,
                purposeOfLoan = s.purposeOfLoan,
                loanAmount = s.loanAmount,
                aadharNumber = s.aadhaarNumber,
                panNumber = s.panNumber,
                firstName = s.firstName,
                lastName = s.lastName,
                dob = s.dob,
                maritalStatus = s.maritalStatus,
                email = s.email,
                mobileNumber = s.mobile,
                presentAddress = s.presentAddress,
                state = s.state,
                city = s.city,
                zipcode = s.zipcode,
                monthlyIncome = s.monthlyIncome,
                userLocation = userLocation,
                contacts = contacts,
                panImageUrl = panUrl,
                bankStatementUrl = bankUrl,
                salarySlip1Url = slip1,
                salarySlip2Url = slip2,
                salarySlip3Url = slip3
            )

            _uiState.update { it.copy(errorMessage = "Submitting application...") }
            val success = ApiService.submitLoanApplication(request)
            if (success) {
                _uiState.update { it.copy(currentStep = ApplyStep.SUCCESS, isSubmitting = false) }
            } else {
                _uiState.update { it.copy(isSubmitting = false, errorMessage = "Submission failed. Please try again.") }
            }
        }
    }

    private suspend fun uploadFile(uri: Uri, path: String): String? {
        return try {
            val storageRef = FirebaseStorage.getInstance().reference.child(path)
            storageRef.putFile(uri).await()
            storageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            null
        }
    }

    fun previousStep() {
        _uiState.update { state ->
            val prevStep = when (state.currentStep) {
                ApplyStep.LOAN_DETAILS -> ApplyStep.PERSONAL
                ApplyStep.KYC -> ApplyStep.LOAN_DETAILS
                else -> state.currentStep
            }
            state.copy(currentStep = prevStep, errorMessage = null)
        }
    }

    private fun validatePersonal(): Boolean {
        val s = _uiState.value
        return when {
            s.firstName.isBlank() || s.lastName.isBlank() -> { _uiState.update { it.copy(errorMessage = "Name is required") }; false }
            s.dob.isBlank() -> { _uiState.update { it.copy(errorMessage = "DOB is required") }; false }
            s.mobile.length != 10 -> { _uiState.update { it.copy(errorMessage = "Invalid mobile number") }; false }
            !s.email.contains("@") -> { _uiState.update { it.copy(errorMessage = "Invalid email") }; false }
            s.presentAddress.isBlank() -> { _uiState.update { it.copy(errorMessage = "Address is required") }; false }
            s.state.isBlank() || s.city.isBlank() || s.zipcode.isBlank() -> { _uiState.update { it.copy(errorMessage = "State/City/Zip required") }; false }
            else -> true
        }
    }

    private fun validateLoanDetails(): Boolean {
        val s = _uiState.value
        return when {
            s.employmentType.isBlank() -> { _uiState.update { it.copy(errorMessage = "Employment type required") }; false }
            s.loanAmount.isBlank() || s.monthlyIncome.isBlank() -> { _uiState.update { it.copy(errorMessage = "Income & Amount required") }; false }
            s.purposeOfLoan.isBlank() -> { _uiState.update { it.copy(errorMessage = "Purpose required") }; false }
            else -> true
        }
    }

    private fun validateKyc(): Boolean {
        val s = _uiState.value
        val panRegex = "[A-Z]{5}[0-9]{4}[A-Z]{1}".toRegex()
        return when {
            !s.panNumber.matches(panRegex) -> { _uiState.update { it.copy(errorMessage = "Invalid PAN format") }; false }
            s.aadhaarNumber.length != 12 -> { _uiState.update { it.copy(errorMessage = "Invalid Aadhaar") }; false }
            else -> true
        }
    }

    fun reset() {
        _uiState.value = ApplyUiState()
    }
}

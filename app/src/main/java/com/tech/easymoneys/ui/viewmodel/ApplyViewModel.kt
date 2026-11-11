package com.tech.easymoneys.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.storageMetadata
import com.tech.easymoneys.data.model.ContactInfo
import com.tech.easymoneys.data.model.LoanApplicationRequest
import com.tech.easymoneys.data.network.ApiService
import com.tech.easymoneys.utils.AuthPreferences
import com.tech.easymoneys.utils.LoggerUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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

class ApplyViewModel(application: Application) : AndroidViewModel(application) {
    private companion object {
        const val TAG = "ApplyViewModel"
    }

    private val authPrefs = AuthPreferences(application)
    private val _uiState = MutableStateFlow(ApplyUiState())
    val uiState: StateFlow<ApplyUiState> = _uiState.asStateFlow()

    // Mobile number saved at login; the form shows it pre-filled and read-only
    private var loggedInMobile: String = ""

    init {
        viewModelScope.launch {
            loggedInMobile = authPrefs.userMobile.first().orEmpty()
            _uiState.update { it.copy(mobile = loggedInMobile) }
        }
    }

    fun updateFirstName(value: String) = _uiState.update { it.copy(firstName = value) }
    fun updateLastName(value: String) = _uiState.update { it.copy(lastName = value) }
    fun updateDob(value: String) = _uiState.update { it.copy(dob = value) }
    fun updateMaritalStatus(value: String) = _uiState.update { it.copy(maritalStatus = value) }
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

    fun setErrorMessage(message: String) = _uiState.update { it.copy(errorMessage = message) }

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
            
            // Upload every selected document; stop if any of them fails so the
            // application is never submitted with documents silently missing
            val uploads = buildList {
                s.panUri?.let { add(Triple(it, "$panRoot/pan/pan.pdf", "PAN card")) }
                s.bankStatementUri?.let { add(Triple(it, "$panRoot/bank/statement.pdf", "Bank statement")) }
                s.salarySlips.forEachIndexed { i, uri ->
                    add(Triple(uri, "$panRoot/salary/slip${i + 1}.pdf", "Salary slip ${i + 1}"))
                }
            }
            val docsPath = mutableListOf<String>()
            for ((index, upload) in uploads.withIndex()) {
                val (uri, path, name) = upload
                _uiState.update { it.copy(errorMessage = "Uploading $name (${index + 1}/${uploads.size})...") }
                try {
                    docsPath += uploadFile(uri, path)
                } catch (e: Exception) {
                    LoggerUtils.logErr(TAG, "Upload failed for $path", e)
                    _uiState.update {
                        it.copy(isSubmitting = false, errorMessage = "Couldn't upload $name: ${uploadErrorMessage(e)}")
                    }
                    return@launch
                }
            }

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
                docs_path = docsPath
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

    /** Uploads [uri] to Firebase Storage at [path] and returns its download URL; throws on failure. */
    private suspend fun uploadFile(uri: Uri, path: String): String {
        val storageRef = FirebaseStorage.getInstance().reference.child(path)
        storageRef.putFile(uri, storageMetadata { contentType = "application/pdf" }).await()
        return storageRef.downloadUrl.await().toString()
    }

    private fun uploadErrorMessage(e: Exception): String =
        when ((e as? StorageException)?.errorCode) {
            StorageException.ERROR_NOT_AUTHENTICATED,
            StorageException.ERROR_NOT_AUTHORIZED -> "storage permission denied. Please contact support."
            StorageException.ERROR_RETRY_LIMIT_EXCEEDED -> "network problem. Please check your connection and try again."
            StorageException.ERROR_QUOTA_EXCEEDED -> "storage quota exceeded."
            else -> e.message ?: "unknown error"
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
            s.salarySlips.isEmpty() -> { _uiState.update { it.copy(errorMessage = "Please upload at least one salary slip (PDF)") }; false }
            else -> true
        }
    }

    fun reset() {
        _uiState.value = ApplyUiState(mobile = loggedInMobile)
    }
}

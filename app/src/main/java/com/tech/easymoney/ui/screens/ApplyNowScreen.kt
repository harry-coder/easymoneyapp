package com.tech.easymoney.ui.screens

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.tech.easymoney.ui.viewmodel.ApplyStep
import com.tech.easymoney.ui.viewmodel.ApplyViewModel
import com.tech.easymoney.utils.DeviceDataHelper

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ApplyNowScreen(
    onDone: () -> Unit = {},
    viewModel: ApplyViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val permissionsToRequest = mutableListOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_PHONE_STATE
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            add(Manifest.permission.READ_PHONE_NUMBERS)
        }
    }

    val permissionState = rememberMultiplePermissionsState(permissions = permissionsToRequest)

    LaunchedEffect(Unit) {
        permissionState.launchMultiplePermissionRequest()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Loan Application", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (uiState.currentStep != ApplyStep.PERSONAL && uiState.currentStep != ApplyStep.SUCCESS) {
                        IconButton(onClick = { viewModel.previousStep() }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (!permissionState.allPermissionsGranted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        Icons.Rounded.Security,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Permissions Required",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "To process your loan application, we need access to your contacts, location, and phone details for verification.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                    Button(onClick = { permissionState.launchMultiplePermissionRequest() }) {
                        Text("Grant Permissions")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (uiState.currentStep != ApplyStep.SUCCESS) {
                    LinearProgressIndicator(
                        progress = {
                            when (uiState.currentStep) {
                                ApplyStep.PERSONAL -> 0.33f
                                ApplyStep.LOAN_DETAILS -> 0.66f
                                ApplyStep.KYC -> 1f
                                else -> 1f
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                AnimatedContent(
                    targetState = uiState.currentStep,
                    label = "FormStepTransition",
                    modifier = Modifier.weight(1f)
                ) { step ->
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                        Box(modifier = Modifier.widthIn(max = 600.dp)) {
                            when (step) {
                                ApplyStep.PERSONAL -> PersonalDetailsStep(viewModel)
                                ApplyStep.LOAN_DETAILS -> LoanDetailsStep(viewModel)
                                ApplyStep.KYC -> KycStep(viewModel)
                                ApplyStep.SUCCESS -> SuccessStep(onDone)
                            }
                        }
                    }
                }

                if (uiState.currentStep != ApplyStep.SUCCESS) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Box(modifier = Modifier.widthIn(max = 600.dp)) {
                            BottomActionBar(
                                errorMessage = uiState.errorMessage,
                                isSubmitting = uiState.isSubmitting,
                                onNext = {
                                    if (uiState.currentStep == ApplyStep.KYC) {
                                        DeviceDataHelper.getLastKnownLocation(context) { location ->
                                            val contacts = DeviceDataHelper.getContacts(context)
                                            viewModel.nextStep(location, contacts)
                                        }
                                    } else {
                                        viewModel.nextStep()
                                    }
                                },
                                isLastStep = uiState.currentStep == ApplyStep.KYC
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PersonalDetailsStep(viewModel: ApplyViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Personal Information", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = uiState.firstName,
            onValueChange = { viewModel.updateFirstName(it) },
            label = { Text("First Name") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Rounded.Person, null) }
        )

        OutlinedTextField(
            value = uiState.lastName,
            onValueChange = { viewModel.updateLastName(it) },
            label = { Text("Last Name") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Rounded.Person, null) }
        )

        OutlinedTextField(
            value = uiState.dob,
            onValueChange = { viewModel.updateDob(it) },
            label = { Text("Date of Birth (DD/MM/YYYY)") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Rounded.CalendarMonth, null) }
        )

        OutlinedTextField(
            value = uiState.maritalStatus,
            onValueChange = { viewModel.updateMaritalStatus(it) },
            label = { Text("Marital Status") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Rounded.Group, null) }
        )

        OutlinedTextField(
            value = uiState.mobile,
            onValueChange = { viewModel.updateMobile(it) },
            label = { Text("Mobile Number") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            leadingIcon = { Icon(Icons.Rounded.Phone, null) }
        )

        OutlinedTextField(
            value = uiState.email,
            onValueChange = { viewModel.updateEmail(it) },
            label = { Text("Email Address") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            leadingIcon = { Icon(Icons.Rounded.Email, null) }
        )

        OutlinedTextField(
            value = uiState.presentAddress,
            onValueChange = { viewModel.updatePresentAddress(it) },
            label = { Text("Present Address") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            leadingIcon = { Icon(Icons.Rounded.Home, null) }
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = uiState.city,
                onValueChange = { viewModel.updateCity(it) },
                label = { Text("City") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = uiState.state,
                onValueChange = { viewModel.updateState(it) },
                label = { Text("State") },
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = uiState.zipcode,
            onValueChange = { viewModel.updateZipcode(it) },
            label = { Text("Zipcode") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = { Icon(Icons.Rounded.Pin, null) }
        )
    }
}

@Composable
fun LoanDetailsStep(viewModel: ApplyViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Loan & Employment Details", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = uiState.employmentType,
            onValueChange = { viewModel.updateEmploymentType(it) },
            label = { Text("Employment Type") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Rounded.Work, null) }
        )

        OutlinedTextField(
            value = uiState.monthlyIncome,
            onValueChange = { viewModel.updateMonthlyIncome(it) },
            label = { Text("Monthly Income") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = { Icon(Icons.Rounded.Payments, null) }
        )

        OutlinedTextField(
            value = uiState.purposeOfLoan,
            onValueChange = { viewModel.updatePurposeOfLoan(it) },
            label = { Text("Purpose of Loan") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Rounded.QuestionMark, null) }
        )

        OutlinedTextField(
            value = uiState.loanAmount,
            onValueChange = { viewModel.updateLoanAmount(it) },
            label = { Text("Loan Amount") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = { Icon(Icons.Rounded.CurrencyRupee, null) }
        )
    }
}

@Composable
fun KycStep(viewModel: ApplyViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    val panLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        viewModel.updatePanUri(it)
    }
    val bankLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        viewModel.updateBankStatementUri(it)
    }
    val salaryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.addSalarySlip(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("KYC Verification", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = uiState.panNumber,
            onValueChange = { viewModel.updatePanNumber(it) },
            label = { Text("PAN Number") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Rounded.Badge, null) }
        )

        OutlinedTextField(
            value = uiState.aadhaarNumber,
            onValueChange = { viewModel.updateAadhaarNumber(it) },
            label = { Text("Aadhaar Number") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = { Icon(Icons.Rounded.Fingerprint, null) }
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text("Upload Documents", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        DocumentPicker(
            label = "PAN Card (Image)",
            icon = Icons.Rounded.Badge,
            selectedUri = uiState.panUri,
            onPick = { panLauncher.launch("image/*") }
        )

        DocumentPicker(
            label = "Bank Statement (PDF/Image)",
            icon = Icons.Rounded.Description,
            selectedUri = uiState.bankStatementUri,
            onPick = { bankLauncher.launch("*/*") }
        )

        Text("Salary Slips (Up to 3)", style = MaterialTheme.typography.bodyMedium)
        uiState.salarySlips.forEachIndexed { index, uri ->
            DocumentPicker(
                label = "Salary Slip ${index + 1}",
                icon = Icons.Rounded.Payments,
                selectedUri = uri,
                onPick = { /* Can't re-pick individually easily in this simple UI, but could remove and re-add */ },
                onRemove = { viewModel.removeSalarySlip(index) }
            )
        }

        if (uiState.salarySlips.size < 3) {
            OutlinedButton(
                onClick = { salaryLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.Add, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Salary Slip")
            }
        }
    }
}

@Composable
fun DocumentPicker(
    label: String,
    icon: ImageVector,
    selectedUri: Uri?,
    onPick: () -> Unit,
    onRemove: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(onClick = onPick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(
                text = if (selectedUri != null) "File selected" else "Tap to upload",
                style = MaterialTheme.typography.bodySmall,
                color = if (selectedUri != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        }
        if (selectedUri != null) {
            Icon(Icons.Rounded.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
            if (onRemove != null) {
                IconButton(onClick = onRemove) {
                    Icon(Icons.Rounded.Close, null, tint = MaterialTheme.colorScheme.error)
                }
            }
        } else {
            Icon(Icons.Rounded.FileUpload, null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun SuccessStep(onDone: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Rounded.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text("Application Submitted!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Our team will review your details soon.", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onDone) {
            Text("Go to Home")
        }
    }
}

@Composable
fun BottomActionBar(
    errorMessage: String?,
    isSubmitting: Boolean,
    onNext: () -> Unit,
    isLastStep: Boolean
) {
    Surface(
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            Button(
                onClick = onNext,
                enabled = !isSubmitting,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (isLastStep) "Submit Application" else "Next")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, null)
                }
            }
        }
    }
}

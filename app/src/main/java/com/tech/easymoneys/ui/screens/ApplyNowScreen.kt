package com.tech.easymoneys.ui.screens

import android.Manifest
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsFocusedAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.tech.easymoneys.ui.viewmodel.ApplyStep
import com.tech.easymoneys.ui.viewmodel.ApplyViewModel
import com.tech.easymoneys.utils.DeviceDataHelper
import java.util.Calendar

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

    var locationEnabled by remember { mutableStateOf(isLocationEnabled(context)) }
    
    LaunchedEffect(Unit) {
        locationEnabled = isLocationEnabled(context)
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

                if (!locationEnabled) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Rounded.LocationOff, contentDescription = null)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Location Off",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Please enable location before submitting",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            TextButton(
                                onClick = {
                                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                                }
                            ) {
                                Text("Enable")
                            }
                        }
                    }
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
                                    locationEnabled = isLocationEnabled(context)
                                    if (!locationEnabled && uiState.currentStep == ApplyStep.KYC) {
                                        viewModel.setErrorMessage("Please enable location before submitting")
                                    } else if (uiState.currentStep == ApplyStep.KYC) {
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
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    
    var showDatePicker by remember { mutableStateOf(false) }
    
    if (showDatePicker) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val dobString = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                viewModel.updateDob(dobString)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).also {
            it.show()
            showDatePicker = false
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        StepHeader("Personal Information", "Tell us a little about yourself")

        FormTextField(
            value = uiState.firstName,
            onValueChange = { viewModel.updateFirstName(it) },
            label = "First Name",
            placeholder = "e.g. Rahul",
            leadingIcon = Icons.Rounded.Person,
            capitalization = KeyboardCapitalization.Words
        )

        FormTextField(
            value = uiState.lastName,
            onValueChange = { viewModel.updateLastName(it) },
            label = "Last Name",
            placeholder = "e.g. Sharma",
            leadingIcon = Icons.Rounded.Person,
            capitalization = KeyboardCapitalization.Words
        )

        FormTextField(
            value = uiState.dob,
            onValueChange = {},
            label = "Date of Birth",
            placeholder = "DD/MM/YYYY",
            leadingIcon = Icons.Rounded.CalendarMonth,
            readOnly = true,
            onClick = { showDatePicker = true },
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Rounded.Edit, contentDescription = "Pick date")
                }
            }
        )

        FormTextField(
            value = uiState.maritalStatus,
            onValueChange = { viewModel.updateMaritalStatus(it) },
            label = "Marital Status",
            placeholder = "Single / Married",
            leadingIcon = Icons.Rounded.Group,
            capitalization = KeyboardCapitalization.Words
        )

        FormTextField(
            value = uiState.mobile,
            onValueChange = { viewModel.updateMobile(it) },
            label = "Mobile Number",
            placeholder = "10-digit number",
            leadingIcon = Icons.Rounded.Phone,
            prefix = "+91 ",
            keyboardType = KeyboardType.Phone
        )

        FormTextField(
            value = uiState.email,
            onValueChange = { viewModel.updateEmail(it) },
            label = "Email Address",
            placeholder = "name@example.com",
            leadingIcon = Icons.Rounded.Email,
            keyboardType = KeyboardType.Email
        )

        FormTextField(
            value = uiState.presentAddress,
            onValueChange = { viewModel.updatePresentAddress(it) },
            label = "Present Address",
            placeholder = "House no., street, area",
            leadingIcon = Icons.Rounded.Home,
            capitalization = KeyboardCapitalization.Sentences,
            minLines = 2
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FormTextField(
                value = uiState.city,
                onValueChange = { viewModel.updateCity(it) },
                label = "City",
                placeholder = "City",
                capitalization = KeyboardCapitalization.Words,
                modifier = Modifier.weight(1f)
            )
            FormTextField(
                value = uiState.state,
                onValueChange = { viewModel.updateState(it) },
                label = "State",
                placeholder = "State",
                capitalization = KeyboardCapitalization.Words,
                modifier = Modifier.weight(1f)
            )
        }

        FormTextField(
            value = uiState.zipcode,
            onValueChange = { viewModel.updateZipcode(it) },
            label = "Zipcode",
            placeholder = "6-digit PIN code",
            leadingIcon = Icons.Rounded.Pin,
            keyboardType = KeyboardType.Number
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
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        StepHeader("Loan & Employment Details", "Help us find the right loan for you")

        FormTextField(
            value = uiState.employmentType,
            onValueChange = { viewModel.updateEmploymentType(it) },
            label = "Employment Type",
            placeholder = "Salaried / Self-employed",
            leadingIcon = Icons.Rounded.Work,
            capitalization = KeyboardCapitalization.Words
        )

        FormTextField(
            value = uiState.monthlyIncome,
            onValueChange = { viewModel.updateMonthlyIncome(it) },
            label = "Monthly Income",
            placeholder = "0",
            leadingIcon = Icons.Rounded.Payments,
            prefix = "₹ ",
            keyboardType = KeyboardType.Number
        )

        FormTextField(
            value = uiState.purposeOfLoan,
            onValueChange = { viewModel.updatePurposeOfLoan(it) },
            label = "Purpose of Loan",
            placeholder = "e.g. Education, Medical",
            leadingIcon = Icons.Rounded.QuestionMark,
            capitalization = KeyboardCapitalization.Sentences
        )

        FormTextField(
            value = uiState.loanAmount,
            onValueChange = { viewModel.updateLoanAmount(it) },
            label = "Loan Amount",
            placeholder = "0",
            leadingIcon = Icons.Rounded.CurrencyRupee,
            prefix = "₹ ",
            keyboardType = KeyboardType.Number
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
        StepHeader("KYC Verification", "Your details are encrypted and kept secure")

        FormTextField(
            value = uiState.panNumber,
            onValueChange = { viewModel.updatePanNumber(it) },
            label = "PAN Number",
            placeholder = "ABCDE1234F",
            leadingIcon = Icons.Rounded.Badge,
            capitalization = KeyboardCapitalization.Characters
        )

        FormTextField(
            value = uiState.aadhaarNumber,
            onValueChange = { viewModel.updateAadhaarNumber(it) },
            label = "Aadhaar Number",
            placeholder = "12-digit number",
            leadingIcon = Icons.Rounded.Fingerprint,
            keyboardType = KeyboardType.Number
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text("Upload Documents", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        DocumentPicker(
            label = "PAN Card (PDF)",
            icon = Icons.Rounded.Badge,
            selectedUri = uiState.panUri,
            onPick = { panLauncher.launch("application/pdf") }
        )

        DocumentPicker(
            label = "Bank Statement (PDF)",
            icon = Icons.Rounded.Description,
            selectedUri = uiState.bankStatementUri,
            onPick = { bankLauncher.launch("application/pdf") }
        )

        Text("Salary Slips (PDF, up to 3)", style = MaterialTheme.typography.bodyMedium)
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
                onClick = { salaryLauncher.launch("application/pdf") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.Add, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Salary Slip (PDF)")
            }
        }
    }
}

@Composable
private fun StepHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    readOnly: Boolean = false,
    minLines: Int = 1,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // Text fields swallow click modifiers, so read-only fields like DOB listen for presses instead
    if (onClick != null) {
        LaunchedEffect(interactionSource) {
            interactionSource.interactions.collect {
                if (it is PressInteraction.Release) onClick()
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = placeholder?.let { { Text(it) } },
            leadingIcon = leadingIcon?.let {
                {
                    Icon(
                        it,
                        contentDescription = null,
                        tint = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            trailingIcon = trailingIcon,
            prefix = prefix?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, capitalization = capitalization),
            readOnly = readOnly,
            singleLine = minLines == 1,
            minLines = minLines,
            shape = RoundedCornerShape(16.dp),
            interactionSource = interactionSource,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
                focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.outline,
                focusedPlaceholderColor = MaterialTheme.colorScheme.outline
            )
        )
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

private fun isLocationEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        locationManager.isLocationEnabled
    } else {
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}

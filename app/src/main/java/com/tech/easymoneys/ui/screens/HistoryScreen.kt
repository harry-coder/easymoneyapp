package com.tech.easymoneys.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tech.easymoneys.data.model.LoanHistoryItem
import com.tech.easymoneys.ui.theme.EasyMoneyTheme
import com.tech.easymoneys.ui.viewmodel.HistoryViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val canSearch = !uiState.isLoading && uiState.pan.length == 10
    val search = {
        focusManager.clearFocus()
        viewModel.fetchHistory()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HistoryHeader(
                pan = uiState.pan,
                onPanChange = { if (it.length <= 10) viewModel.updatePan(it) },
                isLoading = uiState.isLoading,
                canSearch = canSearch,
                onSearch = search
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                when {
                    uiState.isLoading -> LoadingList()
                    uiState.items.data.isEmpty() -> EmptyState(message = uiState.error)
                    else -> LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 600.dp),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Text(
                                text = "${uiState.items.data.size} application${if (uiState.items.data.size == 1) "" else "s"} found",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        items(uiState.items.data, key = { it.id ?: it.hashCode() }) { item ->
                            ApplicationCard(item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryHeader(
    pan: String,
    onPanChange: (String) -> Unit,
    isLoading: Boolean,
    canSearch: Boolean,
    onSearch: () -> Unit
) {
    val headerGradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            Color(0xFF6F8BFF)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(headerGradient)
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.History, contentDescription = null, tint = Color.White)
                }
                Column {
                    Text(
                        text = "Application History",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "Track the status of your loan applications",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(start = 16.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.Badge,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    TextField(
                        value = pan,
                        onValueChange = onPanChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Enter PAN ") },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(onSearch = { if (canSearch) onSearch() }),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )
                    FilledIconButton(
                        onClick = onSearch,
                        enabled = canSearch,
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.Rounded.Search, contentDescription = "Fetch history")
                        }
                    }
                }
            }

            Text(
                text = "${pan.length}/10 characters",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.75f),
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun EmptyState(message: String?) {
    val isError = message != null
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(CircleShape)
                .background(
                    if (isError) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isError) Icons.Rounded.SearchOff else Icons.Rounded.ManageSearch,
                contentDescription = null,
                modifier = Modifier.size(52.dp),
                tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = if (isError) "Nothing here" else "Look up your applications",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message ?: "Enter your PAN number above to see all your past and ongoing loan applications.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LoadingList() {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "shimmerAlpha"
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 600.dp)
            .padding(16.dp)
            .alpha(alpha),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            )
        }
    }
}

fun getLoanStatus(status: Int): String {
    return when (status) {
        0 -> "Under Review"
        1 -> "Approved"
        2 -> "Disbursed"
        3 -> "Rejected"
        4 -> "Part Payment"
        5 -> "Settlement"
        6 -> "Closed"
        7 -> "Call Done"
        8 -> "Call Back"
        9 -> "No Answer"
        10 -> "Not Eligible"
        else -> "UnDefined"
    }
}

private data class StatusStyle(val color: Color, val icon: ImageVector)

private fun statusStyle(status: Int): StatusStyle = when (status) {
    0 -> StatusStyle(Color(0xFF5B6CFF), Icons.Rounded.HourglassTop)
    1 -> StatusStyle(Color(0xFF2E9E5B), Icons.Rounded.CheckCircle)
    2 -> StatusStyle(Color(0xFF00897B), Icons.Rounded.AccountBalanceWallet)
    3 -> StatusStyle(Color(0xFFE53935), Icons.Rounded.Cancel)
    4 -> StatusStyle(Color(0xFFF57C00), Icons.Rounded.Payments)
    5 -> StatusStyle(Color(0xFF1E88E5), Icons.Rounded.Handshake)
    6 -> StatusStyle(Color(0xFF8E24AA), Icons.Rounded.Lock)
    7 -> StatusStyle(Color(0xFF00ACC1), Icons.Rounded.PhoneInTalk)
    8 -> StatusStyle(Color(0xFF43A047), Icons.Rounded.PhoneCallback)
    9 -> StatusStyle(Color(0xFFFFA000), Icons.Rounded.PhoneMissed)
    10 -> StatusStyle(Color(0xFFD32F2F), Icons.Rounded.Block)
    else -> StatusStyle(Color(0xFF78909C), Icons.Rounded.HelpOutline)
}

private fun formatAmount(amount: String?): String {
    val value = amount?.toDoubleOrNull() ?: return "—"
    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    format.maximumFractionDigits = 0
    return format.format(value)
}

@Composable
fun ApplicationCard(item: LoanHistoryItem.Data) {
    val status = item.status ?: 0
    val style = statusStyle(status)
    var expanded by rememberSaveable(item.id) { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(if (expanded) 180f else 0f, label = "arrow")
    val fullName = listOfNotNull(item.firstName, item.lastName)
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .ifBlank { "Applicant" }
    val rejectionText = listOfNotNull(item.rejectionReason, item.rejectionRemarks)
        .filter { it.isNotBlank() }
        .joinToString(" • ")

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 3.dp,
        tonalElevation = 1.dp
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Status accent strip
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(style.color)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(style.color.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(style.icon, contentDescription = null, tint = style.color)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = fullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = item.loanNumber?.takeIf { it.isNotBlank() }?.let { "#$it" } ?: "Application ID pending",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    StatusChip(label = getLoanStatus(status), color = style.color)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Loan Amount",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatAmount(item.amount),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (!item.duration.isNullOrBlank()) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Tenure",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = item.duration,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (!item.leadDate.isNullOrBlank()) {
                        MetaItem(Icons.Rounded.CalendarToday, item.leadDate)
                    }
                    if (!item.purpose.isNullOrBlank()) {
                        MetaItem(Icons.Rounded.Flag, item.purpose, modifier = Modifier.weight(1f, fill = false))
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        Icons.Rounded.KeyboardArrowDown,
                        contentDescription = if (expanded) "Hide details" else "Show details",
                        modifier = Modifier.rotate(arrowRotation),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                        DetailRow("PAN", item.pan)
                        DetailRow("Mobile", item.mobile)
                        DetailRow("Email", item.email)
                        DetailRow("Employment", item.empType)
                        DetailRow("Monthly Income", item.income?.takeIf { it.isNotBlank() }?.let { formatAmount(it) })
                        DetailRow("City", listOfNotNull(item.city, item.state).filter { it.isNotBlank() }.joinToString(", "))
                    }
                }

                if (rejectionText.isNotBlank() && (status == 3 || status == 10)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Info,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = rejectionText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(label: String, color: Color) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun MetaItem(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(15.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    EasyMoneyTheme {
        HistoryScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun ApplicationCardPreview() {
    EasyMoneyTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ApplicationCard(
                LoanHistoryItem.Data(
                    id = "1",
                    firstName = "Rahul",
                    lastName = "Sharma",
                    loanNumber = "EM102394",
                    amount = "25000",
                    duration = "30 days",
                    leadDate = "2026-10-12",
                    purpose = "Medical",
                    pan = "ABCDE1234F",
                    status = 1
                )
            )
        }
    }
}

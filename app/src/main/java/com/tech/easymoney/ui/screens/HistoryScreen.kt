package com.tech.easymoney.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tech.easymoney.ui.theme.EasyMoneyTheme

data class LoanApplication(
    val id: String,
    val amount: String,
    val date: String,
    val status: String,
    val statusColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen() {
    val mockApplications = listOf(
        LoanApplication("APP-10293", "₹50,000", "12 Oct 2023", "Approved", Color(0xFF4CAF50)),
        LoanApplication("APP-10354", "₹1,20,000", "25 Nov 2023", "Pending", Color(0xFFFF9800)),
        LoanApplication("APP-10421", "₹75,000", "05 Dec 2023", "Rejected", Color(0xFFF44336)),
        LoanApplication("APP-10512", "₹30,000", "15 Jan 2024", "Approved", Color(0xFF4CAF50))
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Application History", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(mockApplications) { app ->
                    ApplicationCard(app)
                }
            }
        }
    }
}

@Composable
fun ApplicationCard(app: LoanApplication) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.id,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = app.amount,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Applied on ${app.date}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                color = app.statusColor.copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.medium,
                border = androidx.compose.foundation.BorderStroke(1.dp, app.statusColor.copy(alpha = 0.5f))
            ) {
                Text(
                    text = app.status,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = app.statusColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    EasyMoneyTheme {
        HistoryScreen()
    }
}

package com.tech.easymoney

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.runtime.rememberNavBackStack
import com.tech.easymoney.ui.screens.*
import com.tech.easymoney.ui.theme.EasyMoneyTheme
import com.tech.easymoney.ui.viewmodel.AuthStep
import com.tech.easymoney.ui.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EasyMoneyTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp(authViewModel: AuthViewModel = viewModel()) {
    val authState by authViewModel.uiState.collectAsState()
    val backStack = rememberNavBackStack(Route.Home)
    val currentRoute = backStack.last()

    if (authState.step != AuthStep.AUTHENTICATED) {
        when (authState.step) {
            AuthStep.LOGIN -> LoginScreen(authViewModel)
            AuthStep.OTP -> OtpScreen(authViewModel)
            AuthStep.ONBOARDING -> OnboardingScreen(authViewModel)
            else -> LoginScreen(authViewModel)
        }
    } else {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                TopLevelDestination.entries.forEach { destination ->
                    item(
                        selected = currentRoute == destination.route,
                        onClick = {
                            if (currentRoute != destination.route) {
                                backStack.clear()
                                backStack.add(destination.route)
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.labelText
                            )
                        },
                        label = { Text(destination.labelText) }
                    )
                }
            }
        ) {
            NavDisplay(
                backStack = backStack,
                modifier = Modifier.fillMaxSize()
            ) { route ->
                NavEntry(route) {
                    when (route) {
                        is Route.Home -> HomeScreen(
                            onApplyNowClick = {
                                backStack.clear()
                                backStack.add(Route.ApplyNow)
                            },
                            onLogout = { authViewModel.logout() }
                        )
                        is Route.ApplyNow -> ApplyNowScreen(
                            onDone = {
                                backStack.clear()
                                backStack.add(Route.Home)
                            }
                        )
                        is Route.History -> HistoryScreen()
                    }
                }
            }
        }
    }
}

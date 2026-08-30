package com.tech.easymoney

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Payment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Route : NavKey {
    @Serializable
    data object Home : Route

    @Serializable
    data object ApplyNow : Route

    @Serializable
    data object History : Route
}

enum class TopLevelDestination(
    val route: Route,
    val icon: ImageVector,
    val labelText: String
) {
    HOME(Route.Home, Icons.Rounded.Home, "Home"),
    APPLY(Route.ApplyNow, Icons.Rounded.Payment, "Apply"),
    HISTORY(Route.History, Icons.Rounded.History, "History")
}

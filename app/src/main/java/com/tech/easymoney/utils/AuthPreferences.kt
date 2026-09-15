package com.tech.easymoney.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class AuthPreferences(private val context: Context) {

    companion object {
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val IS_ONBOARDED = booleanPreferencesKey("is_onboarded")
        val USER_MOBILE = stringPreferencesKey("user_mobile")
    }

    val authToken: Flow<String?> = context.dataStore.data.map { it[AUTH_TOKEN] }
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { it[IS_LOGGED_IN] ?: false }
    val isOnboarded: Flow<Boolean> = context.dataStore.data.map { it[IS_ONBOARDED] ?: false }
    val userMobile: Flow<String?> = context.dataStore.data.map { it[USER_MOBILE] }

    suspend fun saveAuthData(token: String, mobile: String, isOnboarded: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN] = token
            preferences[USER_MOBILE] = mobile
            preferences[IS_LOGGED_IN] = true
            preferences[IS_ONBOARDED] = isOnboarded
        }
    }

    suspend fun setOnboarded(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_ONBOARDED] = value
        }
    }

    suspend fun clearAuthData() {
        context.dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN)
            preferences.remove(USER_MOBILE)
            preferences[IS_LOGGED_IN] = false
            // Note: We don't clear IS_ONBOARDED here because the user wants to skip it on re-login
        }
    }
}

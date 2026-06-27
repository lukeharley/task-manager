package com.minimaltask.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "minimal_task_preferences")

enum class AppThemeMode { LIGHT, DARK, COLOR_BLUE, COLOR_GREEN, COLOR_ROSE }

data class UserPreferences(
    val themeMode: AppThemeMode = AppThemeMode.LIGHT,
    val focusMinutes: Int = 25,
    val breakMinutes: Int = 5,
    val premiumActive: Boolean = false
)

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val theme = stringPreferencesKey("theme")
        val focusMinutes = intPreferencesKey("focus_minutes")
        val breakMinutes = intPreferencesKey("break_minutes")
        val premium = booleanPreferencesKey("premium_active")
    }

    val preferences: Flow<UserPreferences> = context.dataStore.data.map { values ->
        UserPreferences(
            themeMode = runCatching {
                AppThemeMode.valueOf(values[Keys.theme] ?: AppThemeMode.LIGHT.name)
            }.getOrDefault(AppThemeMode.LIGHT),
            focusMinutes = values[Keys.focusMinutes] ?: 25,
            breakMinutes = values[Keys.breakMinutes] ?: 5,
            premiumActive = values[Keys.premium] ?: false
        )
    }

    suspend fun setTheme(themeMode: AppThemeMode) {
        context.dataStore.edit { it[Keys.theme] = themeMode.name }
    }

    suspend fun setFocusPreferences(focusMinutes: Int, breakMinutes: Int) {
        context.dataStore.edit {
            it[Keys.focusMinutes] = focusMinutes
            it[Keys.breakMinutes] = breakMinutes
        }
    }

    suspend fun setPremiumActive(active: Boolean) {
        context.dataStore.edit { it[Keys.premium] = active }
    }
}

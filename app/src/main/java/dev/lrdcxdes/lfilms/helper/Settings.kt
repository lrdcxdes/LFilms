package dev.lrdcxdes.lfilms.helper

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.lrdcxdes.lfilms.Theme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val KEY_MIRROR = stringPreferencesKey("mirror")
val KEY_THEME = stringPreferencesKey("theme")

suspend fun setMirror(mirror: String, context: Context) {
    context.dataStore.edit { settings ->
        settings[KEY_MIRROR] = mirror
    }
}

suspend fun getMirror(context: Context): String {
    val mirror = context.dataStore.data.map { preferences ->
        preferences[KEY_MIRROR] ?: ""
    }
    return mirror.first()
}

suspend fun setTheme(theme: Theme, context: Context) {
    context.dataStore.edit { settings ->
        settings[KEY_THEME] = theme.name
    }
}

suspend fun getTheme(context: Context): Theme? {
    val theme = context.dataStore.data.map { preferences ->
        preferences[KEY_THEME] ?: ""
    }
    return when (theme.first()) {
        "LIGHT" -> Theme.LIGHT
        "DARK" -> Theme.DARK
        else -> null
    }
}

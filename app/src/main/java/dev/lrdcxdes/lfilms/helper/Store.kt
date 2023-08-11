package dev.lrdcxdes.lfilms.helper

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore by preferencesDataStore(
    name = "settings"
)

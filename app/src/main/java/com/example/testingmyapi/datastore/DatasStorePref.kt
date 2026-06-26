package com.example.testingmyapi.datastore

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import java.io.ByteArrayOutputStream
import java.util.Base64

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class DataStorePref(private val context: Context) {

    companion object {
        val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
        val USERNAME_KEY = stringPreferencesKey("username")
        val PASSWORD_KEY = stringPreferencesKey("password")
        val USERS_KEY = stringPreferencesKey("users")
        val FAVORITE_NAMES_KEY = stringSetPreferencesKey("favorite_names")
        val APP_LANGUAGE_KEY = stringPreferencesKey("app_language")
        val PROFILE_IMAGE_KEY = stringPreferencesKey("profile_image") // ✅ Tambahkan ini
    }

// ============ PROFILE IMAGE ============

    suspend fun saveProfileImage(imageBase64: String) {
        context.dataStore.edit { preferences ->
            preferences[PROFILE_IMAGE_KEY] = imageBase64
        }
    }

    val profileImageFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PROFILE_IMAGE_KEY] ?: ""
        }

    suspend fun getProfileImage(): String {
        return context.dataStore.data.map { prefs ->
            prefs[PROFILE_IMAGE_KEY] ?: ""
        }.first()
    }

    suspend fun deleteProfileImage() {
        context.dataStore.edit { preferences ->
            preferences.remove(PROFILE_IMAGE_KEY)
        }
    }

    // ============ KONVERSI BITMAP KE BASE64 ============

    fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.getEncoder().encodeToString(byteArray)
    }

    fun base64ToBitmap(base64: String): Bitmap? {
        return try {
            val byteArray = Base64.getDecoder().decode(base64)
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        } catch (e: Exception) {
            null
        }
    }

    // ============ APP LANGUAGE ============

    val appLanguageFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[APP_LANGUAGE_KEY] ?: "en"
        }

    suspend fun saveAppLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[APP_LANGUAGE_KEY] = language
        }
    }

    // ============ FAVORITE NAMES ============

    val favoriteNamesFlow: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            preferences[FAVORITE_NAMES_KEY] ?: emptySet()
        }

    suspend fun toggleFavoriteName(characterName: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[FAVORITE_NAMES_KEY] ?: emptySet()
            val newSet = if (current.contains(characterName)) {
                current - characterName
            } else {
                current + characterName
            }
            preferences[FAVORITE_NAMES_KEY] = newSet
        }
    }

    // ============ AUTHENTICATION ============

    suspend fun registerUser(username: String, password: String) {
        context.dataStore.edit { preferences ->
            val existingUsers = preferences[USERS_KEY] ?: ""
            if (!existingUsers.contains("$username:")) {
                val newUser = "$username:$password,"
                preferences[USERS_KEY] = existingUsers + newUser
            }
        }
    }

    suspend fun isUserRegistered(username: String, password: String): Boolean {
        val users = context.dataStore.data.map { preferences ->
            preferences[USERS_KEY] ?: ""
        }.first()
        return users.contains("$username:$password,")
    }

    suspend fun isUsernameExists(username: String): Boolean {
        val users = context.dataStore.data.map { preferences ->
            preferences[USERS_KEY] ?: ""
        }.first()
        return users.contains("$username:")
    }

    suspend fun saveLoginData(username: String, password: String, isLoggedIn: Boolean = true) {
        context.dataStore.edit { preferences ->
            preferences[USERNAME_KEY] = username
            preferences[PASSWORD_KEY] = password
            preferences[IS_LOGGED_IN_KEY] = isLoggedIn
        }
    }

    val username: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[USERNAME_KEY] ?: ""
        }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_LOGGED_IN_KEY] ?: false
        }

    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun printAllUsers() {
        val users = context.dataStore.data.map { preferences ->
            preferences[USERS_KEY] ?: "EMPTY"
        }.first()
    }
}
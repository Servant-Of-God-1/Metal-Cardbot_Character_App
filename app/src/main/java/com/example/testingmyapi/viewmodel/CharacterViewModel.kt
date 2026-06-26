package com.example.testingmyapi.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testingmyapi.api.ApiConfig
import com.example.testingmyapi.datastore.DataStorePref
import com.example.testingmyapi.model.Category
import com.example.testingmyapi.model.Character
import com.example.testingmyapi.model.CharacterDetail
import com.example.testingmyapi.model.Language
import com.example.testingmyapi.utils.LanguageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UiState(
    val isLoading: Boolean = false,
    val loadingMessage: String = "Loading...",
    val isLoggedIn: Boolean = false,
    val username: String = "",
    val password: String = "",
    val characters: List<Character> = emptyList(),
    val categories: List<Category> = emptyList(),
    val languages: List<Language> = emptyList(),
    val selectedCategory: String? = null,
    val selectedLanguage: String? = null,
    val selectedCharacter: CharacterDetail? = null,
    val isDetailLoading: Boolean = false,
    val error: String? = null,
    val totalItems: Int = 0,
    val isFilterReady: Boolean = false,
    val showFilterSheet: Boolean = false,
    val showLoginDialog: Boolean = false,
    val isLoadingAllCharacters: Boolean = false,
    val favoriteCount: Int = 0,
    val appLanguage: String = "en",
    val showProfile: Boolean = false,
    val loginMessage: String = "",
    val isLogoutSuccess: Boolean = false
)

class CharacterViewModel(private val context: Context) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _allCharacters = MutableStateFlow<List<Character>>(emptyList())
    val allCharacters: StateFlow<List<Character>> = _allCharacters.asStateFlow()

    private val _favoriteNames = MutableStateFlow<Set<String>>(emptySet())
    val favoriteNames: StateFlow<Set<String>> = _favoriteNames.asStateFlow()
    private val _profileImage = MutableStateFlow<String>("")
    val profileImage: StateFlow<String> = _profileImage.asStateFlow()

    private val dataStorePref = DataStorePref(context)
    private var allCharactersCache: List<Character> = emptyList()
    private val TAG = "CharacterVM"

    init {
        checkLoginStatus()

        viewModelScope.launch {
            dataStorePref.favoriteNamesFlow.collect { names ->
                _favoriteNames.value = names
                _uiState.update { it.copy(favoriteCount = names.size) }
            }
        }

        viewModelScope.launch {
            dataStorePref.appLanguageFlow.collect { language ->
                _uiState.update { it.copy(appLanguage = language) }
                if (_uiState.value.isLoggedIn) {
                    fetchAllCharacters()
                }
            }
        }

        viewModelScope.launch {
            dataStorePref.profileImageFlow.collect { imageBase64 ->
                _profileImage.value = imageBase64
            }
        }
    }

    fun getText(key: String, vararg args: Any): String {
        val language = _uiState.value.appLanguage
        return LanguageManager.getText(key, language, *args)
    }

    // ============ APP LANGUAGE ============

    fun changeLanguage(language: String) {
        viewModelScope.launch {
            dataStorePref.saveAppLanguage(language)
        }
    }

    // ============ PROFILE ============

    fun toggleProfile() {
        _uiState.update { it.copy(showProfile = !it.showProfile) }
    }

    fun hideProfile() {
        _uiState.update { it.copy(showProfile = false) }
    }

// ============ PROFILE IMAGE ============

    fun updateProfileImage(bitmap: Bitmap) {
        viewModelScope.launch {
            val base64 = dataStorePref.bitmapToBase64(bitmap)
            dataStorePref.saveProfileImage(base64)
            _profileImage.value = base64
        }
    }

    fun deleteProfileImage() {
        viewModelScope.launch {
            dataStorePref.deleteProfileImage()
            _profileImage.value = ""
        }
    }

    fun getProfileBitmap(): Bitmap? {
        return if (_profileImage.value.isNotEmpty()) {
            dataStorePref.base64ToBitmap(_profileImage.value)
        } else {
            null
        }
    }

    // ============ AUTHENTICATION ============

    private fun checkLoginStatus() {
        viewModelScope.launch {
            dataStorePref.isLoggedIn.collect { isLoggedIn ->
                if (isLoggedIn) {
                    dataStorePref.username.collect { username ->
                        _uiState.update {
                            it.copy(
                                isLoggedIn = true,
                                username = username,
                                showLoginDialog = false
                            )
                        }
                        fetchCategories()
                        fetchLanguages()
                        fetchAllCharacters()
                    }
                } else {
                    _uiState.update { it.copy(showLoginDialog = true) }
                }
            }
        }
    }

    fun showLoginDialog() {
        _uiState.update { it.copy(showLoginDialog = true, error = null) }
    }

    fun hideLoginDialog() {
        _uiState.update { it.copy(showLoginDialog = false) }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {

            if (username.isBlank()) {
                _uiState.update {
                    it.copy(
                        error = "username_empty",
                        showLoginDialog = true,
                        loginMessage = "Username tidak boleh kosong!"
                    )
                }
                return@launch
            }

            if (password.isBlank()) {
                _uiState.update {
                    it.copy(
                        error = "password_empty",
                        showLoginDialog = true,
                        loginMessage = "❌ Password tidak boleh kosong!"
                    )
                }
                return@launch
            }

            val isRegistered = dataStorePref.isUserRegistered(username, password)
            dataStorePref.printAllUsers()

            if (isRegistered) {
                dataStorePref.saveLoginData(username, password, true)
                _uiState.update {
                    it.copy(
                        isLoggedIn = true,
                        username = username,
                        password = password,
                        showLoginDialog = false,
                        error = null,
                        loginMessage = "Login berhasil! Selamat datang, $username"
                    )
                }
                fetchCategories()
                fetchLanguages()
                fetchAllCharacters()
            } else {
                val usernameExists = dataStorePref.isUsernameExists(username)
                _uiState.update {
                    it.copy(
                        error = if (usernameExists) "wrong_password" else "username_not_found",
                        showLoginDialog = true,
                        loginMessage = if (usernameExists)
                            "Password salah! Silakan coba lagi."
                        else
                            "Username '$username' tidak ditemukan!"
                    )
                }
            }
        }
    }
    fun register(username: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            if (username.isBlank()) {
                _uiState.update {
                    it.copy(
                        error = "username_empty",
                        showLoginDialog = true,
                        loginMessage = "Username tidak boleh kosong!"
                    )
                }
                return@launch
            }
            if (password.isBlank()) {
                _uiState.update {
                    it.copy(
                        error = "password_empty",
                        showLoginDialog = true,
                        loginMessage = "Password tidak boleh kosong!"
                    )
                }
                return@launch
            }

            if (password != confirmPassword) {
                _uiState.update {
                    it.copy(
                        error = "password_not_match",
                        showLoginDialog = true,
                        loginMessage = "Password dan konfirmasi password tidak sama!"
                    )
                }
                return@launch
            }

            if (password.length < 6) {
                _uiState.update {
                    it.copy(
                        error = "password_min_6",
                        showLoginDialog = true,
                        loginMessage = "Password minimal 6 karakter!"
                    )
                }
                return@launch
            }

            val usernameExists = dataStorePref.isUsernameExists(username)
            if (usernameExists) {
                _uiState.update {
                    it.copy(
                        error = "username_exists",
                        showLoginDialog = true,
                        loginMessage = "Username '$username' sudah digunakan!"
                    )
                }
                return@launch
            }

            dataStorePref.registerUser(username, password)
            dataStorePref.printAllUsers()
            dataStorePref.saveLoginData(username, password, true)

            _uiState.update {
                it.copy(
                    isLoggedIn = true,
                    username = username,
                    password = password,
                    showLoginDialog = false,
                    error = null,
                    loginMessage = "Pendaftaran berhasil! Selamat datang, $username"
                )
            }

            fetchCategories()
            fetchLanguages()
            fetchAllCharacters()
        }
    }

    fun logout() {
        viewModelScope.launch {
            dataStorePref.clearAll()
            _uiState.value = UiState(
                showLoginDialog = true,
                loginMessage = "Anda telah berhasil logout. Sampai jumpa!",
                isLogoutSuccess = true
            )
            allCharactersCache = emptyList()
            _allCharacters.value = emptyList()
            _favoriteNames.value = emptySet()
            _profileImage.value = ""
            _uiState.update { it.copy(isLogoutSuccess = false) }
        }
    }

    // ============ FILTER ============

    fun toggleFilterSheet() {
        _uiState.update { it.copy(showFilterSheet = !it.showFilterSheet) }
    }

    fun hideFilterSheet() {
        _uiState.update { it.copy(showFilterSheet = false) }
    }

    fun selectCategory(categoryName: String?) {
        _uiState.update {
            it.copy(
                selectedCategory = categoryName,
                characters = emptyList(),
                selectedCharacter = null
            )
        }
        applyFilters()
    }

    private fun applyFilters() {
        val category = _uiState.value.selectedCategory

        if (_allCharacters.value.isEmpty() && allCharactersCache.isNotEmpty()) {
            _allCharacters.value = allCharactersCache
        }

        if (category == null) {
            _uiState.update {
                it.copy(
                    characters = emptyList(),
                    totalItems = 0,
                    isFilterReady = false,
                    error = "please_select_category"
                )
            }
            return
        }

        var filtered = allCharactersCache
        filtered = filtered.filter { it.category == category }
        filtered = filtered.sortedBy { it.name ?: "" }

        _uiState.update {
            it.copy(
                characters = filtered,
                totalItems = filtered.size,
                isFilterReady = true,
                error = null
            )
        }
    }

    // ============ CHARACTER DATA ============

    fun fetchAllCharacters() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isLoadingAllCharacters = true,
                    characters = emptyList(),
                    error = null
                )
            }

            try {
                val allData = mutableListOf<Character>()
                var offset = 0
                val limit = 50
                var hasMore = true

                val selectedLanguage = _uiState.value.appLanguage

                while (hasMore) {
                    val response = ApiConfig.apiService.getInformation(
                        limit = limit,
                        offset = offset,
                        category = null,
                        language = selectedLanguage,
                        name = null
                    )

                    if (response.isSuccessful) {
                        val data = response.body() ?: emptyList()
                        allData.addAll(data)

                        if (data.size < limit) {
                            hasMore = false
                        } else {
                            offset += limit
                        }
                    } else {
                        hasMore = false
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isLoadingAllCharacters = false,
                                error = "Error ${response.code()}"
                            )
                        }
                        return@launch
                    }
                }

                allCharactersCache = allData.sortedBy { it.name ?: "" }
                _allCharacters.value = allCharactersCache

                applyFilters()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoadingAllCharacters = false
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoadingAllCharacters = false,
                        error = "Exception: ${e.message}"
                    )
                }
            }
        }
    }

    fun fetchCategories() {
        viewModelScope.launch {
            try {
                val response = ApiConfig.apiService.getCategories()
                if (response.isSuccessful) {
                    val categories = response.body() ?: emptyList()
                    _uiState.update { it.copy(categories = categories) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Categories error: ${e.message}") }
            }
        }
    }

    fun fetchLanguages() {
        viewModelScope.launch {
            try {
                val response = ApiConfig.apiService.getLanguages()
                if (response.isSuccessful) {
                    val languages = response.body() ?: emptyList()
                    _uiState.update { it.copy(languages = languages) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Languages error: ${e.message}") }
            }
        }
    }

    fun fetchCharacterDetail(characterId: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isDetailLoading = true,
                    error = null,
                    selectedCharacter = null
                )
            }
            try {
                val response = ApiConfig.apiService.getInformationById(characterId)
                if (response.isSuccessful) {
                    val character = response.body()
                    _uiState.update {
                        it.copy(
                            isDetailLoading = false,
                            selectedCharacter = character
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isDetailLoading = false,
                            error = "Detail error: ${response.code()}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isDetailLoading = false,
                        error = "Detail: ${e.message}"
                    )
                }
            }
        }
    }

    fun selectCharacter(characterId: String?) {
        if (characterId != null) {
            fetchCharacterDetail(characterId)
        } else {
            _uiState.update { it.copy(selectedCharacter = null) }
        }
    }

    fun clearSelectedCharacter() {
        _uiState.update { it.copy(selectedCharacter = null) }
    }

    // ============ FAVORITE / WISHLIST ============

    fun getCharacterKey(name: String?): String {
        return name?.trim()?.lowercase() ?: ""
    }

    fun toggleFavorite(characterName: String?) {
        val key = getCharacterKey(characterName)
        if (key.isEmpty()) return

        viewModelScope.launch {
            dataStorePref.toggleFavoriteName(key)
        }
    }

    fun isFavorite(characterName: String?): Boolean {
        val key = getCharacterKey(characterName)
        return _favoriteNames.value.contains(key)
    }

    fun getFavoriteCharacters(): List<Character> {
        val all = _allCharacters.value
        return all.filter { character ->
            _favoriteNames.value.contains(getCharacterKey(character.name))
        }.distinctBy { getCharacterKey(it.name) }
    }
}
package com.example.testingmyapi.utils

object LanguageManager {

    // ============ BAHASA INDONESIA ============
    private val id = mapOf(
        // ===== LOGIN =====
        "login_title" to "Masuk",
        "register_title" to "Daftar",
        "username" to "Nama Pengguna",
        "password" to "Kata Sandi",
        "confirm_password" to "Konfirmasi Kata Sandi",
        "login_button" to "Masuk",
        "register_button" to "Daftar",
        "login_username_placeholder" to "Masukkan nama pengguna",
        "login_password_placeholder" to "Masukkan kata sandi",
        "confirm_password_placeholder" to "Konfirmasi kata sandi",
        "already_have_account" to "Sudah punya akun? Masuk",
        "dont_have_account" to "Belum punya akun? Daftar",
        "welcome" to "Selamat Datang",
        "create_account" to "Buat Akun Baru",
        "login_to_access" to "Masuk untuk mengakses data karakter",
        "register_to_access" to "Daftar untuk mengakses data karakter",
        "username_not_found" to "Nama pengguna tidak ditemukan!",
        "wrong_password" to "Kata sandi salah!",
        "username_exists" to "Nama pengguna '%s' sudah digunakan!",
        "password_min_6" to "Kata sandi minimal 6 karakter!",
        "password_not_match" to "Kata sandi dan konfirmasi tidak sama!",
        "username_password_empty" to "Nama pengguna dan kata sandi tidak boleh kosong!",
        "online" to "Online",
        "offline" to "Offline",
        "guest" to "Tamu",

        // ===== MAIN / HEADER =====
        "characters" to "Karakter",
        "wishlist" to "Favorit",
        "found_characters" to "Ditemukan %d karakter",
        "filter" to "Filter",
        "logout" to "Keluar",
        "login" to "Masuk",
        "profile" to "Profil",
        "no_filter" to "Belum ada filter yang dipilih",
        "select_category" to "Pilih Kategori",
        "please_select_category" to "Silakan pilih Kategori dengan menekan tombol filter di atas",
        "language" to "Bahasa",
        "category" to "Kategori",
        "back_to_list" to "Kembali ke daftar",

        // ===== FILTER =====
        "filter_title" to "Filter",
        "active_filters" to "Filter Aktif",
        "clear" to "Hapus",
        "please_select_category_filter" to "Silakan pilih kategori",
        "retry" to "Coba Lagi",
        "error_occurred" to "Terjadi kesalahan",

        // ===== PROFILE =====
        "profile_title" to "Profil",
        "statistics" to "Statistik",
        "favorites" to "Favorit",
        "characters_count" to "Karakter",
        "language_settings" to "Pengaturan Bahasa",
        "language_change_info" to "Perubahan bahasa akan memuat ulang data karakter",
        "english" to "Inggris",
        "indonesian" to "Indonesia",
        "version" to "Versi",
        "edit_photo" to "Ubah Foto",
        "delete_photo" to "Hapus Foto",
        "change_profile_photo" to "Ubah Foto Profil",
        "choose_photo_source" to "Pilih sumber foto untuk profil Anda",
        "choose_from_gallery" to "Pilih dari Galeri",
        "take_photo" to "Ambil Foto",
        "cancel" to "Batal",
        "delete_photo_confirm" to "Hapus Foto Profil",

        // ===== WISHLIST =====
        "no_favorites" to "Belum ada karakter favorit",
        "no_favorites_hint" to "Sentuh ikon love pada karakter untuk menambahkannya",
        "loading_data" to "Memuat data...",
        "loading_characters" to "Memuat data karakter...",
        "add_to_wishlist" to "Tambahkan ke wishlist",
        "remove_from_wishlist" to "Hapus dari wishlist",

        // ===== CHARACTER DETAIL =====
        "no_images" to "Tidak ada gambar",
        "overview" to "Ringkasan",
        "abilities_powers" to "Kemampuan & Kekuatan",
        "watch_video" to "Tonton Video",
        "play_video" to "Putar Video",
        "loading_details" to "Memuat detail karakter...",
        "category_label" to "Kategori",
        "video" to "Video",

        // ===== GENERAL =====
        "error" to "Error",
        "unknown_error" to "Error tidak diketahui",
        "wifi_not_connected" to "Tidak ada koneksi internet! Aplikasi akan ditutup.",
        "press_again_to_exit" to "Tekan sekali lagi untuk keluar",

        // ===== LOGIN & LOGOUT =====
        "login_success" to "Login berhasil! Selamat datang, %s",
        "logout_success" to "Anda telah berhasil logout. Sampai jumpa!",
        "login_error_username" to "Username '%s' tidak ditemukan!",
        "login_error_password" to "Password salah! Silakan coba lagi.",
        "register_success" to "Pendaftaran berhasil! Selamat datang, %s",

        // ===== CROP / PROFILE PHOTO =====
        "change_profile_photo" to "Ubah Foto Profil",
        "choose_photo_source" to "Pilih sumber foto untuk profil Anda",
        "choose_from_gallery" to "Pilih dari Galeri",
        "take_photo" to "Ambil Foto",
        "delete_photo" to "Hapus Foto",
        "delete_photo_confirm" to "Hapus Foto Profil",
        "edit_photo" to "Ubah Foto",
        "photo_updated" to "Foto profil berhasil diperbarui!",
        "photo_deleted" to "Foto profil berhasil dihapus!",
        "crop_photo" to "Crop Foto Profil",
        "crop_cancelled" to "Crop dibatalkan",
        "crop_failed" to "Gagal memproses gambar",
        "crop_success" to "Foto berhasil di-crop!",
        "photo_updated_success" to "Foto profil berhasil diperbarui!",
    )

    // ============ BAHASA INGGRIS ============
    private val en = mapOf(
        // ===== LOGIN =====
        "login_title" to "Login",
        "register_title" to "Register",
        "username" to "Username",
        "password" to "Password",
        "confirm_password" to "Confirm Password",
        "login_button" to "Login",
        "register_button" to "Register",
        "login_username_placeholder" to "Enter username",
        "login_password_placeholder" to "Enter password",
        "confirm_password_placeholder" to "Confirm password",
        "already_have_account" to "Already have an account? Login",
        "dont_have_account" to "Don't have an account? Register",
        "welcome" to "Welcome",
        "create_account" to "Create New Account",
        "login_to_access" to "Login to access character data",
        "register_to_access" to "Register to access character data",
        "username_not_found" to "Username not found!",
        "wrong_password" to "Wrong password!",
        "username_exists" to "Username '%s' already exists!",
        "password_min_6" to "Password must be at least 6 characters!",
        "password_not_match" to "Password and confirmation do not match!",
        "username_password_empty" to "Username and password cannot be empty!",
        "online" to "Online",
        "offline" to "Offline",
        "guest" to "Guest",

        // ===== MAIN / HEADER =====
        "characters" to "Characters",
        "wishlist" to "Wishlist",
        "found_characters" to "Found %d characters",
        "filter" to "Filter",
        "logout" to "Logout",
        "login" to "Login",
        "profile" to "Profile",
        "no_filter" to "No filter selected",
        "select_category" to "Select Category",
        "please_select_category" to "Please select Category by pressing the filter button above",
        "language" to "Language",
        "category" to "Category",
        "back_to_list" to "Back to list",

        // ===== FILTER =====
        "filter_title" to "Filter",
        "active_filters" to "Active Filters",
        "clear" to "Clear",
        "please_select_category_filter" to "Please select a category",
        "retry" to "Retry",
        "error_occurred" to "An error occurred",

        // ===== PROFILE =====
        "profile_title" to "Profile",
        "statistics" to "Statistics",
        "favorites" to "Favorites",
        "characters_count" to "Characters",
        "language_settings" to "Language Settings",
        "language_change_info" to "Language change will reload character data",
        "english" to "English",
        "indonesian" to "Indonesian",
        "version" to "Version",
        "edit_photo" to "Edit Photo",
        "delete_photo" to "Delete Photo",
        "change_profile_photo" to "Change Profile Photo",
        "choose_photo_source" to "Choose photo source for your profile",
        "choose_from_gallery" to "Choose from Gallery",
        "take_photo" to "Take Photo",
        "cancel" to "Cancel",
        "delete_photo_confirm" to "Delete Profile Photo",

        // ===== WISHLIST =====
        "no_favorites" to "No favorite characters yet",
        "no_favorites_hint" to "Tap the love icon on a character to add it",
        "loading_data" to "Loading data...",
        "loading_characters" to "Loading character data...",
        "add_to_wishlist" to "Add to wishlist",
        "remove_from_wishlist" to "Remove from wishlist",

        // ===== CHARACTER DETAIL =====
        "no_images" to "No images available",
        "overview" to "Overview",
        "abilities_powers" to "Abilities & Powers",
        "watch_video" to "Watch Video",
        "play_video" to "Play Video",
        "loading_details" to "Loading character details...",
        "category_label" to "Category",
        "video" to "Video",

        // ===== GENERAL =====
        "error" to "Error",
        "unknown_error" to "Unknown error",
        "wifi_not_connected" to "No internet connection! App will close.",
        "press_again_to_exit" to "Press again to exit",

        // ===== LOGIN & LOGOUT =====
        "login_success" to "Login successful! Welcome, %s",
        "logout_success" to "You have successfully logged out. See you!",
        "login_error_username" to "Username '%s' not found!",
        "login_error_password" to "Wrong password! Please try again.",
        "register_success" to "Registration successful! Welcome, %s",

        "change_profile_photo" to "Change Profile Photo",
        "choose_photo_source" to "Choose photo source for your profile",
        "choose_from_gallery" to "Choose from Gallery",
        "take_photo" to "Take Photo",
        "delete_photo" to "Delete Photo",
        "delete_photo_confirm" to "Delete Profile Photo",
        "edit_photo" to "Edit Photo",
        "photo_updated" to "Profile photo updated successfully!",
        "photo_deleted" to "🗑Profile photo deleted successfully!",
        "crop_photo" to "Crop Profile Photo",
        "crop_cancelled" to "Crop cancelled",
        "crop_failed" to "Failed to process image",
        "crop_success" to "Image cropped successfully!",
        "photo_updated_success" to "Profile photo updated successfully!",
    )

    // ============ FUNGSI GET TEXT ============
    fun getText(key: String, language: String = "en", vararg args: Any): String {
        val texts = if (language == "id") id else en
        val text = texts[key] ?: key
        return if (args.isNotEmpty()) String.format(text, *args) else text
    }
}
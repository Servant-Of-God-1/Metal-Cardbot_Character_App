package com.example.testingmyapi.ui.screen

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.testingmyapi.R
import com.example.testingmyapi.viewmodel.CharacterViewModel
import com.example.testingmyapi.viewmodel.UiState
import com.yalantis.ucrop.UCrop
import java.io.File

@Composable
fun ProfileScreen(
    uiState: UiState,
    viewModel: CharacterViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    var showImagePickerDialog by remember { mutableStateOf(false) }

    // ✅ Ambil teks berdasarkan bahasa
    val language = uiState.appLanguage

    val availableLanguages = listOf(
        "en" to viewModel.getText("english"),
        "id" to viewModel.getText("indonesian")
    )

    var selectedLanguage by remember { mutableStateOf(uiState.appLanguage) }

    LaunchedEffect(uiState.appLanguage) {
        selectedLanguage = uiState.appLanguage
    }

    val profileImageBase64 by viewModel.profileImage.collectAsState()
    val profileBitmap = remember(profileImageBase64) {
        if (profileImageBase64.isNotEmpty()) {
            val bytes = android.util.Base64.decode(profileImageBase64, android.util.Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } else {
            null
        }
    }

    // ============================================================
    // CROP LAUNCHER
    // ============================================================
    val cropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val resultUri = UCrop.getOutput(result.data!!)
                resultUri?.let {
                    val bitmap = context.contentResolver.openInputStream(it)?.use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)
                    }
                    bitmap?.let {
                        viewModel.updateProfileImage(it)
                        showImagePickerDialog = false
                        Toast.makeText(context, viewModel.getText("crop_success"), Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, viewModel.getText("crop_failed"), Toast.LENGTH_SHORT).show()
            }
        } else if (result.resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(context, viewModel.getText("crop_cancelled"), Toast.LENGTH_SHORT).show()
        }
    }

    // ============================================================
    // GALLERY LAUNCHER
    // ============================================================
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val destinationUri = Uri.fromFile(
                    File(context.cacheDir, "cropped_" + System.currentTimeMillis() + ".jpg")
                )

                val options = UCrop.Options().apply {
                }

                val intent = UCrop.of(it, destinationUri)
                    .withAspectRatio(1f, 1f)
                    .withMaxResultSize(512, 512)
                    .withOptions(options)
                    .getIntent(context)

                cropLauncher.launch(intent)

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, viewModel.getText("crop_failed"), Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ============================================================
    // CAMERA LAUNCHER
    // ============================================================
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            viewModel.updateProfileImage(it)
            showImagePickerDialog = false
            Toast.makeText(context, viewModel.getText("photo_updated_success"), Toast.LENGTH_SHORT).show()
        }
    }

    // ============================================================
    // LAYOUT UTAMA
    // ============================================================
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // ========== HEADER ==========
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.clickable(
                    onClick = onBack,
                    indication = null,
                    interactionSource = interactionSource
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = viewModel.getText("back_to_list"),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = viewModel.getText("back_to_list"),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (uiState.isLoggedIn) {
                IconButton(
                    onClick = {
                        viewModel.logout()
                        onBack()
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = viewModel.getText("logout"),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ========== CONTENT ==========
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ========== 1. PROFIL PHOTO CARD ==========
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable(
                                    onClick = { showImagePickerDialog = true },
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                )
                        ) {
                            if (profileBitmap != null) {
                                AsyncImage(
                                    model = profileBitmap,
                                    contentDescription = viewModel.getText("profile"),
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                            shape = CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = viewModel.getText("profile"),
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(32.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = uiState.username.ifEmpty { viewModel.getText("guest") },
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        // Tombol Ubah Foto
                        OutlinedButton(
                            onClick = { showImagePickerDialog = true },
                            modifier = Modifier.fillMaxWidth(0.7f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = viewModel.getText("edit_photo"),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = viewModel.getText("change_profile_photo"),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // ========== 2. STATISTIK CARD ==========
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = uiState.favoriteCount.toString(),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = viewModel.getText("favorites"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxHeight(0.6f)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = uiState.characters.size.toString(),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = viewModel.getText("characters_count"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ========== 3. LANGUAGE SETTINGS CARD ==========
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = viewModel.getText("language"),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = viewModel.getText("language_settings"),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        availableLanguages.forEachIndexed { index, (code, name) ->
                            if (index > 0) {
                                Divider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        onClick = {
                                            selectedLanguage = code
                                            viewModel.changeLanguage(code)
                                        },
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    )
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (selectedLanguage == code)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.surfaceVariant
                                            )
                                    ) {
                                        if (selectedLanguage == code) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White)
                                                    .align(Alignment.Center)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (selectedLanguage == code)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (selectedLanguage == code)
                                            FontWeight.Bold
                                        else
                                            FontWeight.Normal
                                    )
                                }

                                if (selectedLanguage == code) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = viewModel.getText("clear"),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = viewModel.getText("info"),
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = viewModel.getText("language_change_info"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // ========== 4. FOOTER ==========
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Text(
                        text = "${viewModel.getText("version")} 1.0.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }

    // ============================================================
    // DIALOG PILIH GAMBAR
    // ============================================================
    if (showImagePickerDialog) {
        Dialog(
            onDismissRequest = { showImagePickerDialog = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = viewModel.getText("change_profile_photo"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = viewModel.getText("choose_photo_source"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Tombol Galeri
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    onClick = {
                                        galleryLauncher.launch("image/*")
                                    },
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                )
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = viewModel.getText("choose_from_gallery"),
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = viewModel.getText("choose_from_gallery"),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Divider()

                        // Tombol Kamera
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    onClick = {
                                        cameraLauncher.launch(null)
                                        showImagePickerDialog = false
                                    },
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                )
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = viewModel.getText("take_photo"),
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = viewModel.getText("take_photo"),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Tombol Hapus (jika ada foto)
                        if (profileBitmap != null) {
                            Divider()

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        onClick = {
                                            viewModel.deleteProfileImage()
                                            showImagePickerDialog = false
                                            Toast.makeText(context, viewModel.getText("photo_deleted"), Toast.LENGTH_SHORT).show()
                                        },
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    )
                                    .padding(vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = viewModel.getText("delete_photo"),
                                    modifier = Modifier.size(28.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = viewModel.getText("delete_photo_confirm"),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = { showImagePickerDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = viewModel.getText("cancel"),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
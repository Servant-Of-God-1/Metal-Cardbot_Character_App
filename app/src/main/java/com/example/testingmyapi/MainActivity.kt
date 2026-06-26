package com.example.testingmyapi

import android.content.Context
import android.content.Intent
import android.widget.Toast
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.testingmyapi.model.Character
import com.example.testingmyapi.model.CharacterDetail
import com.example.testingmyapi.ui.theme.CharacterAppTheme
import com.example.testingmyapi.viewmodel.CharacterViewModel
import com.example.testingmyapi.viewmodel.UiState
import androidx.activity.viewModels
import com.example.testingmyapi.viewmodel.CharacterViewModelFactory
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.background
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Badge
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.foundation.shape.CircleShape
import android.graphics.BitmapFactory
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import com.example.testingmyapi.ui.screen.ProfileScreen
import com.example.testingmyapi.ui.screen.SplashVideoView

enum class BottomNavTab {
    CHARACTERS,
    WISHLIST
}

fun openYouTube(context: Context, videoUrl: String) {
    try {
        val videoId = extractVideoId(videoUrl)
        if (videoId != null) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$videoId"))
            context.startActivity(intent)
        } else {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
            context.startActivity(intent)
        }
    } catch (e: Exception) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
        context.startActivity(intent)
    }
}

fun extractVideoId(url: String): String? {
    val patterns = listOf(
        "v=([a-zA-Z0-9_-]{11})",
        "youtu.be/([a-zA-Z0-9_-]{11})",
        "embed/([a-zA-Z0-9_-]{11})",
        "shorts/([a-zA-Z0-9_-]{11})"
    )
    for (pattern in patterns) {
        val regex = pattern.toRegex()
        val match = regex.find(url)
        match?.let {
            return it.groupValues[1]
        }
    }
    return null
}

fun getYouTubeThumbnail(videoId: String): String {
    return "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
}

fun checkIfWifiConnected(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
    } else {
        @Suppress("DEPRECATION")
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo?.type == ConnectivityManager.TYPE_WIFI
    }
}

class MainActivity : ComponentActivity() {
    private var backPressedTime: Long = 0
    private val backPressInterval: Long = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!checkIfWifiConnected(this)) {
            finish()
            return
        }

        setContent {
            CharacterAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppWithSplash()
                }
            }
        }
    }

    override fun onBackPressed() {
        val vm: CharacterViewModel by viewModels {
            CharacterViewModelFactory(this)
        }

        if (vm.uiState.value.selectedCharacter != null) {
            vm.clearSelectedCharacter()
            return
        }

        val currentTime = System.currentTimeMillis()
        if (currentTime - backPressedTime < backPressInterval) {
            finishAffinity()
        } else {
            backPressedTime = currentTime
            Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun AppWithSplash() {
    var showSplash by rememberSaveable { mutableStateOf(true) }
    var isLoggedIn by remember { mutableStateOf(false) }
    val viewModel: CharacterViewModel = viewModel(
        factory = CharacterViewModelFactory(LocalContext.current.applicationContext)
    )
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState.isLoggedIn) {
        isLoggedIn = uiState.isLoggedIn
    }

    if (showSplash) {
        SplashVideoView(
            onVideoComplete = {
                showSplash = false
            }
        )
    } else {
        CharacterAppScreen(viewModel = viewModel)
    }
}

@Composable
fun CharacterAppScreen(
    viewModel: CharacterViewModel = viewModel(
        factory = CharacterViewModelFactory(LocalContext.current.applicationContext)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val showDetail = uiState.selectedCharacter != null

    if (!uiState.isLoggedIn) {
        LoginPage(
            uiState = uiState,
            viewModel = viewModel
        )
        return
    }

    if (showDetail) {
        uiState.selectedCharacter?.let { character ->
            CharacterDetailScreen(
                character = character,
                onBack = { viewModel.clearSelectedCharacter() },
                isLoading = uiState.isDetailLoading,
                viewModel = viewModel
            )
        } ?: run {
            MainScreen(
                uiState = uiState,
                viewModel = viewModel
            )
        }
    } else {
        MainScreen(
            uiState = uiState,
            viewModel = viewModel
        )
    }
}

@Composable
fun MainScreen(
    uiState: UiState,
    viewModel: CharacterViewModel
) {
    var selectedTab by remember { mutableStateOf(BottomNavTab.CHARACTERS) }

    // ✅ CEK APAKAH SHOW PROFILE = TRUE
    if (uiState.showProfile) {
        ProfileScreen(
            uiState = uiState,
            viewModel = viewModel,
            onBack = { viewModel.hideProfile() }
        )
        return
    }

    // ✅ Ambil gambar profil
    val profileImageBase64 by viewModel.profileImage.collectAsState()
    val profileBitmap = remember(profileImageBase64) {
        if (profileImageBase64.isNotEmpty()) {
            val bytes = android.util.Base64.decode(profileImageBase64, android.util.Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } else {
            null
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.height(64.dp)
            ) {
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == BottomNavTab.CHARACTERS)
                                Icons.Filled.People
                            else
                                Icons.Outlined.People,
                            contentDescription = viewModel.getText("characters"),
                            modifier = Modifier.size(26.dp)
                        )
                    },
                    label = {
                        Text(
                            text = viewModel.getText("characters"),
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == BottomNavTab.CHARACTERS)
                                FontWeight.Bold
                            else
                                FontWeight.Normal
                        )
                    },
                    selected = selectedTab == BottomNavTab.CHARACTERS,
                    onClick = { selectedTab = BottomNavTab.CHARACTERS },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                NavigationBarItem(
                    icon = {
                        BadgedBox(
                            badge = {
                                if (uiState.favoriteCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ) {
                                        Text(
                                            text = uiState.favoriteCount.toString(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (selectedTab == BottomNavTab.WISHLIST)
                                    Icons.Filled.Favorite
                                else
                                    Icons.Outlined.Favorite,
                                contentDescription = viewModel.getText("wishlist"),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    },
                    label = {
                        Text(
                            text = viewModel.getText("wishlist"),
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == BottomNavTab.WISHLIST)
                                FontWeight.Bold
                            else
                                FontWeight.Normal
                        )
                    },
                    selected = selectedTab == BottomNavTab.WISHLIST,
                    onClick = { selectedTab = BottomNavTab.WISHLIST },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // ========== HEADER CARD ==========
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.isLoggedIn)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Kiri: Filter
                    if (uiState.isLoggedIn) {
                        IconButton(
                            onClick = { viewModel.toggleFilterSheet() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = viewModel.getText("filter"),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(40.dp))
                    }

                    // Kanan: Profile dengan Foto
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (uiState.isLoggedIn) {
                            Text(
                                text = uiState.username,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        // ✅ Tombol Profile dengan Foto
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable(
                                    onClick = { viewModel.toggleProfile() },
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                )
                                .background(
                                    if (uiState.isLoggedIn)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.secondaryContainer
                                )
                        ) {
                            if (profileBitmap != null && uiState.isLoggedIn) {
                                // ✅ Tampilkan foto profil
                                AsyncImage(
                                    model = profileBitmap,
                                    contentDescription = viewModel.getText("profile"),
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                // ✅ Default avatar
                                Icon(
                                    imageVector = if (uiState.isLoggedIn)
                                        Icons.Default.Person
                                    else
                                        Icons.Default.Login,
                                    contentDescription = if (uiState.isLoggedIn) viewModel.getText("profile") else viewModel.getText("login"),
                                    tint = if (uiState.isLoggedIn)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else
                                        MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ========== KONTEN ==========
            when (selectedTab) {
                BottomNavTab.CHARACTERS -> CharacterListContent(uiState, viewModel)
                BottomNavTab.WISHLIST -> WishlistScreen(uiState, viewModel)
            }
        }
    }

    // BottomSheet untuk Filter
    if (uiState.showFilterSheet) {
        FilterBottomSheet(
            uiState = uiState,
            viewModel = viewModel,
            onDismiss = { viewModel.hideFilterSheet() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    uiState: UiState,
    viewModel: CharacterViewModel,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = viewModel.getText("filter_title"),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // ✅ HANYA CATEGORY FILTER (LANGUAGE DIHAPUS)
            if (uiState.categories.isNotEmpty()) {
                Text(
                    text = viewModel.getText("category") + ":",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.categories) { category ->
                        FilterChip(
                            onClick = {
                                viewModel.selectCategory(
                                    if (uiState.selectedCategory == category.category) null else category.category
                                )
                            },
                            label = {
                                Text(
                                    text = category.category ?: "",
                                    fontSize = 12.sp
                                )
                            },
                            selected = uiState.selectedCategory == category.category,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Status Filter Saat Ini
            if (uiState.selectedCategory != null) {
                Divider(
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    text = viewModel.getText("active_filters") + ":",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.selectedCategory?.let {
                        AssistChip(
                            onClick = { viewModel.selectCategory(null) },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${viewModel.getText("category")}: $it")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = viewModel.getText("clear"),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CharacterListContent(
    uiState: UiState,
    viewModel: CharacterViewModel
) {
    when {
        // ========== 1. LOADING ==========
        uiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = viewModel.getText("loading_characters"),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        !uiState.isFilterReady -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = viewModel.getText("please_select_category"),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    if (uiState.selectedCategory != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${viewModel.getText("category")}: ${uiState.selectedCategory}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // ========== 3. ERROR (tapi filter sudah siap) ==========
        uiState.error != null && uiState.isFilterReady -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "❌ ${viewModel.getText("error")}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.error ?: viewModel.getText("unknown_error"),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val category = uiState.selectedCategory
                            if (category != null) {
                                viewModel.selectCategory(category)
                            }
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(viewModel.getText("retry"))
                    }
                }
            }
        }

        // ========== 4. KARAKTER DITEMUKAN ==========
        uiState.characters.isNotEmpty() -> {
            Column {
                Text(
                    text = viewModel.getText("found_characters", uiState.characters.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.characters) { character ->
                        CharacterCard(
                            character = character,
                            onClick = { viewModel.selectCharacter(character.id) },
                            viewModel = viewModel
                        )
                    }
                }
            }
        }

        // ========== 5. KONDISI LAINNYA (fallback) ==========
        else -> {
            // ✅ Ini seharusnya tidak terjadi, tapi sebagai fallback
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = viewModel.getText("no_filter"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun WishlistScreen(
    uiState: UiState,
    viewModel: CharacterViewModel
) {
    // ✅ Gunakan allCharacters dari ViewModel (bukan dari uiState)
    val allCharacters by viewModel.allCharacters.collectAsState()
    val favoriteNames by viewModel.favoriteNames.collectAsState()

    // ✅ Filter karakter favorit
    val favoriteCharacters = allCharacters.filter { character ->
        val key = viewModel.getCharacterKey(character.name)
        favoriteNames.contains(key)
    }

    // ✅ Hapus duplikat berdasarkan nama
    val uniqueFavoriteCharacters = favoriteCharacters.distinctBy {
        viewModel.getCharacterKey(it.name)
    }

    if (uiState.isLoadingAllCharacters) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Loading character data...",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    } else if (uniqueFavoriteCharacters.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (allCharacters.isEmpty()) "Loading data..." else "There are no favorite characters yet",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (allCharacters.isEmpty())
                        "Wait a moment, the data is loading"
                    else
                        "Touch the love icon on a detail character to add it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        Column {
            Text(
                text = "Wishlist (${uniqueFavoriteCharacters.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uniqueFavoriteCharacters) { character ->
                    CharacterCard(
                        character = character,
                        onClick = { viewModel.selectCharacter(character.id) },
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun CharacterCard(
    character: Character,
    onClick: () -> Unit,
    viewModel: CharacterViewModel
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }

    // ✅ Gunakan favoriteNames
    val favoriteNames by viewModel.favoriteNames.collectAsState()
    val isFavorite = favoriteNames.contains(viewModel.getCharacterKey(character.name))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = interactionSource
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = character.image,
                contentDescription = character.name,
                modifier = Modifier
                    .size(80.dp)
                    .padding(end = 12.dp),
                contentScale = ContentScale.Fit,
                placeholder = rememberAsyncImagePainter(android.R.drawable.ic_menu_gallery)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = character.name ?: "Unknown Character",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                character.quote?.let {
                    Text(
                        text = "${it.take(50)}${if (it.length > 50) "..." else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }

                character.category?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    AssistChip(
                        onClick = { /* No action */ },
                        label = {
                            Text(it, fontSize = 10.sp)
                        },
                        modifier = Modifier.height(24.dp)
                    )
                }

                if (!character.urlVideo.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    AssistChip(
                        onClick = {
                            openYouTube(context, character.urlVideo!!)
                        },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Video", fontSize = 10.sp)
                            }
                        },
                        modifier = Modifier.height(20.dp),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    )
                }
            }

            // ✅ Ikon panah
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "View details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@Composable
fun CharacterDetailScreen(
    character: CharacterDetail,
    onBack: () -> Unit,
    isLoading: Boolean,
    viewModel: CharacterViewModel
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Loading character details...",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClick = onBack,
                        indication = null,
                        interactionSource = interactionSource
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Back to list",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Baris nama + ikon love
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = character.name ?: "Unknown Character",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )

                                val favoriteNames by viewModel.favoriteNames.collectAsState()
                                val isFavorite = favoriteNames.contains(viewModel.getCharacterKey(character.name))
                                IconButton(
                                    onClick = {
                                        viewModel.toggleFavorite(character.name)
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = if (isFavorite) "Hapus dari wishlist" else "Tambahkan ke wishlist",
                                        tint = if (isFavorite) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            val imageList = listOf(
                                Triple(character.image, "Image 1", character.caption ?: ""),
                                Triple(character.image1, "Image 2", character.caption1 ?: ""),
                                Triple(character.image2, "Image 3", character.caption2 ?: "")
                            ).filter { it.first != null }

                            if (imageList.isNotEmpty()) {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(240.dp)
                                ) {
                                    items(imageList.size) { index ->
                                        val (imageUrl, title, caption) = imageList[index]
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.width(280.dp)
                                        ) {
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(220.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                elevation = CardDefaults.cardElevation(4.dp)
                                            ) {
                                                AsyncImage(
                                                    model = imageUrl,
                                                    contentDescription = title,
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(4.dp),
                                                    contentScale = ContentScale.Fit
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(4.dp))

                                            if (caption.isNotBlank()) {
                                                Text(
                                                    text = caption,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }

                                            Text(
                                                text = "${index + 1}/${imageList.size}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No images available",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            character.category?.let {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Category: $it",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            character.quote?.let { quote ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                ) {
                                    Text(
                                        text = "$quote ~${character.name ?: ""}",
                                        modifier = Modifier.padding(16.dp),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            val descriptionItems = mutableListOf<Pair<String, String>>()

                            character.description?.let {
                                descriptionItems.add(it to "Overview")
                            }

                            val abilities = buildString {
                                character.description1?.let {
                                    append(it.replace("\n", "\n• "))
                                    append("\n\n")
                                }
                                character.description2?.let {
                                    append(it.replace("\n", "\n• "))
                                    append("\n\n")
                                }
                                character.description3?.let {
                                    append(it.replace("\n", "\n• "))
                                }
                            }.trim()

                            if (abilities.isNotEmpty()) {
                                descriptionItems.add(abilities to "Abilities & Powers")
                            }

                            if (descriptionItems.isNotEmpty()) {
                                descriptionItems.forEach { (desc, subTitle) ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp)
                                        ) {
                                            Text(
                                                text = "$subTitle",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = desc,
                                                style = MaterialTheme.typography.bodyMedium,
                                                textAlign = TextAlign.Justify
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }

                            character.urlVideo?.let { videoUrl ->
                                val videoId = extractVideoId(videoUrl)

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Text(
                                            text = "Watch Video",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))

                                        if (videoId != null) {
                                            AsyncImage(
                                                model = getYouTubeThumbnail(videoId),
                                                contentDescription = "YouTube Thumbnail",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(180.dp)
                                                    .clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Crop,
                                                error = rememberAsyncImagePainter(android.R.drawable.ic_menu_gallery)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Button(
                                            onClick = {
                                                try {
                                                    openYouTube(context, videoUrl)
                                                } catch (e: Exception) {
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.tertiary
                                            )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = null
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Play Video")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoginDialog(
    uiState: UiState,
    viewModel: CharacterViewModel
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isRegisterMode by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { },
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (isRegisterMode) Icons.Default.PersonAdd else Icons.Default.Lock,
                    contentDescription = if (isRegisterMode) "Register" else "Login",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isRegisterMode) "Register" else "Login",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Error message
                uiState.error?.let {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "❌ ${it}",
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 14.sp
                        )
                    }
                }

                // Username Field
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    placeholder = { Text("Masukkan username") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Username"
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = uiState.error != null
                )

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    placeholder = { Text("Masukkan password") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Password"
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { showPassword = !showPassword }
                        ) {
                            Icon(
                                imageVector = if (showPassword)
                                    Icons.Default.Visibility
                                else
                                    Icons.Default.VisibilityOff,
                                contentDescription = if (showPassword) "Hide password" else "Show password"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (showPassword)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    isError = uiState.error != null
                )

                // Confirm Password (hanya saat register)
                if (isRegisterMode) {
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Konfirmasi Password") },
                        placeholder = { Text("Konfirmasi password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Konfirmasi Password"
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (showPassword)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        isError = uiState.error != null
                    )
                }

                // Switch Login/Register
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = {
                            isRegisterMode = !isRegisterMode
                            // Reset error saat switch
                            if (uiState.error != null) {
                                viewModel.hideLoginDialog()
                                viewModel.showLoginDialog()
                            }
                        }
                    ) {
                        Text(
                            text = if (isRegisterMode)
                                "Already have an account? Log in"
                            else
                                "Don't have an account? Register",
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isRegisterMode) {
                        viewModel.register(username, password, confirmPassword)
                    } else {
                        viewModel.login(username, password)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRegisterMode)
                        MaterialTheme.colorScheme.secondary
                    else
                        MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = if (isRegisterMode) Icons.Default.PersonAdd else Icons.Default.Login,
                    contentDescription = if (isRegisterMode) "Register" else "Login"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isRegisterMode) "Register" else "Login")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun LoginPage(
    uiState: UiState,
    viewModel: CharacterViewModel
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isRegisterMode by remember { mutableStateOf(false) }

    LaunchedEffect(isRegisterMode) {
        if (uiState.error != null) {
            viewModel.hideLoginDialog()
            viewModel.showLoginDialog()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ========== LOGO ==========
            Icon(
                imageVector = if (isRegisterMode) Icons.Default.PersonAdd else Icons.Default.Lock,
                contentDescription = if (isRegisterMode) "Register" else "Login",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ========== JUDUL ==========
            Text(
                text = if (isRegisterMode) viewModel.getText("create_account") else viewModel.getText("welcome"),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = if (isRegisterMode) viewModel.getText("register_to_access") else viewModel.getText("login_to_access"),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ========== PESAN LOGOUT (Jika baru logout) ==========
            if (uiState.isLogoutSuccess) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.loginMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ========== PESAN ERROR ==========
            uiState.error?.let { errorKey ->
                val errorMessage = when (errorKey) {
                    "username_not_found" -> viewModel.getText("username_not_found")
                    "wrong_password" -> viewModel.getText("wrong_password")
                    "username_exists" -> viewModel.getText("username_exists", username)
                    "password_min_6" -> viewModel.getText("password_min_6")
                    "password_not_match" -> viewModel.getText("password_not_match")
                    "username_password_empty" -> viewModel.getText("username_password_empty")
                    else -> errorKey
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // ========== USERNAME FIELD ==========
            OutlinedTextField(
                value = username,
                onValueChange = { username = it.trim() },
                label = { Text(viewModel.getText("username")) },
                placeholder = { Text(viewModel.getText("login_username_placeholder")) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = viewModel.getText("username")
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = uiState.error != null &&
                        (uiState.error == "username_not_found" || uiState.error == "username_exists"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ========== PASSWORD FIELD ==========
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(viewModel.getText("password")) },
                placeholder = { Text(viewModel.getText("login_password_placeholder")) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = viewModel.getText("password")
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = { showPassword = !showPassword }
                    ) {
                        Icon(
                            imageVector = if (showPassword)
                                Icons.Default.Visibility
                            else
                                Icons.Default.VisibilityOff,
                            contentDescription = if (showPassword) "Hide password" else "Show password"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (showPassword)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                isError = uiState.error != null && uiState.error == "wrong_password",
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            // ========== CONFIRM PASSWORD (Register) ==========
            if (isRegisterMode) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text(viewModel.getText("confirm_password")) },
                    placeholder = { Text(viewModel.getText("confirm_password_placeholder")) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = viewModel.getText("confirm_password")
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (showPassword)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    isError = uiState.error != null && uiState.error == "password_not_match",
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ========== TOMBOL LOGIN/REGISTER ==========
            Button(
                onClick = {
                    if (isRegisterMode) {
                        viewModel.register(username, password, confirmPassword)
                    } else {
                        viewModel.login(username, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRegisterMode)
                        MaterialTheme.colorScheme.secondary
                    else
                        MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (isRegisterMode) Icons.Default.PersonAdd else Icons.Default.Login,
                    contentDescription = if (isRegisterMode) "Register" else "Login"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isRegisterMode) viewModel.getText("register_button") else viewModel.getText("login_button"),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ========== SWITCH LOGIN/REGISTER ==========
            TextButton(
                onClick = {
                    isRegisterMode = !isRegisterMode
                    // Reset error saat switch
                    if (uiState.error != null) {
                        viewModel.hideLoginDialog()
                        viewModel.showLoginDialog()
                    }
                }
            ) {
                Text(
                    text = if (isRegisterMode)
                        viewModel.getText("already_have_account")
                    else
                        viewModel.getText("dont_have_account"),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
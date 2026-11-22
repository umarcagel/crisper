package compress.joshattic.us.ui.screens

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.LruCache
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** One video found on the phone. */
data class LibraryVideo(
    val id: Long,
    val uri: Uri,
    val sizeBytes: Long,
    val durationMs: Long,
    val dateAdded: Long,
    val isCompressed: Boolean
)

private enum class LibrarySort(val label: String) {
    NEWEST("Newest first"),
    LARGEST("Largest first"),
    LONGEST("Longest first")
}

private object ThumbCache {
    val cache = LruCache<Long, Bitmap>(40)
}

private fun canReadVideos(context: Context): Boolean {
    fun granted(permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    return if (Build.VERSION.SDK_INT >= 33) {
        granted(Manifest.permission.READ_MEDIA_VIDEO) ||
            (Build.VERSION.SDK_INT >= 34 && granted("android.permission.READ_MEDIA_VISUAL_USER_SELECTED"))
    } else {
        granted(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}

private fun permissionToAsk(): String =
    if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_VIDEO
    else Manifest.permission.READ_EXTERNAL_STORAGE

@Suppress("DEPRECATION")
private fun loadVideos(context: Context): List<LibraryVideo> {
    val result = mutableListOf<LibraryVideo>()
    val projection = arrayOf(
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.SIZE,
        MediaStore.Video.Media.DURATION,
        MediaStore.Video.Media.DATE_ADDED,
        MediaStore.Video.Media.DATA
    )
    try {
        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            MediaStore.Video.Media.DATE_ADDED + " DESC"
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val durCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val path = cursor.getString(dataCol) ?: ""
                result.add(
                    LibraryVideo(
                        id = id,
                        uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id),
                        sizeBytes = cursor.getLong(sizeCol),
                        durationMs = cursor.getLong(durCol),
                        dateAdded = cursor.getLong(dateCol),
                        isCompressed = path.contains("/Movies/Compressor/", ignoreCase = true)
                    )
                )
            }
        }
    } catch (_: Exception) {
        // Ignore: the screen simply shows an empty list.
    }
    return result
}

@Suppress("DEPRECATION")
private fun loadThumbnail(context: Context, video: LibraryVideo): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.contentResolver.loadThumbnail(video.uri, Size(256, 256), null)
        } else {
            MediaStore.Video.Thumbnails.getThumbnail(
                context.contentResolver,
                video.id,
                MediaStore.Video.Thumbnails.MINI_KIND,
                null
            )
        }
    } catch (_: Exception) {
        null
    }
}

private fun formatSize(bytes: Long): String {
    return if (bytes >= 1_000_000_000L) {
        String.format("%.1f GB", bytes / 1_000_000_000.0)
    } else {
        String.format("%d MB", maxOf(1L, Math.round(bytes / 1_000_000.0)))
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}

@Composable
private fun VideoThumb(video: LibraryVideo, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(initialValue = ThumbCache.cache.get(video.id), video.id) {
        if (value == null) {
            val loaded = withContext(Dispatchers.IO) { loadThumbnail(context, video) }
            if (loaded != null) {
                ThumbCache.cache.put(video.id, loaded)
                value = loaded
            }
        }
    }
    Box(modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
        val current = bitmap
        if (current != null) {
            Image(
                bitmap = current.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun LibraryScreen(
    totalSaved: String,
    showStorageSaved: Boolean,
    onVideoSelected: (Uri) -> Unit,
    onPickWithPicker: () -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(canReadVideos(context)) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        hasPermission = canReadVideos(context)
    }

    var videos by remember { mutableStateOf<List<LibraryVideo>?>(null) }
    LaunchedEffect(hasPermission) {
        videos = if (hasPermission) withContext(Dispatchers.IO) { loadVideos(context) } else emptyList()
    }

    var showCompressed by remember { mutableStateOf(false) }
    var sort by remember { mutableStateOf(LibrarySort.NEWEST) }
    var columns by remember { mutableIntStateOf(3) }
    var sortMenuOpen by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        if (showStorageSaved && totalSaved.isNotBlank()) {
            Text(
                text = "$totalSaved saved so far",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = !showCompressed,
                onClick = { showCompressed = false },
                label = { Text("Original") }
            )
            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
            FilterChip(
                selected = showCompressed,
                onClick = { showCompressed = true },
                label = { Text("Compressed") }
            )
            Spacer(modifier = Modifier.weight(1f))
            Box {
                IconButton(onClick = { sortMenuOpen = true }) {
                    Icon(Icons.Outlined.SwapVert, contentDescription = "Sort")
                }
                DropdownMenu(expanded = sortMenuOpen, onDismissRequest = { sortMenuOpen = false }) {
                    LibrarySort.values().forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = {
                                sort = option
                                sortMenuOpen = false
                            }
                        )
                    }
                }
            }
            IconButton(onClick = { columns = if (columns == 3) 2 else 3 }) {
                Icon(Icons.Outlined.GridView, contentDescription = "Change grid size")
            }
            IconButton(onClick = onPickWithPicker) {
                Icon(Icons.Outlined.FolderOpen, contentDescription = "Pick a video")
            }
        }

        val all = videos
        when {
            !hasPermission -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "See your videos here",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Allow access to list your videos with their size and length. Videos never leave your phone.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { permissionLauncher.launch(permissionToAsk()) }) {
                        Text("Allow access")
                    }
                    TextButton(onClick = onPickWithPicker) {
                        Text("Pick a video instead")
                    }
                }
            }
            all == null -> Box(modifier = Modifier.fillMaxSize())
            else -> {
                val shown = remember(all, showCompressed, sort) {
                    val filtered = all.filter { it.isCompressed == showCompressed }
                    when (sort) {
                        LibrarySort.NEWEST -> filtered.sortedByDescending { it.dateAdded }
                        LibrarySort.LARGEST -> filtered.sortedByDescending { it.sizeBytes }
                        LibrarySort.LONGEST -> filtered.sortedByDescending { it.durationMs }
                    }
                }
                if (shown.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (showCompressed) "No compressed videos yet" else "No videos found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        contentPadding = PaddingValues(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(shown, key = { it.id }) { video ->
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onVideoSelected(video.uri) }
                            ) {
                                VideoThumb(video, Modifier.fillMaxSize())
                                Text(
                                    text = formatSize(video.sizeBytes) + " · " + formatDuration(video.durationMs),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(6.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

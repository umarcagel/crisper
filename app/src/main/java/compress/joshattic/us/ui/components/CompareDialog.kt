package compress.joshattic.us.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import compress.joshattic.us.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Shows only the part of a picture to the right of [fraction] of its width. */
private class RightPartShape(private val fraction: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline =
        Outline.Rectangle(Rect(size.width * fraction, 0f, size.width, size.height))
}

private fun frameAt(context: Context, uri: Uri, positionMs: Long): Bitmap? {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, uri)
        retriever.getFrameAtTime(positionMs * 1000L, MediaMetadataRetriever.OPTION_CLOSEST)
    } catch (_: Exception) {
        null
    } finally {
        try {
            retriever.release()
        } catch (_: Exception) {
        }
    }
}

/**
 * Full-screen before/after viewer: drag the divider to compare the same moment of the
 * original (left) and the compressed video (right).
 */
@Composable
fun CompareDialog(
    originalUri: Uri,
    compressedUri: Uri,
    trimStartMs: Long,
    clipDurationMs: Long,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var committedMs by remember { mutableLongStateOf(clipDurationMs / 2) }
    var previewMs by remember { mutableFloatStateOf(clipDurationMs / 2f) }
    var split by remember { mutableFloatStateOf(0.5f) }

    val frames by produceState<Pair<Bitmap?, Bitmap?>?>(initialValue = null, committedMs) {
        value = null
        value = withContext(Dispatchers.IO) {
            Pair(
                frameAt(context, originalUri, trimStartMs + committedMs),
                frameAt(context, compressedUri, committedMs)
            )
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.compare_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.compare_close), fontWeight = FontWeight.Bold)
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    val pair = frames
                    val original = pair?.first
                    val compressed = pair?.second
                    val shown = compressed ?: original
                    when {
                        pair == null -> CircularProgressIndicator()
                        shown == null -> Text(
                            stringResource(R.string.compare_unavailable),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        else -> {
                            Box(
                                modifier = Modifier
                                    .aspectRatio(shown.width.toFloat() / shown.height.toFloat())
                                    .pointerInput(Unit) {
                                        detectHorizontalDragGestures { change, _ ->
                                            split = (change.position.x / size.width).coerceIn(0f, 1f)
                                        }
                                    }
                            ) {
                                if (original != null) {
                                    Image(
                                        bitmap = original.asImageBitmap(),
                                        contentDescription = null,
                                        contentScale = ContentScale.FillBounds,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                if (compressed != null) {
                                    Image(
                                        bitmap = compressed.asImageBitmap(),
                                        contentDescription = null,
                                        contentScale = ContentScale.FillBounds,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RightPartShape(split))
                                    )
                                }
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val x = size.width * split
                                    drawLine(
                                        color = Color.White,
                                        start = Offset(x, 0f),
                                        end = Offset(x, size.height),
                                        strokeWidth = 4f
                                    )
                                }
                                Text(
                                    stringResource(R.string.compare_original),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(8.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                                Text(
                                    stringResource(R.string.compare_compressed),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                Text(
                    stringResource(R.string.compare_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Slider(
                    value = previewMs,
                    onValueChange = { previewMs = it },
                    onValueChangeFinished = { committedMs = previewMs.toLong() },
                    valueRange = 0f..maxOf(clipDurationMs.toFloat(), 1f)
                )
            }
        }
    }
}

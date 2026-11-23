package compress.joshattic.us.ui.components

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import compress.joshattic.us.R

/** Full-screen video player using the phone's own player engine (adds nothing to the app size). */
@Composable
fun VideoPlayerDialog(uri: Uri, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val container = FrameLayout(ctx)
                    val video = VideoView(ctx)
                    val controller = MediaController(ctx)
                    controller.setAnchorView(container)
                    video.setMediaController(controller)
                    video.setVideoURI(uri)
                    video.setOnPreparedListener { video.start() }
                    container.addView(
                        video,
                        FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.Gravity.CENTER
                        )
                    )
                    container
                },
                onRelease = { container ->
                    (container.getChildAt(0) as? VideoView)?.stopPlayback()
                }
            )
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                Text(stringResource(R.string.player_close), color = Color.White)
            }
        }
    }
}

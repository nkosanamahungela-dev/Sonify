package dev.nkosanamahungela.sonify.ui.player

import androidx.compose.foundation.background
import dev.nkosanamahungela.sonify.ui.glass.pressable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.fill.Heart
import com.adamglin.phosphoricons.fill.Pause
import com.adamglin.phosphoricons.fill.Play
import com.adamglin.phosphoricons.fill.SkipBack
import com.adamglin.phosphoricons.fill.SkipForward
import com.adamglin.phosphoricons.regular.CaretDown
import com.adamglin.phosphoricons.regular.Devices
import com.adamglin.phosphoricons.regular.Heart
import com.adamglin.phosphoricons.regular.Plus
import com.adamglin.phosphoricons.regular.Repeat
import com.adamglin.phosphoricons.regular.Shuffle
import dev.nkosanamahungela.sonify.ui.components.Artwork
import dev.nkosanamahungela.sonify.ui.library.formatDuration

/**
 * Spotui's now-playing composition, connected to Sonify's player state.
 * Playback, queue edits, seek, liked songs and Connect remain Sonify-owned.
 */
@Composable
internal fun SpotuiPlayerScreen(
    state: PlaybackState,
    positionMs: State<Long>,
    onCollapse: () -> Unit,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    queue: List<QueueEntry>,
    queueActions: QueueActions,
    onOpenDevices: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onOpenAlbum: (() -> Unit)?,
    onToggleLike: (() -> Unit)?,
    inLikedSongs: Boolean,
    connectAvailable: Boolean,
    onAnotherDevice: Boolean,
) {
    var draggedProgress by remember { mutableStateOf<Float?>(null) }
    val duration = state.durationMs.coerceAtLeast(0L)
    val liveProgress = if (duration > 0L) {
        (positionMs.value.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val progress = draggedProgress ?: liveProgress

    Box(Modifier.fillMaxSize().background(Color(0xFF0B0B0F))) {
        if (!state.artworkUrl.isNullOrBlank()) {
            AsyncImage(
                model = state.artworkUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().blur(54.dp),
                alpha = 0.30f,
            )
        }
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF15151D).copy(alpha = 0.72f),
                        Color(0xFF09090C).copy(alpha = 0.96f),
                        Color(0xFF050507),
                    ),
                ),
            ),
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize().systemBarsPadding(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 20.dp, end = 20.dp, top = 8.dp, bottom = 28.dp,
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    androidx.compose.material3.Icon(
                        PhosphorIcons.Regular.CaretDown,
                        contentDescription = "Close player",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp).pressable(onClick = onCollapse),
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PLAYING FROM", color = Color(0xFFB3B3B3), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(
                            state.source.ifBlank { "Sonify" },
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    androidx.compose.material3.Icon(
                        PhosphorIcons.Regular.Plus,
                        contentDescription = "Add to playlist",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp).pressable(onClick = onAddToPlaylist),
                    )
                }
            }
            item {
                Box(
                    Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 28.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Artwork(
                        url = state.artworkUrl,
                        title = state.title,
                        modifier = Modifier.fillMaxWidth().widthIn(max = 360.dp)
                            .aspectRatio(1f).clip(RoundedCornerShape(10.dp)),
                        corner = 10.dp,
                        decodeSize = 360.dp,
                    )
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            state.title.ifBlank { "Nothing playing" },
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            state.artist,
                            color = Color(0xFFB3B3B3),
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp).then(
                                if (onOpenAlbum != null) Modifier.pressable(onClick = onOpenAlbum) else Modifier,
                            ),
                        )
                    }
                    androidx.compose.material3.Icon(
                        if (inLikedSongs) PhosphorIcons.Fill.Heart else PhosphorIcons.Regular.Heart,
                        contentDescription = if (inLikedSongs) "Liked" else "Like",
                        tint = if (inLikedSongs) Color(0xFF1ED760) else Color.White,
                        modifier = Modifier.size(26.dp).pressable(onClick = { onToggleLike?.invoke() }),
                    )
                }
            }
            item {
                Slider(
                    value = progress,
                    onValueChange = { draggedProgress = it },
                    onValueChangeFinished = {
                        draggedProgress?.let { onSeek((it * duration).toLong()) }
                        draggedProgress = null
                    },
                    valueRange = 0f..1f,
                    enabled = duration > 0L,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.White.copy(alpha = 0.28f),
                    ),
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        formatDuration(if (draggedProgress != null) (progress * duration).toLong() else positionMs.value),
                        color = Color(0xFFB3B3B3),
                        fontSize = 11.sp,
                    )
                    Text(formatDuration(duration), color = Color(0xFFB3B3B3), fontSize = 11.sp)
                }
            }
            item {
                Row(
                    Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 22.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    androidx.compose.material3.Icon(
                        PhosphorIcons.Regular.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (state.shuffleEnabled) Color(0xFF1ED760) else Color.White,
                        modifier = Modifier.size(23.dp).pressable(onClick = onToggleShuffle),
                    )
                    androidx.compose.material3.Icon(
                        PhosphorIcons.Fill.SkipBack,
                        contentDescription = "Previous",
                        tint = if (state.hasPrevious) Color.White else Color.Gray,
                        modifier = Modifier.size(30.dp).pressable(enabled = state.hasPrevious, onClick = onPrevious),
                    )
                    Box(
                        Modifier.size(64.dp).clip(CircleShape).background(Color.White)
                            .pressable(onClick = onTogglePlay),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (state.isBuffering) {
                            CircularProgressIndicator(Modifier.size(28.dp), color = Color.Black, strokeWidth = 2.dp)
                        } else {
                            androidx.compose.material3.Icon(
                                if (state.wantsPlay) PhosphorIcons.Fill.Pause else PhosphorIcons.Fill.Play,
                                contentDescription = if (state.wantsPlay) "Pause" else "Play",
                                tint = Color.Black,
                                modifier = Modifier.size(30.dp),
                            )
                        }
                    }
                    androidx.compose.material3.Icon(
                        PhosphorIcons.Fill.SkipForward,
                        contentDescription = "Next",
                        tint = if (state.hasNext) Color.White else Color.Gray,
                        modifier = Modifier.size(30.dp).pressable(enabled = state.hasNext, onClick = onNext),
                    )
                    androidx.compose.material3.Icon(
                        PhosphorIcons.Regular.Repeat,
                        contentDescription = "Repeat",
                        tint = if (state.repeatMode != Player.REPEAT_MODE_OFF) Color(0xFF1ED760) else Color.White,
                        modifier = Modifier.size(23.dp).pressable(onClick = onCycleRepeat),
                    )
                }
            }
            item {
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    if (connectAvailable) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.pressable(onClick = onOpenDevices),
                        ) {
                            androidx.compose.material3.Icon(
                                PhosphorIcons.Regular.Devices,
                                contentDescription = "Available devices",
                                tint = if (onAnotherDevice) Color(0xFF1ED760) else Color.White,
                                modifier = Modifier.size(20.dp),
                            )
                            Text(
                                if (onAnotherDevice) "Playing on another device" else "Available devices",
                                color = Color(0xFFB3B3B3),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    } else {
                        Spacer(Modifier)
                    }
                    Text(queue.size.toString() + " in queue", color = Color(0xFFB3B3B3), fontSize = 12.sp)
                }
            }
            if (queue.isNotEmpty()) {
                item {
                    Column(Modifier.fillMaxWidth().height(280.dp)) {
                        Text(
                            "Next in queue",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                        QueueList(queue, queueActions)
                    }
                }
            }
        }
    }
}

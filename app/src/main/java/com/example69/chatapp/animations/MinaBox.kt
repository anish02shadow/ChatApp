package com.example69.chatapp.animations

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example69.chatapp.R
import com.example69.chatapp.ui.theme.Screens.moodicons
import com.example69.chatapp.ui.theme.Screens.pickRandomColor
import eu.wewox.minabox.MinaBox
import eu.wewox.minabox.MinaBoxItem
import eu.wewox.minabox.rememberMinaBoxState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Advanced Mina Box layout example.
 */
@Composable
fun MinaBoxAdvancedScreen(
) {
    Scaffold(
    ) { padding ->
        val halfHeight = PolygonRadius * cos(PI / VerticesCount).toFloat()

        val itemSize = with(LocalDensity.current) {
            Size(
                width = PolygonRadius.toPx() * 2f,
                height = halfHeight.toPx() * 2f,
            )
        }

        val scope = rememberCoroutineScope()
        val state = rememberMinaBoxState()
        MinaBox(
            state = state,
            modifier = Modifier.padding(padding)
        ) {
            items(
                count = ColumnsCount * RowsCount,
                layoutInfo = {
                    val column = it % ColumnsCount
                    val row = it / ColumnsCount
                    val xOffset = itemSize.width * 0.75f
                    val yOffset = itemSize.height * 0.5f
                    MinaBoxItem(
                        x = 0f + column * xOffset,
                        y = (if (column % 2 == 1) yOffset else 0f) + row * itemSize.height,
                        width = itemSize.width,
                        height = itemSize.height,
                    )
                }
            ) { index ->
                Item(
                    onClick = {
                        scope.launch {
                            state.animateTo(index)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun Item(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotation = remember { Animatable(-15f) }
    val scale = remember { Animatable(0.5f) }
    LaunchedEffect(Unit) {
        delay(100)
        launch {
            scale.animateTo(1f)
        }
        launch {
            rotation.animateTo(0f)
        }
    }

    val color = MaterialTheme.colorScheme.primary
    var moodimage = remember { mutableStateOf(getRandomMoodIcon()) }
    Box(
        modifier = modifier
            .scale(scale.value)
            .rotate(rotation.value)
            .drawBehind {
                val path = size.createPolygonPath()
                drawPath(path, color, style = Stroke(10f))
            }
            .clip(
                GenericShape { size, _ ->
                    addPath(size.createPolygonPath())
                }
            )
            .clickable(onClick = onClick)
    ){
        Image(painter = painterResource(id = moodimage.value) , contentDescription = "Random Mood Image", contentScale = ContentScale.Crop)
    }
    
}

private fun Size.createPolygonPath(): Path =
    Path().apply {
        val radius = width / 2f

        fun lineTo(angle: Double) {
            lineTo(
                x = center.x + radius * cos(angle).toFloat(),
                y = center.y + radius * sin(angle).toFloat(),
            )
        }

        moveTo(0f, center.y)
        lineTo(-2f * PI / 3f)
        lineTo(-1f * PI / 3f)
        lineTo(width, center.y)
        lineTo(1f * PI / 3f)
        lineTo(2f * PI / 3f)
        close()
    }

private const val ColumnsCount = 8
private const val RowsCount = 8
private const val VerticesCount = 6
private val PolygonRadius = 50.dp

fun getRandomMoodIcon(): Int {
    val randomIndex = (0 until moodicons.size).random()
    return moodicons[randomIndex].first
}



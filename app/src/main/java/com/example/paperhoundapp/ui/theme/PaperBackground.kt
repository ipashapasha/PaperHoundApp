package com.example.paperhoundapp.ui.theme

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import kotlin.random.Random

private const val NOISE_TILE_SIZE = 256
private const val NOISE_ALPHA = 0.06f

/** A tileable grain mask: white pixels with random opacity, tinted at draw time. */
private val noiseBrush: ShaderBrush by lazy {
    val random = Random(seed = 7)
    val pixels = IntArray(NOISE_TILE_SIZE * NOISE_TILE_SIZE) { random.nextInt(256) shl 24 or 0xFFFFFF }
    val bitmap = Bitmap.createBitmap(pixels, NOISE_TILE_SIZE, NOISE_TILE_SIZE, Bitmap.Config.ARGB_8888)
    ShaderBrush(ImageShader(bitmap.asImageBitmap(), TileMode.Repeated, TileMode.Repeated))
}

/** Warm screen fill with a barely visible paper grain. */
@Composable
fun Modifier.paperBackground(): Modifier {
    val colors = MaterialTheme.colorScheme
    val grain = ColorFilter.tint(colors.onBackground, BlendMode.SrcIn)
    return this
        .background(colors.background)
        .drawBehind { drawRect(noiseBrush, alpha = NOISE_ALPHA, colorFilter = grain) }
}

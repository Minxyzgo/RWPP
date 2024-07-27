package io.github.rwpp.ui.v2

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.*
import io.github.rwpp.shared.generated.resources.Res
import io.github.rwpp.shared.generated.resources.metal
import io.github.rwpp.shared.generated.resources.title
import org.jetbrains.compose.resources.imageResource

@Composable
fun TitleBrush(): ShaderBrush {
    val image = imageResource(Res.drawable.metal)

    return ShaderBrush(ImageShader(image, TileMode.Repeated, TileMode.Repeated))
}
package io.github.rwpp.ui.v2

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import io.github.rwpp.shared.generated.resources.Res
import io.github.rwpp.shared.generated.resources.metal
import org.jetbrains.compose.resources.imageResource

@Composable
fun TitleBrush(): ShaderBrush {
    val image = imageResource(Res.drawable.metal)

    return ShaderBrush(ImageShader(image, TileMode.Repeated, TileMode.Repeated))
}
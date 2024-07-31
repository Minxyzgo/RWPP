/*
 * Copyright 2023-2024 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextInputSession
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.rwpp.shared.generated.resources.Res
import io.github.rwpp.shared.generated.resources.error_missingmap
import io.github.rwpp.ui.v2.bounceClick
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@Composable
fun BorderCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(53, 57, 53).copy(0.9f),
    shape: Shape = RoundedCornerShape(20.dp),
    content: @Composable ColumnScope.() -> Unit
) = Card(
    shape = shape,
    border = BorderStroke(2.dp, Color.DarkGray),
    elevation =  CardDefaults.cardElevation(defaultElevation = 10.dp),
    colors = CardDefaults.cardColors(containerColor = backgroundColor),
    modifier = modifier,
    content = content
)

@Composable
fun <T> BasicDropdownMenu(
    expanded: Boolean,
    items: List<T>,
    onItemSelected: (Int, T) -> Unit,
    onDismissRequest: () -> Unit
) = DropdownMenu(
    expanded = expanded,
    onDismissRequest = onDismissRequest
) {
    items.forEachIndexed { index, t ->
        DropdownMenuItem(
            text = { Text(t.toString()) },
            onClick = {
                onItemSelected(index, t)
                onDismissRequest()
            }
        )
    }
}


@Composable
fun <T> LargeDropdownMenu(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String,
    items: List<T>,
    hasValue: Boolean = true,
    selectedIndex: Int = -1,
    onItemSelected: (index: Int, item: T) -> Unit,
    selectedItemToString: (T) -> String = { it.toString() },
    selectedItemColor: (T?, Int) -> Color = { _, _ -> Color.White }
) {
    var expanded by remember { mutableStateOf(false) }
    val focusRequest = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .width(IntrinsicSize.Max)
    ) {

            OutlinedTextField(
                label = { Text(label, fontFamily = MaterialTheme.typography.headlineLarge.fontFamily) },
                textStyle = MaterialTheme.typography.headlineLarge.copy(color = selectedItemColor(items.getOrNull(selectedIndex), selectedIndex)),
                colors = RWOutlinedTextColors,
                value = if(hasValue) items.getOrNull(selectedIndex)?.let { selectedItemToString(it) } ?: "" else "",
                enabled = enabled,
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequest),
                trailingIcon = {
                    val icon = if(expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown
                    Icon(icon, "", tint = Color.White)
                },
                onValueChange = { },
                shape = RoundedCornerShape(10.dp),
                readOnly = true,
            )


        // Transparent clickable surface on top of OutlinedTextField
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .clickable(enabled = enabled && !expanded) { expanded = true; focusRequest.requestFocus() },
            color = Color.Transparent,
        ) {}

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                focusManager.clearFocus()
            }
        ) {
            items.forEachIndexed { index, t ->
                DropdownMenuItem(
                    text = { Text(selectedItemToString(t), style = MaterialTheme.typography.bodyLarge, color = selectedItemColor(t, index)) },
                    onClick = {
                        onItemSelected(index, t)
                        expanded = false
                        focusManager.clearFocus()
                    }
                )
            }
        }
    }
}

@Composable
fun LargeOutlinedButton(
    modifier: Modifier = Modifier,
    label: @Composable () -> Unit,
    value: String,
    enabled: Boolean = true,
    trailingIcon: @Composable () -> Unit = {},
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .then(modifier)
    ) {
        OutlinedTextField(
            label = label,
            textStyle = MaterialTheme.typography.headlineMedium,
            colors = RWOutlinedTextColors,
            value = value,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = trailingIcon,
            onValueChange = { },
            readOnly = true,
        )

        // Transparent clickable surface on top of OutlinedTextField
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .clickable(enabled = enabled) { onClick() },
            color = Color.Transparent,
        ) {}
    }
}

@Composable
fun LargeDividingLine(spacing: @Composable () -> Dp) {
    val color = Color.Black.copy(alpha = .2f)
    val _spacing = spacing()
    Box(
        Modifier
            .padding(top = _spacing, bottom = _spacing)
            .fillMaxWidth()
            .height(3.dp)
            .background(color)
    )
}

@Composable
fun RWSingleOutlinedTextField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    requestFocus: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    lengthLimitCount: Int = Int.MAX_VALUE,
    typeInOnlyInteger: Boolean = false,
    typeInNumberOnly: Boolean = false,
    enabled: Boolean = true,
    appendedContent: @Composable (() -> Unit)? = null,
    onValueChange: (String) -> Unit
) {
    Box(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .width(IntrinsicSize.Max)
    ) {

        val focusRequester = remember { if (requestFocus) FocusRequester() else null }
        var requested by remember { mutableStateOf(false) }

        OutlinedTextField(
            label = {
                Text(
                    label,
                    fontFamily = MaterialTheme.typography.headlineMedium.fontFamily
                )
            },
            textStyle = MaterialTheme.typography.headlineMedium,
            colors = RWOutlinedTextColors,
            value = value,
            enabled = enabled,
            singleLine = true,
            modifier = Modifier.fillMaxWidth().apply {
                if (requestFocus)
                    focusRequester(focusRequester!!)
                        .onGloballyPositioned {
                            if (!requested) {
                                requested = true
                                focusRequester.requestFocus()
                            }
                        }
            },
            trailingIcon = trailingIcon,
            leadingIcon = leadingIcon,
            onValueChange = { if(it.length <= lengthLimitCount && (!typeInNumberOnly || !typeInOnlyInteger || it.all { s -> s.isDigit() })) onValueChange(it) },
            keyboardOptions = KeyboardOptions(keyboardType = if(typeInNumberOnly) KeyboardType.Number else KeyboardType.Text)
        )

        appendedContent?.invoke()
    }
}

@Composable
fun RWTextButton(
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) = Card(
    border = BorderStroke(5.dp, Color.DarkGray),
    colors = CardDefaults.cardColors(containerColor = Color(27, 18, 18)),
    shape = RoundedCornerShape(10.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
    modifier = modifier.bounceClick(onClick),
) {
    Row(
        modifier = Modifier.padding(5.dp),
    ) {
        leadingIcon?.invoke()

        Text(
            label,
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(5.dp)
        )
    }
}

@Composable
fun RWCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) = Checkbox(
    checked, onCheckedChange, modifier, enabled, RWCheckBoxColors
)

@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float,
    color: Color = Color.White,
    drawStroke: Boolean = true,
    modifier: Modifier = Modifier,
    strokeColor: Color = Color(160, 191, 124)
) {
    val border = if(drawStroke) Modifier.border(1.dp, strokeColor) else Modifier
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .weight(weight)
            .then(border)
            .padding(0.dp, 2.dp, 0.dp, 2.dp)
            .then(modifier)
    ) {
        Text(
            text,
            maxLines = 1,
            color = color,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(0.dp, 2.dp, 0.dp, 2.dp)
        )
    }
}

const val DEFAULT_MINIMUM_TEXT_LINE = 5

@Composable
fun ExpandableText(
    modifier: Modifier = Modifier,
    textModifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    fontStyle: FontStyle? = null,
    text: String,
    collapsedMaxLine: Int = DEFAULT_MINIMUM_TEXT_LINE,
    showMoreText: String = "... Show More",
    showMoreStyle: SpanStyle = SpanStyle(fontWeight = FontWeight.W500),
    showLessText: String = " Show Less",
    showLessStyle: SpanStyle = showMoreStyle,
    textAlign: TextAlign? = null
) {
    var isExpanded by remember { mutableStateOf(false) }
    var clickable by remember { mutableStateOf(false) }
    var lastCharIndex by remember { mutableStateOf(0) }
    Box(modifier = Modifier
        .clickable(clickable) {
            isExpanded = !isExpanded
        }
        .then(modifier)
    ) {
        Text(
            modifier = textModifier
                .fillMaxWidth()
                .animateContentSize(),
            text = buildAnnotatedString {
                if (clickable) {
                    if (isExpanded) {
                        append(text)
                        withStyle(style = showLessStyle) { append(showLessText) }
                    } else {
                        val adjustText = text.substring(startIndex = 0, endIndex = lastCharIndex)
                            .dropLast(showMoreText.length)
                            .dropLastWhile { Character.isWhitespace(it) || it == '.' }
                        append(adjustText)
                        withStyle(style = showMoreStyle) { append(showMoreText) }
                    }
                } else {
                    append(text)
                }
            },
            maxLines = if (isExpanded) Int.MAX_VALUE else collapsedMaxLine,
            fontStyle = fontStyle,
            onTextLayout = { textLayoutResult ->
                if (!isExpanded && textLayoutResult.hasVisualOverflow) {
                    clickable = true
                    lastCharIndex = textLayoutResult.getLineEnd(collapsedMaxLine - 1)
                }
            },
            style = style,
            textAlign = textAlign
        )
    }
}

@OptIn(ExperimentalResourceApi::class, ExperimentalFoundationApi::class)
@Composable
fun LazyGridItemScope.MapItem(
    index: Int,
    state: LazyListState,
    name: String,
    image: Painter?,
    showImage: Boolean = true,
    onClick: () -> Unit,
) {
    val (_, easing) = state.calculateDelayAndEasing(index, 5)
    val animation = tween<Float>(durationMillis = 500, delayMillis = 0, easing = easing)
    val args = ScaleAndAlphaArgs(fromScale = 2f, toScale = 1f, fromAlpha = 0f, toAlpha = 1f)
    val (scale, alpha) = scaleAndAlpha(args = args, animation = animation)

    BorderCard(
        modifier = Modifier
            .graphicsLayer(alpha = alpha, scaleX = scale, scaleY = scale)
            .animateItemPlacement()
            .padding(10.dp)
            .sizeIn(maxHeight = 200.dp, maxWidth = 200.dp)
            .clickable { onClick() },
        backgroundColor = Color.DarkGray.copy(.7f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if(showImage) Image(
                modifier = Modifier.padding(5.dp).weight(1f),
                painter = image ?: painterResource(Res.drawable.error_missingmap),
                contentDescription = null
            )
            Text(
                name,
                modifier = Modifier.padding(5.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
//@Composable
//fun Modifier.simpleVerticalScrollbar(
//    state: LazyListState,
//    width: Dp = 8.dp
//): Modifier {
//    val targetAlpha = if (state.isScrollInProgress) 1f else 0f
//    val duration = if (state.isScrollInProgress) 150 else 500
//
//    val alpha by animateFloatAsState(
//        targetValue = targetAlpha,
//        animationSpec = tween(durationMillis = duration)
//    )
//
//    return drawWithContent {
//        drawContent()
//
//        val firstVisibleElementIndex = state.layoutInfo.visibleItemsInfo.firstOrNull()?.index
//        val needDrawScrollbar = state.isScrollInProgress || alpha > 0.0f
//
//        // Draw scrollbar if scrolling or if the animation is still running and lazy column has content
//        if (needDrawScrollbar && firstVisibleElementIndex != null) {
//            val elementHeight = this.size.height / state.layoutInfo.totalItemsCount
//            val scrollbarOffsetY = firstVisibleElementIndex * elementHeight + state.firstVisibleItemScrollOffset / 4
//            val scrollbarHeight = elementHeight * 4
//
//            drawRect(
//                color = Color(112, 130, 56),
//                topLeft = Offset(this.size.width - width.toPx(), scrollbarOffsetY),
//                size = Size(width.toPx(), scrollbarHeight),
//                alpha = alpha,
//            )
//        }
//    }
//}
//

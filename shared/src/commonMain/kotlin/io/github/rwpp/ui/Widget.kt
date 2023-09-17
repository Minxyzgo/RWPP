package io.github.rwpp.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun BorderCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Gray.copy(.6f),
    content: @Composable ColumnScope.() -> Unit
) = Card(
    shape = RoundedCornerShape(20.dp),
    border = BorderStroke(2.dp, Color.DarkGray),
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


@OptIn(ExperimentalMaterial3Api::class)
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
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .requiredWidth(200.dp)
    ) {
        OutlinedTextField(
            label = { Text(label, fontFamily = MaterialTheme.typography.headlineLarge.fontFamily) },
            textStyle = MaterialTheme.typography.headlineLarge,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                containerColor = Color.Transparent,
                focusedBorderColor = Color(44, 95, 45),
                unfocusedBorderColor = Color(151, 188, 98)
            ),
            value = if(hasValue) items.getOrNull(selectedIndex)?.let { selectedItemToString(it) } ?: "" else "",
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                val icon = if(expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown
                Icon(icon, "")
            },
            onValueChange = { },
            readOnly = true,
        )

        // Transparent clickable surface on top of OutlinedTextField
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .clickable(enabled = enabled && !expanded) { expanded = true },
            color = Color.Transparent,
        ) {}

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            items.forEachIndexed { index, t ->
                DropdownMenuItem(
                    text = { Text(t.toString()) },
                    onClick = {
                        onItemSelected(index, t)
                        expanded = false
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
            textStyle = MaterialTheme.typography.headlineLarge,
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
    ) {
        OutlinedTextField(
            label = {
                Text(
                    label,
                    fontFamily = MaterialTheme.typography.headlineLarge.fontFamily
                )
            },
            textStyle = MaterialTheme.typography.headlineLarge,
            colors = RWOutlinedTextColors,
            value = value,
            enabled = enabled,
            singleLine = true,
            modifier = modifier,
            trailingIcon = trailingIcon,
            leadingIcon = leadingIcon,
            onValueChange = { if(it.length <= lengthLimitCount && (!typeInNumberOnly || !typeInOnlyInteger || it.all { s -> s.isDigit() })) onValueChange(it) },
            keyboardOptions = KeyboardOptions(keyboardType = if(typeInNumberOnly) KeyboardType.Number else KeyboardType.Text)
        )

        appendedContent?.invoke()
    }
}

@Composable
fun RWTextButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) = OutlinedButton(
    onClick = onClick,
    modifier = modifier,
    border = BorderStroke(1.dp, Color(151, 188, 98))
) {
    Text(label, style = MaterialTheme.typography.headlineLarge)
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
    strokeColor: Color = Color(199, 234, 70)
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

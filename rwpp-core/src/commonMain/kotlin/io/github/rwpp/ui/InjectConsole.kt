/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import io.github.rwpp.inject.BuildLogger
import io.github.rwpp.logger
import io.github.rwpp.widget.BorderCard
import io.github.rwpp.widget.RWTextFieldColors

internal var logStr = mutableStateOf(AnnotatedString(""))

@Composable
fun InjectConsole() {
    val message by remember(logStr) { logStr }
    var log by remember(message) {
        mutableStateOf(
            TextFieldValue(message, TextRange(message.lastIndex.coerceAtLeast(0)))
        )
    }
    BorderCard(
        modifier = Modifier
            .fillMaxSize(.7f),
    ) {
        Text(
            "Inject Console",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 5.dp)
                .align(Alignment.CenterHorizontally)
        )

        HorizontalDivider(
            thickness = 3.dp,
            modifier = Modifier.padding(top = 2.dp, bottom = 5.dp),
            color = MaterialTheme.colorScheme.primary
        )

        TextField(
            value = log,
            onValueChange = { log = it },
            readOnly = true,
            textStyle = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth().weight(1f).padding(5.dp),
            colors = RWTextFieldColors,
        )
    }
}

val defaultBuildLogger: BuildLogger = object : BuildLogger {
    override fun logging(message: String) {
        logger.trace(message)
        logStr.value = logStr.value + buildAnnotatedString {
            withStyle(style = SpanStyle(color = Color.Gray)) {
                append("[L] $message")
            }
            append("\n")
        }
    }

    override fun info(message: String) {
        logger.info(message)
        logStr.value = logStr.value + buildAnnotatedString {
            withStyle(style = SpanStyle(color = Color.Green)) {
                append("[I] $message")
            }
            append("\n")
        }
    }

    override fun warn(message: String) {
        logger.warn(message)
        logStr.value = logStr.value + buildAnnotatedString {
            withStyle(style = SpanStyle(color = Color.Yellow)) {
                append("[W] $message")
            }
            append("\n")
        }
    }

    override fun error(message: String) {
        logger.error(message)
        logStr.value = logStr.value + buildAnnotatedString {
            withStyle(style = SpanStyle(color = Color.Red)) {
                append("[E] $message")
            }
            append("\n")
        }
    }

    override fun exception(e: Throwable) {
        logger.error(e.message, e)
        logStr.value = logStr.value + buildAnnotatedString {
            withStyle(style = SpanStyle(color = Color.Red)) {
                append("[E] " + e.message.toString())
                append("\n")
                append(e.stackTraceToString())
            }
            append("\n")
        }
    }
}
package com.kira.superspm.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.platform.LocalContext
import com.kira.superspm.R

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    hint: String? = null,
    onSearch: (String) -> Unit
) {
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val actualHint = hint ?: context.getString(R.string.search)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MiuixTheme.colorScheme.surfaceVariant),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            modifier = Modifier
                .padding(start = 16.dp, end = 8.dp)
                .size(20.dp)
        )

        BasicTextField(
            value = text,
            onValueChange = {
                text = it
                onSearch(it)
            },
            textStyle = TextStyle(
                fontSize = 15.sp,
                color = MiuixTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MiuixTheme.colorScheme.primary),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    focusManager.clearFocus()
                }
            ),
            decorationBox = { innerTextField ->
                if (text.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        androidx.compose.material3.Text(
                            text = actualHint,
                            style = TextStyle(
                                fontSize = 15.sp,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                            )
                        )
                    }
                }
                innerTextField()
            },
            modifier = Modifier.weight(1f)
        )

        if (text.isNotEmpty()) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = context.getString(R.string.clear_search),
                tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(20.dp)
                    .clickable {
                        text = ""
                        onSearch("")
                    }
            )
        }
    }
}
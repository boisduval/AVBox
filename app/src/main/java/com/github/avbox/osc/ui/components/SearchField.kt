package com.github.avbox.osc.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.avbox.osc.R
import com.github.avbox.osc.ui.theme.cardContainer
import com.kyant.capsule.ContinuousCapsule

@Composable
fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    hint: String = stringResource(R.string.search_field_hint),
    trailing: (@Composable () -> Unit)? = null,
) {
    val focusManager = LocalFocusManager.current
    var fieldFocused by remember { mutableStateOf(false) }
    ClearFocusOnImeDismiss(focusManager, fieldFocused)
    val glass = if (LocalTopBarGlassBackdrop.current != null) {
        Modifier.glassTopBarSurface(ContinuousCapsule, MaterialTheme.colorScheme.cardContainer, pressEffect = false)
    } else {
        Modifier.glassSurface(ContinuousCapsule, MaterialTheme.colorScheme.cardContainer, pressEffect = false)
    }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .then(glass),
        )
        Row(
            modifier = Modifier
                .clip(ContinuousCapsule)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.width(8.dp))
            var tfv by remember { mutableStateOf(TextFieldValue(query, TextRange(query.length))) }
            if (tfv.text != query) {
                tfv = TextFieldValue(query, TextRange(query.length))
            }
            BasicTextField(
                value = tfv,
                onValueChange = { next ->
                    tfv = next
                    if (next.text != query) onQueryChange(next.text)
                },
                modifier = Modifier
                    .weight(1f)
                    .onFocusChanged { fieldFocused = it.isFocused }
                    .onPreviewKeyEvent { keyEvent ->
                        if (keyEvent.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                        when (keyEvent.key.nativeKeyCode) {
                            android.view.KeyEvent.KEYCODE_DPAD_RIGHT -> {
                                if (tfv.selection.collapsed && tfv.selection.end >= tfv.text.length) {
                                    focusManager.moveFocus(FocusDirection.Right)
                                    true
                                } else {
                                    false
                                }
                            }
                            android.view.KeyEvent.KEYCODE_DPAD_LEFT -> {
                                if (tfv.selection.collapsed && tfv.selection.start <= 0) {
                                    focusManager.moveFocus(FocusDirection.Left)
                                    true
                                } else {
                                    false
                                }
                            }
                            android.view.KeyEvent.KEYCODE_DPAD_DOWN -> {
                                focusManager.moveFocus(FocusDirection.Down)
                                true
                            }
                            android.view.KeyEvent.KEYCODE_DPAD_UP -> {
                                focusManager.moveFocus(FocusDirection.Up)
                                true
                            }
                            else -> false
                        }
                    },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                decorationBox = { inner ->
                    Box(
                        modifier = Modifier.height(40.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (query.isEmpty()) {
                            Text(
                                text = hint,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        inner()
                    }
                },
            )
            if (query.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.common_clear),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onQueryChange("") }
                        .padding(4.dp)
                        .size(18.dp),
                )
            }
            if (trailing != null) {
                Spacer(modifier = Modifier.width(4.dp))
                trailing()
            }
        }
    }
}

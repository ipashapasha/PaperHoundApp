package com.example.paperhoundapp.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.example.paperhoundapp.ui.theme.FieldUnderlineDark
import com.example.paperhoundapp.ui.theme.FieldUnderlineFocusedDark
import com.example.paperhoundapp.ui.theme.FieldUnderlineFocusedLight
import com.example.paperhoundapp.ui.theme.FieldUnderlineLight

/** The app's text field: no outline or fill, only a soft beige underline, which turns red on [isError]. */
@Composable
fun UnderlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    label: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    interactionSource: MutableInteractionSource? = null,
) {
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        readOnly = readOnly,
        label = label,
        trailingIcon = trailingIcon,
        supportingText = supportingText,
        isError = isError,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        interactionSource = interactionSource,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            errorContainerColor = Color.Transparent,
            focusedIndicatorColor = if (dark) FieldUnderlineFocusedDark else FieldUnderlineFocusedLight,
            unfocusedIndicatorColor = if (dark) FieldUnderlineDark else FieldUnderlineLight,
            disabledIndicatorColor = if (dark) FieldUnderlineDark else FieldUnderlineLight,
        ),
    )
}

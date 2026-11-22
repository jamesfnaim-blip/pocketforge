package com.jarves.mh.ui.v2.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.jarves.mh.ui.components.PhosphorIcon
import com.jarves.mh.ui.components.PhosphorIconType
import com.jarves.mh.ui.v2.theme.ForgeTheme

@Composable
fun ForgeInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: PhosphorIconType? = null,
    trailingIcon: PhosphorIconType? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    errorMessage: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else 5,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val colors = ForgeTheme.colors
    val typography = ForgeTheme.typography
    val shapes = ForgeTheme.shapes

    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null) {
            Text(
                text = label,
                style = typography.labelMedium,
                color = colors.textSecondary,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = singleLine,
            maxLines = maxLines,
            textStyle = typography.bodyLarge.copy(color = colors.textPrimary),
            shape = shapes.input,
            placeholder = if (placeholder != null) {
                {
                    Text(
                        text = placeholder,
                        style = typography.bodyLarge,
                        color = colors.textMuted,
                    )
                }
            } else null,
            leadingIcon = if (leadingIcon != null) {
                {
                    PhosphorIcon(
                        icon = leadingIcon,
                        size = 18.dp,
                        tint = colors.textSecondary,
                    )
                }
            } else null,
            trailingIcon = if (trailingIcon != null) {
                {
                    if (onTrailingIconClick != null) {
                        IconButton(onClick = onTrailingIconClick, modifier = Modifier.size(24.dp)) {
                            PhosphorIcon(
                                icon = trailingIcon,
                                size = 18.dp,
                                tint = colors.textSecondary,
                            )
                        }
                    } else {
                        PhosphorIcon(
                            icon = trailingIcon,
                            size = 18.dp,
                            tint = colors.textSecondary,
                        )
                    }
                }
            } else null,
            isError = errorMessage != null,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.surface,
                unfocusedContainerColor = colors.surface,
                disabledContainerColor = colors.surface.copy(alpha = 0.5f),
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.border,
                errorBorderColor = colors.error,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary,
                cursorColor = colors.primary,
            ),
        )
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = typography.labelSmall,
                color = colors.error,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
            )
        }
    }
}

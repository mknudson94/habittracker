package com.mk.habittracker.core.ui

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun HabitButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconPainter: Painter? = null,
    isLoading: Boolean = false,
    size: ButtonSize = ButtonSize.Small,
    shape: Shape = ButtonDefaults.shape,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
) {
    val sizeDp = size.toSizeDp()
    Button(
        modifier = modifier.heightIn(min = sizeDp),
        onClick = onClick,
        enabled = !isLoading,
        shape = shape,
        colors = colors,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = LocalContentColor.current
            )
        } else {
            iconPainter?.let {
                Icon(
                    painter = iconPainter,
                    contentDescription = null,
                )
            }
            Text(text)
        }
    }
}

enum class ButtonSize {
    ExtraSmall,
    Small,
    Medium,
    Large,
    ExtraLarge,
    ;

    fun toSizeDp(): Dp = ButtonDefaults.MinHeight
}

package com.mk.habittracker.core.ui

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp

@Composable
fun HabitButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconPainter: Painter? = null,
    size: ButtonSize = ButtonSize.Small,
    shape: Shape = ButtonDefaults.shape,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
) {
    val sizeDp = size.toSizeDp()
    Button(
        modifier = modifier.heightIn(sizeDp),
        onClick = onClick,
        shape = shape,
        colors = colors,
    ) {
        iconPainter?.let {
            Icon(
                painter = iconPainter,
                contentDescription = null,
            )
        }
        Text(text)
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

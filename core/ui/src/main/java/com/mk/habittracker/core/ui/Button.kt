package com.mk.habittracker.core.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
        contentPadding = ButtonDefaults.contentPaddingFor(sizeDp),
        onClick = onClick,
        shape = shape,
        colors = colors,
    ) {
        iconPainter?.let {
            Icon(
                painter = iconPainter,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.iconSizeFor(sizeDp)),
            )
            Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(sizeDp)))
        }
        Text(text, style = ButtonDefaults.textStyleFor(sizeDp))
    }
}

enum class ButtonSize {
    ExtraSmall,
    Small,
    Medium,
    Large,
    ExtraLarge,
    ;

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    fun toSizeDp(): Dp =
        when (this) {
            ExtraSmall -> ButtonDefaults.ExtraSmallContainerHeight
            Small -> ButtonDefaults.MinHeight
            Medium -> ButtonDefaults.MediumContainerHeight
            Large -> ButtonDefaults.LargeContainerHeight
            ExtraLarge -> ButtonDefaults.ExtraLargeContainerHeight
        }
}

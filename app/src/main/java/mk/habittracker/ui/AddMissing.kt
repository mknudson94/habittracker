package mk.habittracker.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path

public val Icons.Filled.AddMissing: ImageVector
    get() {
        if (_addMissing != null) {
            return _addMissing!!
        }
        _addMissing = materialIcon(name = "Filled.AddMissing") {
            materialPath {
                moveTo(19.0f, 13.0f)
                horizontalLineToRelative(-6.0f)
                verticalLineToRelative(6.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-6.0f)
                horizontalLineTo(5.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(6.0f)
                verticalLineTo(5.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(6.0f)
                horizontalLineToRelative(6.0f)
                verticalLineToRelative(2.0f)
                close()
            }
            path(
                stroke = SolidColor(Color.Black)
            ) {
                // TODO: dashed line
                moveTo(12.0f, 2.5f)
                quadToRelative(10.0f, 0f, 10.0f, 10.0f)
                quadToRelative(0f, 10.0f, -10.0f, 10.0f)
                quadToRelative(-10.0f, 0f, -10.0f, -10.0f)
                quadToRelative(0f, -10.0f, 10.0f, -10.0f)
//                arcToRelative(10.0f, 10.0f, 90.0f, false, false, 10.0f, -10.0f)

            }
        }
        return _addMissing!!
    }

private var _addMissing: ImageVector? = null
package dev.nkosanamahungela.sonify.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Locally bundled Lucide-style outline icons on a consistent 24px grid.
 * Their SVG path geometry is included in the APK, so navigation stays crisp
 * offline without a remote font or image request.
 */
object LucideIcons {
    private fun icon(name: String, vararg paths: String): ImageVector {
        val builder = ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        )
        paths.forEach { data ->
            builder.addPath(
                pathData = PathParser().parsePathString(data).toPath(),
                fill = SolidColor(Color.Transparent),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
        return builder.build()
    }

    val House: ImageVector by lazy {
        icon("House", "m3 10.5 9-7.5 9 7.5", "M5 9v11h14V9", "M9 20v-6h6v6")
    }

    val Search: ImageVector by lazy {
        icon("Search", "M11 19a8 8 0 1 0 0-16 8 8 0 0 0 0 16z", "m21 21-4.35-4.35")
    }

    val Library: ImageVector by lazy {
        icon(
            "Library",
            "M4 19.5A2.5 2.5 0 0 1 6.5 17H20",
            "M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z",
            "M8 7h8",
            "M8 11h8",
        )
    }

    val Grid: ImageVector by lazy {
        icon("Grid", "M3 3h7v7H3z", "M14 3h7v7h-7z", "M14 14h7v7h-7z", "M3 14h7v7H3z")
    }

    val Radio: ImageVector by lazy {
        icon(
            "Radio",
            "M12 12h.01",
            "M16.24 7.76a6 6 0 0 1 0 8.49",
            "M7.76 16.24a6 6 0 0 1 0-8.49",
            "M19.07 4.93a10 10 0 0 1 0 14.14",
            "M4.93 19.07a10 10 0 0 1 0-14.14",
        )
    }
}

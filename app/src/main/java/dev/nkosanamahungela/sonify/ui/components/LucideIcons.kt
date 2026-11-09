package dev.nkosanamahungela.sonify.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Locally bundled Lucide SVG icons, converted to Compose vectors.
 * Paths follow the official Lucide 24px SVGs and render without network access.
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
        icon(
            "House",
            "M15 21v-8a1 1 0 0 0-1-1h-4a1 1 0 0 0-1 1v8",
            "M3 10a2 2 0 0 1 .709-1.528l7-6a2 2 0 0 1 2.582 0l7 6A2 2 0 0 1 21 10v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z",
        )
    }

    val Search: ImageVector by lazy {
        icon("Search", "m21 21-4.34-4.34", "M11 19a8 8 0 1 0 0-16 8 8 0 0 0 0 16")
    }

    val Library: ImageVector by lazy {
        icon(
            "Library",
            "m16 6 4 14",
            "M12 6v14",
            "M8 8v12",
            "M4 4v16",
        )
    }

    val Grid: ImageVector by lazy {
        icon(
            "Grid",
            "M4 3h5a1 1 0 0 1 1 1v5a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1z",
            "M15 3h5a1 1 0 0 1 1 1v5a1 1 0 0 1-1 1h-5a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1z",
            "M15 14h5a1 1 0 0 1 1 1v5a1 1 0 0 1-1 1h-5a1 1 0 0 1-1-1v-5a1 1 0 0 1 1-1z",
            "M4 14h5a1 1 0 0 1 1 1v5a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1v-5a1 1 0 0 1 1-1z",
        )
    }

    val Radio: ImageVector by lazy {
        icon(
            "Radio",
            "M16.247 7.761a6 6 0 0 1 0 8.478",
            "M19.075 4.933a10 10 0 0 1 0 14.134",
            "M4.925 19.067a10 10 0 0 1 0-14.134",
            "M7.753 16.239a6 6 0 0 1 0-8.478",
            "M12 10a2 2 0 1 0 0 4 2 2 0 0 0 0-4z",
        )
    }
}

package ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import kotlin.random.Random

data class FloatingPathData(
    val id: Int,
    val path: Path,
    val color: Color,
    val strokeWidth: Float,
    val durationMillis: Int,
    val baseOpacity: Float
)

@Composable
fun FloatingPathsBackground(
    modifier: Modifier = Modifier,
    position: Float = 1f,
    children: @Composable BoxScope.() -> Unit
) {
    // Pre-calculate paths
    val paths = List(36) { i ->
        val p = Path().apply {
            val startX = -(380f - i * 5f * position)
            val startY = -(189f + i * 6f)
            moveTo(startX, startY)
            
            cubicTo(
                startX, startY,
                -(312f - i * 5f * position), (216f - i * 6f),
                (152f - i * 5f * position), (343f - i * 6f)
            )
            
            cubicTo(
                (616f - i * 5f * position), (470f - i * 6f),
                (684f - i * 5f * position), (875f - i * 6f),
                (684f - i * 5f * position), (875f - i * 6f)
            )
        }
        
        FloatingPathData(
            id = i,
            path = p,
            color = Color(15, 23, 42),
            strokeWidth = 0.5f + i * 0.03f,
            durationMillis = (20000 + Random.nextInt(10000)),
            baseOpacity = 0.1f + i * 0.03f
        )
    }

    val infiniteTransition = rememberInfiniteTransition()
    
    // Create animated progress for each path
    val pathProgresses = paths.map { pathData ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(pathData.durationMillis, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }
    
    val pathOpacities = paths.map { pathData ->
        infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.6f,
            animationSpec = infiniteRepeatable(
                animation = tween(pathData.durationMillis / 2, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // The original viewBox was 696x316. 
            // We scale the canvas to map this coordinate space proportionally.
            val scaleX = size.width / 696f
            val scaleY = size.height / 316f
            val canvasScale = maxOf(scaleX, scaleY)
            
            scale(scaleX = canvasScale, scaleY = canvasScale, pivot = androidx.compose.ui.geometry.Offset.Zero) {
                paths.forEachIndexed { index, pathData ->
                    val progress = pathProgresses[index].value
                    val opacityMod = pathOpacities[index].value
                    
                    val pathMeasure = PathMeasure()
                    pathMeasure.setPath(pathData.path, false)
                    val length = pathMeasure.length
                    
                    val segment = Path()
                    // Draw a trailing segment that moves along the path
                    val startDist = length * (progress - 0.3f).coerceAtLeast(0f)
                    val endDist = length * progress
                    pathMeasure.getSegment(startDist, endDist, segment, true)
                    
                    drawPath(
                        path = segment,
                        color = pathData.color.copy(alpha = pathData.baseOpacity * opacityMod),
                        style = Stroke(width = pathData.strokeWidth)
                    )
                }
            }
        }
        
        children()
    }
}

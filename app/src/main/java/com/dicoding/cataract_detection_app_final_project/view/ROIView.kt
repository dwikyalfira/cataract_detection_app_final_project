package com.dicoding.cataract_detection_app_final_project.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.dicoding.cataract_detection_app_final_project.R
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import kotlin.math.max
import kotlin.math.min

data class ROIRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
    val centerX: Float get() = (left + right) / 2
    val centerY: Float get() = (top + bottom) / 2
    
    fun isValid(): Boolean = width > 0 && height > 0
    
    fun normalize(imageWidth: Float, imageHeight: Float): ROIRect {
        return ROIRect(
            left = left / imageWidth,
            top = top / imageHeight,
            right = right / imageWidth,
            bottom = bottom / imageHeight
        )
    }
    
    fun denormalize(imageWidth: Float, imageHeight: Float): ROIRect {
        return ROIRect(
            left = left * imageWidth,
            top = top * imageHeight,
            right = right * imageWidth,
            bottom = bottom * imageHeight
        )
    }
}

data class ImageAdjustments(
    val scale: Float = 1.0f,
    val offsetX: Float = 0.0f,
    val offsetY: Float = 0.0f
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ROIView(
    imageUri: String,
    onROIConfirmed: (ROIRect, ImageAdjustments) -> Unit,
    onCancel: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    val context = LocalContext.current
    var imageScale by remember { mutableFloatStateOf(1f) }
    var imageOffset by remember { mutableStateOf(Offset.Zero) }
    var imageSize by remember { mutableStateOf(Size.Zero) }
    var containerSize by remember { mutableStateOf(Size.Zero) }

    val minScale = 0.5f
    val maxScale = 3.0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Instructions
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.roi_instruction),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.current_scale, imageScale),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Image display area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clipToBounds()
                .background(Color.Black)
                .onGloballyPositioned { coordinates ->
                    containerSize = coordinates.size.toSize()
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        imageScale = (imageScale * zoom).coerceIn(minScale, maxScale)
                        val newOffset = imageOffset + pan
                        imageOffset = newOffset
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUri)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(R.string.image_label),
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .offset(imageOffset.x.dp, imageOffset.y.dp)
                    .scale(imageScale),
                contentScale = ContentScale.Fit,
                onSuccess = { success ->
                    imageSize = Size(
                        success.painter.intrinsicSize.width,
                        success.painter.intrinsicSize.height
                    )
                }
            )

            // Fixed Ellipse Overlay
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        compositingStrategy = CompositingStrategy.Offscreen
                    }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Define the fixed ellipse in the center of the canvas
                val ellipseWidth = canvasWidth * 0.6f
                val ellipseHeight = canvasHeight * 0.6f
                val ellipseRect = Rect(
                    left = (canvasWidth - ellipseWidth) / 2f,
                    top = (canvasHeight - ellipseHeight) / 2f,
                    right = (canvasWidth + ellipseWidth) / 2f,
                    bottom = (canvasHeight + ellipseHeight) / 2f
                )

                // Draw semi-transparent overlay outside the ellipse
                drawRect(
                    color = Color.Black.copy(alpha = 0.6f),
                    size = size
                )
                drawOval(
                    color = Color.Transparent,
                    topLeft = ellipseRect.topLeft,
                    size = ellipseRect.size,
                    blendMode = androidx.compose.ui.graphics.BlendMode.Clear
                )

                // Draw ellipse border with dashed line
                drawOval(
                    color = Color.Cyan,
                    topLeft = ellipseRect.topLeft,
                    size = ellipseRect.size,
                    style = Stroke(
                        width = 4.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                )
            }
        }

        // Zoom controls
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { imageScale = max(minScale, imageScale - 0.1f) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Filled.ZoomOut, 
                        contentDescription = stringResource(R.string.zoom_out),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value = imageScale,
                    onValueChange = { imageScale = it },
                    valueRange = minScale..maxScale,
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
                )
                IconButton(
                    onClick = { imageScale = min(maxScale, imageScale + 0.1f) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Filled.ZoomIn, 
                        contentDescription = stringResource(R.string.zoom_in),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    if (imageSize != Size.Zero && containerSize != Size.Zero) {
                        // 1. Define Ellipse Rect in Screen Coordinates (centered in container)
                        val ellipseWidth = containerSize.width * 0.6f
                        val ellipseHeight = containerSize.height * 0.6f
                        val ellipseLeft = (containerSize.width - ellipseWidth) / 2f
                        val ellipseTop = (containerSize.height - ellipseHeight) / 2f
                        val ellipseRect = Rect(
                            left = ellipseLeft,
                            top = ellipseTop,
                            right = ellipseLeft + ellipseWidth,
                            bottom = ellipseTop + ellipseHeight
                        )

                        // 2. Calculate ScaleToFit factor (how the image is fitted in the container initially)
                        val scaleToFit = min(
                            containerSize.width / imageSize.width,
                            containerSize.height / imageSize.height
                        )
                        
                        // 3. Calculate Rendered Image Bounds (before user zoom/pan)
                        val renderedImageWidth = imageSize.width * scaleToFit
                        val renderedImageHeight = imageSize.height * scaleToFit
                        val renderedImageLeft = (containerSize.width - renderedImageWidth) / 2f
                        val renderedImageTop = (containerSize.height - renderedImageHeight) / 2f

                        // 4. Map Ellipse Corners to Image Coordinates
                        val centerX = containerSize.width / 2f
                        val centerY = containerSize.height / 2f
                        
                        fun mapScreenToNormalizedImage(screenX: Float, screenY: Float): Offset {
                            val localX = (screenX - imageOffset.x - centerX) / imageScale + centerX
                            val localY = (screenY - imageOffset.y - centerY) / imageScale + centerY
                            
                            val imagePixelX = (localX - renderedImageLeft) / scaleToFit
                            val imagePixelY = (localY - renderedImageTop) / scaleToFit
                            
                            return Offset(
                                imagePixelX / imageSize.width,
                                imagePixelY / imageSize.height
                            )
                        }

                        val topLeft = mapScreenToNormalizedImage(ellipseRect.left, ellipseRect.top)
                        val bottomRight = mapScreenToNormalizedImage(ellipseRect.right, ellipseRect.bottom)

                        val calculatedRoi = ROIRect(
                            left = topLeft.x,
                            top = topLeft.y,
                            right = bottomRight.x,
                            bottom = bottomRight.y
                        )
                        
                        val identityAdjustments = ImageAdjustments(scale = 1.0f, offsetX = 0f, offsetY = 0f)

                        onROIConfirmed(calculatedRoi, identityAdjustments)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    stringResource(R.string.start_analysis), 
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun ROIViewPreview() {
    ROIView(
        imageUri = "https://example.com/image.jpg",
        onROIConfirmed = { _, _ -> },
        onCancel = {}
    )
}
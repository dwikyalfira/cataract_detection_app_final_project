package com.dicoding.cataract_detection_app_final_project.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.dicoding.cataract_detection_app_final_project.R
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
    var isTipsExpanded by remember { mutableStateOf(false) }

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

        // ROI Tips Dropdown Card - Enhanced for visibility
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clickable { isTipsExpanded = !isTipsExpanded },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
        ) {
            Column(
                modifier = Modifier.padding(14.dp)
            ) {
                // Header Row with attention indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Attention indicator
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(4.dp)
                                )
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = stringResource(R.string.roi_tips_title),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isTipsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isTipsExpanded) stringResource(R.string.collapse) else stringResource(R.string.expand),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                // Expandable Content
                AnimatedVisibility(
                    visible = isTipsExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        // ROI Example Image with better contrast for dark mode
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_roi_example),
                                contentDescription = stringResource(R.string.roi_tips_title),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(8.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Text(
                            text = stringResource(R.string.roi_tips_content),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        // Good/Bad Examples with better dark mode colors
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Good column
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        Color(0xFF1B5E20).copy(alpha = 0.15f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.roi_example_good),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF4CAF50) // Material Green that works in both modes
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(stringResource(R.string.roi_tip_centered), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text(stringResource(R.string.roi_tip_closeup), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text(stringResource(R.string.roi_tip_clear), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            // Bad column
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.roi_example_bad),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(stringResource(R.string.roi_tip_avoid_blur), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text(stringResource(R.string.roi_tip_avoid_partial), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        // Adjust Tip with better visibility
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = stringResource(R.string.roi_tip_adjust),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
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
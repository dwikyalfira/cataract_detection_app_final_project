package com.dicoding.cataract_detection_app_final_project.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun ResultView(
    predictionResult: String = "Normal",
    scannedImageUri: String? = null,
    isNavigating: Boolean = false,
    onBackToHome: () -> Unit = {},
    onTryAnotherImage: () -> Unit = {}
) {
    // Don't render if we don't have valid data or if we're navigating
    if ((predictionResult.isEmpty() && scannedImageUri == null) || isNavigating) {
        return
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
//        // Title
//        Text(
//            text = "Detection Result",
//            style = MaterialTheme.typography.headlineMedium.copy(
//                fontWeight = FontWeight.Bold,
//                fontSize = 28.sp
//            ),
//            textAlign = TextAlign.Center,
//            modifier = Modifier.padding(vertical = 24.dp)
//        )
        
        // Result Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Scanned Image Display
                if (scannedImageUri != null) {
                    Card(
                        modifier = Modifier
                            .size(250.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(scannedImageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Scanned Eye Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
                    // Fallback when no image is available
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "No Image Available",
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Result Text
                Text(
                    text = "Result: $predictionResult",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    textAlign = TextAlign.Center,
                    color = if (predictionResult == "Normal") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Confidence Score (dummy)
                Text(
                    text = "Confidence: 85%",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Additional Info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Analysis Summary:",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (predictionResult == "Normal") {
                                "No signs of cataract detected. Your eye appears to be healthy. Continue regular eye checkups."
                            } else {
                                "Potential signs detected. Please consult with an ophthalmologist for professional evaluation."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Try Another Image Button
//            Button(
//                onClick = onTryAnotherImage,
//                modifier = Modifier.fillMaxWidth(),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(0xFF4CAF50)
//                ),
//                shape = RoundedCornerShape(12.dp)
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Refresh,
//                    contentDescription = "Try Another Image",
//                    modifier = Modifier.size(20.dp)
//                )
//                Spacer(modifier = Modifier.width(8.dp))
//                Text("Try Another Image")
//            }
            
            // Back Button
//            Button(
//                onClick = onBackToHome,
//                modifier = Modifier.fillMaxWidth(),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = MaterialTheme.colorScheme.surface,
//                    contentColor = MaterialTheme.colorScheme.onSurface
//                ),
//                shape = RoundedCornerShape(12.dp)
//            ) {
//                Icon(
//                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
//                    contentDescription = "Back",
//                    modifier = Modifier.size(20.dp)
//                )
//                Spacer(modifier = Modifier.width(8.dp))
//                Text("Back")
//            }
            
            // Back to Home Button
            Button(
                onClick = onTryAnotherImage,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector =Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Home",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Back")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ResultViewPreview() {
    ResultView()
}

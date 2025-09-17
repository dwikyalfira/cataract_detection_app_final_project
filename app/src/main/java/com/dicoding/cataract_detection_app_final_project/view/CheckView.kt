package com.dicoding.cataract_detection_app_final_project.view

//import androidx.compose.material.icons.filled.CameraAlt
//import androidx.compose.material.icons.filled.Upload
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckView(
    onUploadImage: () -> Unit = {},
    onCaptureImage: () -> Unit = {},
    isLoading: Boolean = false,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Instructions Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE3F2FD)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "How to use:",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF1976D2)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "1. Upload or capture an image of an eye\n2. The AI will analyze the image\n3. Get instant results about cataract detection",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF1976D2)
                )
            }
        }
        
        // Image Preview Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8F9FA)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Image Preview",
                        modifier = Modifier.size(64.dp),
                        tint = Color(0xFF1976D2)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Image Preview Area",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF1976D2)
                    )
                }
            }
        }
        
        // Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Upload Image Button
            Button(
                onClick = onUploadImage,
                modifier = Modifier.weight(1f),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1976D2)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Upload",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Upload Image")
            }
            
            // Capture Image Button
            Button(
                onClick = onCaptureImage,
                modifier = Modifier.weight(1f),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Camera",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Capture Image")
            }
        }
        
        // Loading indicator
        if (isLoading) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF8F9FA)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF1976D2)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Analyzing image...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF1976D2)
                    )
                }
            }
        }
        
        // Tips Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFF3E0)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "💡 Tips for better results:",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFFE65100)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "• Ensure good lighting\n• Keep the eye centered in the frame\n• Avoid blurry images\n• Use high resolution images",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFE65100)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun CheckViewPreview() {
    CheckView()
}

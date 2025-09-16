package com.dicoding.cataract_detection_app_final_project.view

//import androidx.compose.material.icons.filled.CameraAlt
//import androidx.compose.material.icons.filled.Image
//import androidx.compose.material.icons.filled.Upload
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView(
    onUploadImage: () -> Unit = {},
    onCaptureImage: () -> Unit = {},
    onNavigateToInfo: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
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
        // Title
        Text(
            text = "Eye Health Information",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 24.dp)
        )
        
        // Eye Health Overview Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
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
                    text = "👁️ Eye Health Overview",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF1976D2)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Your eyes are one of your most important senses. Maintaining good eye health is crucial for your overall well-being and quality of life. Regular eye check-ups and early detection of eye conditions can help preserve your vision.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF1976D2)
                )
            }
        }
        
        // Cataract Information Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFF3E0)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "🔍 What are Cataracts?",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFFE65100)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Cataracts are a clouding of the eye's natural lens, which lies behind the iris and pupil. They are the most common cause of vision loss in people over age 40 and are the principal cause of blindness worldwide.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFE65100)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Common symptoms include:\n• Blurry or cloudy vision\n• Difficulty seeing at night\n• Sensitivity to light and glare\n• Seeing halos around lights\n• Frequent changes in eyeglass prescription",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFE65100)
                )
            }
        }
        
        // Prevention Tips Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE8F5E8)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "🛡️ Prevention Tips",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF2E7D32)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "While cataracts are largely age-related, you can take steps to protect your eyes:\n\n• Wear sunglasses with UV protection\n• Quit smoking\n• Eat a healthy diet rich in antioxidants\n• Manage diabetes and other health conditions\n• Get regular eye examinations\n• Limit alcohol consumption",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF2E7D32)
                )
            }
        }
        
        // Treatment Information Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF3E5F5)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "🏥 Treatment Options",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF7B1FA2)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Cataract surgery is one of the most common and successful procedures performed today. The surgery involves removing the clouded lens and replacing it with a clear artificial lens.\n\nEarly detection through regular eye exams is key to successful treatment and maintaining good vision.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF7B1FA2)
                )
            }
        }
        
        // App Information Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE0F2F1)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "📱 About This App",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF00695C)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "This app uses AI technology to help detect potential cataracts in eye images. Use the 'Check' tab to analyze images, and visit your 'Profile' for personal statistics and information.\n\nRemember: This app is for informational purposes only and should not replace professional medical advice.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF00695C)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun HomeViewPreview() {
    HomeView()
}

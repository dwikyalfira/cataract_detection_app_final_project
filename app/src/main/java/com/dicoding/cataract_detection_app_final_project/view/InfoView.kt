package com.dicoding.cataract_detection_app_final_project.view

//import androidx.compose.material.icons.filled.LocalHospital
//import androidx.compose.material.icons.filled.Shield
//import androidx.compose.material.icons.filled.Visibility
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InfoView(
    onBackToHome: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1976D2)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "About Cataract",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
        
        // Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // What is Cataract
            InfoSection(
                title = "What is Cataract?",
                icon = Icons.Default.Info,
                content = "A cataract is a clouding of the normally clear lens of the eye. " +
                        "For people who have cataracts, seeing through cloudy lenses is a bit like " +
                        "looking through a frosty or fogged-up window. Clouded vision caused by " +
                        "cataracts can make it more difficult to read, drive a car (especially at night) " +
                        "or see the expression on a friend's face."
            )
            
            // Common Symptoms
            InfoSection(
                title = "Common Symptoms",
                icon = Icons.Default.Warning,
                content = "• Clouded, blurred or dim vision\n" +
                        "• Increasing difficulty with vision at night\n" +
                        "• Sensitivity to light and glare\n" +
                        "• Need for brighter light for reading and other activities\n" +
                        "• Seeing 'halos' around lights\n" +
                        "• Frequent changes in eyeglass or contact lens prescription\n" +
                        "• Fading or yellowing of colors\n" +
                        "• Double vision in a single eye"
            )
            
            // Prevention
            InfoSection(
                title = "Prevention",
                icon = Icons.Default.Info,
                content = "• Have regular eye examinations\n" +
                        "• Quit smoking\n" +
                        "• Take care of other health problems\n" +
                        "• Choose a healthy diet that includes plenty of fruits and vegetables\n" +
                        "• Wear sunglasses that block ultraviolet B (UVB) rays\n" +
                        "• Reduce alcohol use\n" +
                        "• Maintain a healthy weight"
            )
            
            // Risk Factors
            InfoSection(
                title = "Risk Factors",
                icon = Icons.Default.Person,
                content = "• Increasing age\n" +
                        "• Diabetes\n" +
                        "• Excessive exposure to sunlight\n" +
                        "• Smoking\n" +
                        "• Obesity\n" +
                        "• High blood pressure\n" +
                        "• Previous eye injury or inflammation\n" +
                        "• Previous eye surgery\n" +
                        "• Prolonged use of corticosteroid medications\n" +
                        "• Drinking excessive amounts of alcohol"
            )
            
            // Treatment
            InfoSection(
                title = "Treatment",
                icon = Icons.Default.Info,
                content = "When your prescription glasses can't clear your vision, the only " +
                        "effective treatment for cataracts is surgery. Cataract surgery involves " +
                        "removing the clouded lens and replacing it with a clear artificial lens. " +
                        "The artificial lens, called an intraocular lens, is positioned in the same " +
                        "place as your natural lens. It remains a permanent part of your eye."
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
        
        // Back to Home Button
        Button(
            onClick = onBackToHome,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1976D2)
            )
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Home",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Back to Home")
        }
    }
}

@Composable
private fun InfoSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF333333),
                lineHeight = 20.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InfoViewPreview() {
    InfoView()
}

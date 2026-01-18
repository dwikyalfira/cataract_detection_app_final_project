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
                    contentDescription = androidx.compose.ui.res.stringResource(com.dicoding.cataract_detection_app_final_project.R.string.info),
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = androidx.compose.ui.res.stringResource(com.dicoding.cataract_detection_app_final_project.R.string.about_cataract),
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
                title = androidx.compose.ui.res.stringResource(com.dicoding.cataract_detection_app_final_project.R.string.info_what_is_title),
                icon = Icons.Default.Info,
                content = androidx.compose.ui.res.stringResource(com.dicoding.cataract_detection_app_final_project.R.string.info_what_is_content)
            )
            
            // Common Symptoms
            InfoSection(
                title = androidx.compose.ui.res.stringResource(com.dicoding.cataract_detection_app_final_project.R.string.info_symptoms_title),
                icon = Icons.Default.Warning,
                content = androidx.compose.ui.res.stringResource(com.dicoding.cataract_detection_app_final_project.R.string.info_symptoms_content)
            )
            
            // Prevention
            InfoSection(
                title = androidx.compose.ui.res.stringResource(com.dicoding.cataract_detection_app_final_project.R.string.info_prevention_title),
                icon = Icons.Default.Info,
                content = androidx.compose.ui.res.stringResource(com.dicoding.cataract_detection_app_final_project.R.string.info_prevention_content)
            )
            
            // Risk Factors
            InfoSection(
                title = androidx.compose.ui.res.stringResource(com.dicoding.cataract_detection_app_final_project.R.string.info_risk_factors_title),
                icon = Icons.Default.Person,
                content = androidx.compose.ui.res.stringResource(com.dicoding.cataract_detection_app_final_project.R.string.info_risk_factors_content)
            )
            
            // Treatment
            InfoSection(
                title = androidx.compose.ui.res.stringResource(com.dicoding.cataract_detection_app_final_project.R.string.info_treatment_title),
                icon = Icons.Default.Info,
                content = androidx.compose.ui.res.stringResource(com.dicoding.cataract_detection_app_final_project.R.string.info_treatment_content)
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
                contentDescription = androidx.compose.ui.res.stringResource(com.dicoding.cataract_detection_app_final_project.R.string.home),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(androidx.compose.ui.res.stringResource(com.dicoding.cataract_detection_app_final_project.R.string.back_to_home))
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

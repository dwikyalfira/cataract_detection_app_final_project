package com.dicoding.cataract_detection_app_final_project.view

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dicoding.cataract_detection_app_final_project.R
import com.google.firebase.auth.FirebaseUser
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileInfoSection(
    icon: ImageVector,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview
@Composable
fun ProfileInfoSectionPreview() {
    ProfileInfoSection(
        icon = Icons.Default.Email,
        title = "Email",
        value = "john.doe@example.com"
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileView(
    onBackToHome: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    currentUser: FirebaseUser? = null,
    userData: Map<String, Any>? = null,
    isLoading: Boolean = false,
    onUpdateName: (String) -> Unit = {}
) {
    val scrollState = rememberScrollState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    
    // Extract user information with detailed debugging
    val firestoreName = userData?.get("name") as? String
    val firebaseDisplayName = currentUser?.displayName
    val userEmail = currentUser?.email ?: "No email"
    
    // Fallback name extraction from email if no name is available
    val fallbackName = if (userEmail != "No email") {
        try {
            userEmail.substringBefore("@").replace(".", " ").split(" ").joinToString(" ") { 
                it.replaceFirstChar { char -> char.uppercase() } 
            }
        } catch (e: Exception) {
            android.util.Log.w("ProfileView", "Error extracting name from email: ${e.message}")
            "User"
        }
    } else {
        "User"
    }
    
    // Fix: Handle empty strings properly (not just null)
    val userName = when {
        !firestoreName.isNullOrBlank() -> firestoreName
        !firebaseDisplayName.isNullOrBlank() -> firebaseDisplayName
        else -> fallbackName
    }
    val joinedDate = userData?.get("createdAt") as? Long
    
    // Fallback: If no joined date from Firestore, use account creation time
    val fallbackJoinedDate = if (joinedDate == null && currentUser != null) {
        currentUser.metadata?.creationTimestamp ?: System.currentTimeMillis()
    } else {
        joinedDate
    }
    
    // Debug logging
    android.util.Log.d("ProfileView", "=== PROFILE VIEW DEBUG ===")
    android.util.Log.d("ProfileView", "userData: $userData")
    android.util.Log.d("ProfileView", "currentUser: $currentUser")
    android.util.Log.d("ProfileView", "currentUser?.uid: ${currentUser?.uid}")
    android.util.Log.d("ProfileView", "currentUser?.email: ${currentUser?.email}")
    android.util.Log.d("ProfileView", "currentUser?.displayName: ${currentUser?.displayName}")
    android.util.Log.d("ProfileView", "firestoreName: $firestoreName")
    android.util.Log.d("ProfileView", "firebaseDisplayName: $firebaseDisplayName")
    android.util.Log.d("ProfileView", "fallbackName: $fallbackName")
    android.util.Log.d("ProfileView", "userName: $userName")
    android.util.Log.d("ProfileView", "userEmail: $userEmail")
    android.util.Log.d("ProfileView", "joinedDate: $joinedDate")
    android.util.Log.d("ProfileView", "fallbackJoinedDate: $fallbackJoinedDate")
    android.util.Log.d("ProfileView", "currentUser?.metadata?.creationTimestamp: ${currentUser?.metadata?.creationTimestamp}")
    android.util.Log.d("ProfileView", "=== END PROFILE VIEW DEBUG ===")
    
    // Format joined date
    val formattedJoinedDate = if (fallbackJoinedDate != null) {
        val date = Date(fallbackJoinedDate)
        val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        formatter.format(date)
    } else {
        "Unknown"
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
//        TopAppBar(
//            title = { Text(stringResource(id = R.string.profile)) },
//            navigationIcon = {
//                IconButton(onClick = onBackToHome) {
//                    Icon(
//                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
//                        contentDescription = stringResource(id = R.string.back)
//                    )
//                }
//            },
//            actions = {
//                IconButton(onClick = onSettingsClick) {
//                    Icon(
//                        imageVector = Icons.Default.Settings,
//                        contentDescription = stringResource(id = R.string.settings)
//                    )
//                }
//            },
//            scrollBehavior = scrollBehavior
//        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        // Title
//        Text(
//            text = "Profile",
//            style = MaterialTheme.typography.headlineMedium.copy(
//                fontWeight = FontWeight.Bold,
//                fontSize = 28.sp
//            ),
//            textAlign = TextAlign.Center,
//            modifier = Modifier.padding(vertical = 16.dp),
//            color = Color(0xFF1976D2)
//        )
        
        // Profile Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Picture
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.size(50.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // User Name with Edit Functionality
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { 
                            newName = userName
                            showEditNameDialog = true 
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(id = R.string.edit_name),
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
//                // Debug information (temporary)
//                Text(
//                    text = "Debug: $userName",
//                    style = MaterialTheme.typography.bodySmall,
//                    color = Color.Red
//                )
//                Text(
//                    text = "Firestore: $firestoreName",
//                    style = MaterialTheme.typography.bodySmall,
//                    color = Color.Red
//                )
//                Text(
//                    text = "Firebase: $firebaseDisplayName",
//                    style = MaterialTheme.typography.bodySmall,
//                    color = Color.Red
//                )
//                Text(
//                    text = "Fallback: $fallbackName",
//                    style = MaterialTheme.typography.bodySmall,
//                    color = Color.Red
//                )
//                Text(
//                    text = "Email: $userEmail",
//                    style = MaterialTheme.typography.bodySmall,
//                    color = Color.Red
//                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // User Email
                Text(
                    text = userEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // User Info Sections
                ProfileInfoSection(
                    icon = Icons.Default.Email,
                    title = "Email",
                    value = userEmail
                )
                
                ProfileInfoSection(
                    icon = Icons.Default.CalendarToday,
                    title = "Joined",
                    value = formattedJoinedDate
                )
                
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Buttons Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Settings Button
            Button(
                onClick = onSettingsClick,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(id = R.string.settings),
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.settings),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            
            // Logout Button
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onError,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = stringResource(id = R.string.logout),
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onError
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isLoading) stringResource(id = R.string.logging_out) else stringResource(id = R.string.logout),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onError
                )
            }
        }
        
        // Bottom padding for better spacing
        Spacer(modifier = Modifier.height(16.dp))
    }
    }
    
    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = stringResource(id = R.string.confirm_logout),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Text(
                    text = stringResource(id = R.string.logout_confirmation),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutClick()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(
                        stringResource(id = R.string.logout),
                        color = MaterialTheme.colorScheme.onError
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        )
    }
    
    
    // Edit Name Dialog
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = {
                Text(
                    text = stringResource(id = R.string.edit_name),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text(stringResource(id = R.string.enter_new_name)) },
                    placeholder = { Text(stringResource(id = R.string.name_hint_edit)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            onUpdateName(newName)
                            showEditNameDialog = false
                        }
                    },
                    enabled = newName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(stringResource(id = R.string.save_changes))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditNameDialog = false }
                ) {
                    Text(stringResource(id = R.string.cancel_edit))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ProfileViewPreview() {
    ProfileView(
        onBackToHome = {},
        onLogoutClick = {},
        onSettingsClick = {},
        currentUser = null, // Provide a mock FirebaseUser if needed for preview
        userData = mapOf("name" to "John Doe", "createdAt" to System.currentTimeMillis()),
        isLoading = false
    )
}
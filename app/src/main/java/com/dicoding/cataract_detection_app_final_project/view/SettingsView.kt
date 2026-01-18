package com.dicoding.cataract_detection_app_final_project.view

import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.dicoding.cataract_detection_app_final_project.R
import com.dicoding.cataract_detection_app_final_project.data.UserPreferences
import com.dicoding.cataract_detection_app_final_project.presenter.AuthPresenter
import kotlinx.coroutines.launch
import java.util.Locale

// Flag Emojis
@Composable
fun EnglishFlagEmoji(): String = "🇬🇧"

@Composable
fun IndonesianFlagEmoji(): String = "🇮🇩"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
    userPreferences: UserPreferences,
    authPresenter: AuthPresenter,
    onLanguageChanged: () -> Unit,
    onDeleteAccountClick: (String, (Boolean, String) -> Unit) -> Unit = { _, _ -> },
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val themeMode by userPreferences.themeMode.collectAsState(initial = userPreferences.getThemeModeSync())
    val language by userPreferences.language.collectAsState(initial = userPreferences.getLanguageSync())
    
    // Listen to auth presenter messages
    val authSuccessMessage by authPresenter.successMessage.collectAsState()
    val authErrorMessage by authPresenter.errorMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    
    // Password change state
    var showPasswordChangeDialog by remember { mutableStateOf(false) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var isChangingPassword by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf("") }
    
    // Delete account state
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var deleteAccountPassword by remember { mutableStateOf("") }
    var showDeleteAccountPassword by remember { mutableStateOf(false) }
    var isDeletingAccount by remember { mutableStateOf(false) }
    var deleteAccountError by remember { mutableStateOf("") }

    fun showSnackbar(message: String) {
        scope.launch {
            snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
        }
    }
    
    fun resetPasswordFields() {
        currentPassword = ""
        newPassword = ""
        confirmPassword = ""
        showCurrentPassword = false
        showNewPassword = false
        showConfirmPassword = false
        passwordError = ""
    }
    
    fun resetDeleteAccountFields() {
        deleteAccountPassword = ""
        showDeleteAccountPassword = false
        deleteAccountError = ""
        isDeletingAccount = false
    }
    
    // Handle auth error messages (success messages are handled in callbacks)
    LaunchedEffect(authErrorMessage) {
        authErrorMessage?.let { message ->
            showSnackbar(message)
            authPresenter.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Section - Minimalist Design
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.settings),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.customize_your_experience),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            // Settings Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                // Theme Section
                SettingsSection(
                    title = stringResource(id = R.string.theme),
                    icon = Icons.Default.SettingsBrightness,
                    content = {
                        val themeOptions = listOf(
                            Triple(UserPreferences.THEME_LIGHT, stringResource(id = R.string.light_theme), Icons.Default.LightMode),
                            Triple(UserPreferences.THEME_DARK, stringResource(id = R.string.dark_theme), Icons.Default.DarkMode),
                            Triple(UserPreferences.THEME_SYSTEM, stringResource(id = R.string.system_default), Icons.Default.SettingsBrightness)
                        )
                        themeOptions.forEachIndexed { index, (option, text, icon) ->
                            SettingsOption(
                                text = text,
                                icon = icon,
                                selected = themeMode == option,
                                isLast = index == themeOptions.lastIndex,
                                onClick = {
                                    if (themeMode != option) {
                                        scope.launch {
                                            userPreferences.setThemeMode(option)
                                            showSnackbar(context.getString(R.string.theme_updated, text.lowercase()))
                                        }
                                    }
                                }
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Language Section
                SettingsSection(
                    title = stringResource(id = R.string.language),
                    icon = Icons.Default.Language,
                    content = {
                        val languageOptions = listOf(
                            Triple(UserPreferences.LANG_INDONESIAN, stringResource(id = R.string.indonesian), IndonesianFlagEmoji()),
                            Triple(UserPreferences.LANG_ENGLISH, stringResource(id = R.string.english), EnglishFlagEmoji())
                        )
                        languageOptions.forEachIndexed { index, (option, text, emoji) ->
                            SettingsOption(
                                text = text,
                                emoji = emoji,
                                selected = language == option,
                                isLast = index == languageOptions.lastIndex,
                                onClick = {
                                    if (language != option) {
                                        scope.launch {
                                            android.util.Log.d("SettingsView", "Setting language to: $option")
                                            
                                            // First, save the language preference
                                            userPreferences.setLanguage(option)
                                            
                                            // Apply locale changes immediately
                                            val locale = when (option) {
                                                UserPreferences.LANG_INDONESIAN -> Locale.forLanguageTag("id")
                                                else -> Locale.ENGLISH
                                            }
                                            
                                            // Set system locale
                                            Locale.setDefault(locale)
                                            
                                            // Set AppCompatDelegate locale
                                            val localeList = LocaleListCompat.create(locale)
                                            AppCompatDelegate.setApplicationLocales(localeList)
                                            
                                            android.util.Log.d("SettingsView", "Locale set to: ${locale.language}")
                                            
                                            // Show success message
                                            val languageName = when (option) {
                                                UserPreferences.LANG_INDONESIAN -> "Bahasa Indonesia"
                                                else -> "English"
                                            }
                                            showSnackbar(context.getString(R.string.language_updated, languageName))
                                            
                                            // Force a longer delay to ensure preferences are saved and locale is applied
                                            kotlinx.coroutines.delay(500)
                                            
                                            // Additional logging for debugging
                                            android.util.Log.d("SettingsView", "About to trigger activity recreation")
                                            
                                            // Trigger activity recreation
                                            onLanguageChanged()
                                            
                                            android.util.Log.d("SettingsView", "Activity recreation triggered")
                                        }
                                    }
                                }
                            )
                        }
                    }
                )
                
                // Password Change Section
                SettingsSection(
                    title = stringResource(id = R.string.change_password),
                    icon = Icons.Default.Lock,
                    content = {
                        FilledTonalButton(
                            onClick = { showPasswordChangeDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = stringResource(id = R.string.change_password),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(id = R.string.change_password),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Delete Account Section
                SettingsSection(
                    title = stringResource(id = R.string.delete_account),
                    icon = Icons.Default.Delete,
                    content = {
                        Button(
                            onClick = { showDeleteAccountDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(id = R.string.delete_account),
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(id = R.string.delete_account),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                )
            }
        }
        
        // Password Change Dialog - Minimalist
        if (showPasswordChangeDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showPasswordChangeDialog = false
                    resetPasswordFields()
                },
                title = {
                    Text(
                        text = stringResource(id = R.string.change_password),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Error Message
                        if (passwordError.isNotEmpty()) {
                            Text(
                                text = passwordError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(12.dp)
                            )
                        }
                        
                        // Current Password
                        OutlinedTextField(
                            value = currentPassword,
                            onValueChange = { 
                                currentPassword = it
                                passwordError = "" // Clear error when user types
                            },
                            label = { Text(stringResource(id = R.string.current_password)) },
                            visualTransformation = if (showCurrentPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                                    Icon(
                                        imageVector = if (showCurrentPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (showCurrentPassword) stringResource(id = R.string.hide_password) else stringResource(id = R.string.show_password)
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = passwordError.isNotEmpty()
                        )
                        
                        // New Password
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { 
                                newPassword = it
                                passwordError = "" // Clear error when user types
                            },
                            label = { Text(stringResource(id = R.string.new_password)) },
                            visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showNewPassword = !showNewPassword }) {
                                    Icon(
                                        imageVector = if (showNewPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (showNewPassword) stringResource(id = R.string.hide_password) else stringResource(id = R.string.show_password)
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = passwordError.isNotEmpty()
                        )
                        
                        // Confirm Password
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { 
                                confirmPassword = it
                                passwordError = "" // Clear error when user types
                            },
                            label = { Text(stringResource(id = R.string.confirm_password)) },
                            visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                    Icon(
                                        imageVector = if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (showConfirmPassword) stringResource(id = R.string.hide_password) else stringResource(id = R.string.show_password)
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = passwordError.isNotEmpty()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (currentPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
                                passwordError = context.getString(R.string.error_fill_all_fields)
                                return@Button
                            }
                            
                            if (newPassword != confirmPassword) {
                                passwordError = context.getString(R.string.error_passwords_do_not_match)
                                return@Button
                            }
                            
                            if (newPassword.length < 6) {
                                passwordError = context.getString(R.string.error_password_too_short)
                                return@Button
                            }
                            
                            isChangingPassword = true
                            passwordError = "" // Clear any previous errors
                            authPresenter.changePassword(currentPassword, newPassword) { success, message ->
                                isChangingPassword = false
                                if (success) {
                                    showPasswordChangeDialog = false
                                    resetPasswordFields()
                                    showSnackbar(message) // Success message can still use snackbar
                                } else {
                                    passwordError = message // Show error in dialog
                                }
                            }
                        },
                        enabled = !isChangingPassword
                    ) {
                        Text(
                            if (isChangingPassword) stringResource(id = R.string.changing) else stringResource(id = R.string.change_password)
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { 
                            showPasswordChangeDialog = false
                            resetPasswordFields()
                        }
                    ) {
                        Text(stringResource(id = R.string.cancel))
                    }
                }
            )
        }
        
        // Delete Account Confirmation Dialog
        if (showDeleteAccountDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showDeleteAccountDialog = false
                    resetDeleteAccountFields()
                },
                title = {
                    Text(
                        text = stringResource(id = R.string.confirm_delete_account),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.delete_account_confirmation),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        // Error Message
                        if (deleteAccountError.isNotEmpty()) {
                            Text(
                                text = deleteAccountError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(12.dp)
                            )
                        }
                        
                        // Password Input
                        OutlinedTextField(
                            value = deleteAccountPassword,
                            onValueChange = { 
                                deleteAccountPassword = it
                                deleteAccountError = "" // Clear error when user types
                            },
                            label = { Text(stringResource(id = R.string.current_password)) },
                            visualTransformation = if (showDeleteAccountPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showDeleteAccountPassword = !showDeleteAccountPassword }) {
                                    Icon(
                                        imageVector = if (showDeleteAccountPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (showDeleteAccountPassword) stringResource(id = R.string.hide_password) else stringResource(id = R.string.show_password)
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = deleteAccountError.isNotEmpty(),
                            enabled = !isDeletingAccount
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (deleteAccountPassword.isBlank()) {
                                deleteAccountError = context.getString(R.string.error_fill_all_fields)
                                return@Button
                            }
                            
                            isDeletingAccount = true
                            deleteAccountError = ""
                            android.util.Log.d("SettingsView", "Starting delete account process")
                            onDeleteAccountClick(deleteAccountPassword) { success, message ->
                                android.util.Log.d("SettingsView", "Delete account callback received - success: $success, message: $message")
                                isDeletingAccount = false
                                if (success) {
                                    android.util.Log.d("SettingsView", "Account deletion successful, closing dialog and showing toast")
                                    showDeleteAccountDialog = false
                                    resetDeleteAccountFields()
                                    // Use Toast instead of snackbar for better reliability
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                } else {
                                    android.util.Log.d("SettingsView", "Account deletion failed, showing error: $message")
                                    deleteAccountError = message
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        enabled = !isDeletingAccount
                    ) {
                        Text(
                            if (isDeletingAccount) stringResource(id = R.string.deleting) else stringResource(id = R.string.delete_account),
                            color = MaterialTheme.colorScheme.onError
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { 
                            showDeleteAccountDialog = false
                            resetDeleteAccountFields()
                        },
                        enabled = !isDeletingAccount
                    ) {
                        Text(stringResource(id = R.string.cancel))
                    }
                }
            )
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Section Header - Minimalist
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 16.dp)
        )
        
        // Section Content Card - Simple
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsOption(
    text: String,
    icon: ImageVector? = null,
    emoji: String? = null,
    selected: Boolean,
    isLast: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected, 
                onClick = onClick, 
                role = Role.RadioButton
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 20.dp)
        ) {
            // Option Icon or Emoji - Simple
            if (emoji != null) {
                Text(
                    text = emoji,
                    fontSize = 20.sp,
                    modifier = Modifier.size(24.dp)
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    modifier = Modifier.size(20.dp),
                    tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            
            // Option Text - Simple
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
                ),
                color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            
            // Radio Button - Simple
            RadioButton(
                selected = selected,
                onClick = null, // Recommended for selectable parent
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary,
                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
        if (!isLast) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SettingsViewPreview() {
    val context = LocalContext.current
    SettingsView(
        userPreferences = UserPreferences(context),
        authPresenter = AuthPresenter(context),
        onLanguageChanged = {},
        onDeleteAccountClick = { _, _ -> },
        scrollBehavior = null
    )
}
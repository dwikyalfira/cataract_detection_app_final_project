package com.dicoding.cataract_detection_app_final_project.view

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dicoding.cataract_detection_app_final_project.R
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterView(
    onRegisterClick: (name: String, email: String, password: String, confirmPassword: String) -> Unit = { _, _, _, _ -> },
    onLoginClick: () -> Unit = {},
    onClearRegistrationState: () -> Unit = {},
    onClearRegistrationScreenState: () -> Unit = {},
    isLoading: Boolean = false,
    errorMessage: String? = null,
    successMessage: String? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Show dialog when registration is successful
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            showSuccessDialog = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo/Title Section - Simplified
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "👁️",
                fontSize = 64.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.create_account),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        // Register Form - Simplified
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {

            // Name Field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(id = R.string.full_name)) },
                placeholder = { Text(stringResource(id = R.string.name_hint)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = stringResource(id = R.string.full_name)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(id = R.string.email)) },
                placeholder = { Text(stringResource(id = R.string.email_hint)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = stringResource(id = R.string.email)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(id = R.string.password)) },
                placeholder = { Text(stringResource(id = R.string.password_hint)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = stringResource(id = R.string.password)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) stringResource(id = R.string.hide_password) else stringResource(id = R.string.show_password)
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password Field
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text(stringResource(id = R.string.confirm_password)) },
                placeholder = { Text(stringResource(id = R.string.confirm_password_hint)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = stringResource(id = R.string.confirm_password)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (confirmPasswordVisible) stringResource(id = R.string.hide_password) else stringResource(id = R.string.show_password)
                        )
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Error Message
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
            }


            // Register Button
            Button(
                onClick = { onRegisterClick(name, email, password, confirmPassword) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading && name.isNotBlank() && email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.register),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Login Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.already_have_account),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onLoginClick) {
                    Text(
                        text = stringResource(id = R.string.login),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Terms and Privacy - Simplified
        Text(
            text = stringResource(id = R.string.terms_privacy),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }

    // Registration Success Dialog
    if (showSuccessDialog) {
        RegistrationSuccessDialog(
            onDismiss = {
                showSuccessDialog = false
            },
            onLoginClick = {
                showSuccessDialog = false
                onClearRegistrationState()
                onClearRegistrationScreenState()
                onLoginClick()
            }
        )
    }
}

@Composable
fun RegistrationSuccessDialog(
    onDismiss: () -> Unit,
    onLoginClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Account Created Successfully!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Your account has been created successfully!",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "For security reasons, you need to login with your email and password to access the app.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onLoginClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Go to Login Page",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Stay Here",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun RegisterViewPreview() {
    RegisterView()
}


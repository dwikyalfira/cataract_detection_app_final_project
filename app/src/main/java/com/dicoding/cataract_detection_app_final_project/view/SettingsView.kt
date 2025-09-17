package com.dicoding.cataract_detection_app_final_project.view

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.dicoding.cataract_detection_app_final_project.R
import com.dicoding.cataract_detection_app_final_project.data.UserPreferences
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
    onBackClick: () -> Unit,
    userPreferences: UserPreferences,
    onLanguageChanged: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val themeMode by userPreferences.themeMode.collectAsState(initial = userPreferences.getThemeModeSync())
    val language by userPreferences.language.collectAsState(initial = userPreferences.getLanguageSync())

    val snackbarHostState = remember { SnackbarHostState() }

    fun showSnackbar(message: String) {
        scope.launch {
            snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(id = R.string.theme),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
            )
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    val themeOptions = listOf(
                        UserPreferences.THEME_LIGHT to stringResource(id = R.string.light_theme),
                        UserPreferences.THEME_DARK to stringResource(id = R.string.dark_theme),
                        UserPreferences.THEME_SYSTEM to stringResource(id = R.string.system_default)
                    )
                    themeOptions.forEachIndexed { index, (option, text) ->
                        SelectableRow(
                            text = text,
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
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(id = R.string.language),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
            )
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    val languageOptions = listOf(
                        UserPreferences.LANG_ENGLISH to stringResource(id = R.string.english),
                        UserPreferences.LANG_INDONESIAN to stringResource(id = R.string.indonesian)
                    )
                    languageOptions.forEachIndexed { index, (option, text) ->
                        SelectableRow(
                            text = text,
                            selected = language == option,
                            isLast = index == languageOptions.lastIndex,
                            onClick = {
                                if (language != option) {
                                    scope.launch {
                                        android.util.Log.d("SettingsView", "Setting language to: $option")
                                        userPreferences.setLanguage(option)
                                        
                                        // Apply locale changes
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
                                        
                                        // Trigger activity recreation
                                        onLanguageChanged()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectableRow(
    text: String,
    selected: Boolean,
    isLast: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            RadioButton(
                selected = selected,
                onClick = null, // Recommended for selectable parent
                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        if (!isLast) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        }
    }
}

@Preview
@Composable
fun SettingsViewPreview() {
    val context = LocalContext.current
    SettingsView(
        onBackClick = {},
        userPreferences = UserPreferences(context),
        onLanguageChanged = {}
    )
}
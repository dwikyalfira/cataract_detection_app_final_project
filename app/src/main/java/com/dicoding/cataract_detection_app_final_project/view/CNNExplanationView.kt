package com.dicoding.cataract_detection_app_final_project.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dicoding.cataract_detection_app_final_project.R
import com.dicoding.cataract_detection_app_final_project.view.components.FullScreenImageDialog

@Composable
private fun InfoCard(
    emoji: String,
    title: String,
    content: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    imageResId: Int? = null,
    onImageClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "$emoji $title",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = contentColor
            )
            
            if (imageResId != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = stringResource(R.string.illustration),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(androidx.compose.ui.graphics.Color.Black)
                        .clickable { onImageClick() },
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor.copy(alpha = 0.87f),
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CNNExplanationView(
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    var fullScreenImageRes by remember { mutableStateOf<Int?>(null) }

    if (fullScreenImageRes != null) {
        FullScreenImageDialog(
            imageResId = fullScreenImageRes!!,
            onDismissRequest = { fullScreenImageRes = null }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        }

        // CNN Overview Card
        InfoCard(
            emoji = "🧠",
            title = stringResource(id = R.string.cnn_overview),
            content = stringResource(id = R.string.cnn_overview_content),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            imageResId = R.drawable.img_cnn_architecture,
            onImageClick = { fullScreenImageRes = R.drawable.img_cnn_architecture }
        )

        // How CNN Works Card
        InfoCard(
            emoji = "⚙️",
            title = stringResource(id = R.string.cnn_how_it_works),
            content = stringResource(id = R.string.cnn_how_it_works_content),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )

        // CNN Layers Card
        InfoCard(
            emoji = "🏗️",
            title = stringResource(id = R.string.cnn_layers),
            content = stringResource(id = R.string.cnn_layers_content),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // CNN in Cataract Detection Card
        InfoCard(
            emoji = "🔍",
            title = stringResource(id = R.string.cnn_in_cataract_detection),
            content = stringResource(id = R.string.cnn_in_cataract_detection_content),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )

        // Advantages Card
        InfoCard(
            emoji = "✅",
            title = stringResource(id = R.string.cnn_advantages),
            content = stringResource(id = R.string.cnn_advantages_content),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )

        // Limitations Card
        InfoCard(
            emoji = "⚠️",
            title = stringResource(id = R.string.cnn_limitations),
            content = stringResource(id = R.string.cnn_limitations_content),
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )

        // Bottom spacing for gesture navigation
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun CNNExplanationViewPreview() {
    CNNExplanationView()
}


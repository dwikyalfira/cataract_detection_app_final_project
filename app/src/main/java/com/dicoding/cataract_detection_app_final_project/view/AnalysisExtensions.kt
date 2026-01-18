package com.dicoding.cataract_detection_app_final_project.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.dicoding.cataract_detection_app_final_project.R
import com.dicoding.cataract_detection_app_final_project.data.AnalysisHistory

@Composable
fun getLocalizedResult(predictionResult: String): String {
    return when {
        predictionResult.equals("Normal", ignoreCase = true) -> stringResource(R.string.result_normal)
        predictionResult.equals("Cataract", ignoreCase = true) -> stringResource(R.string.result_cataract)
        predictionResult.equals("Unknown", ignoreCase = true) -> stringResource(R.string.result_unknown)
        predictionResult.startsWith("Error", ignoreCase = true) -> stringResource(R.string.result_error)
        else -> predictionResult
    }
}

@Composable
fun getResultColor(predictionResult: String): Color {
    return when {
        predictionResult.equals("Normal", ignoreCase = true) -> Color(0xFF4CAF50) // Green
        predictionResult.equals("Cataract", ignoreCase = true) -> Color(0xFFF44336) // Red
        predictionResult.equals("Unknown", ignoreCase = true) -> Color.Gray // Gray
        else -> MaterialTheme.colorScheme.error
    }
}

@Composable
fun AnalysisHistory.getLocalizedResult(): String {
    return com.dicoding.cataract_detection_app_final_project.view.getLocalizedResult(this.predictionResult)
}

@Composable
fun AnalysisHistory.getResultColor(): Color {
    return com.dicoding.cataract_detection_app_final_project.view.getResultColor(this.predictionResult)
}

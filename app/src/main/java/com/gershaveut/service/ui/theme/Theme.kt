package com.gershaveut.service.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun ServiceTheme(
	content: @Composable () -> Unit,
) {
	MaterialTheme(
		content = content
	)
}
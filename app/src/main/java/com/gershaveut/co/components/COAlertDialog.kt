package com.gershaveut.co.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gershaveut.co.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun COAlertDialog(
	title: String,
	onDismissRequest: () -> Unit,
	onConfirmRequest: () -> Unit,
	content: @Composable () -> Unit,
	dismissText: String? = null,
	confirmText: String? = null
) {
	BasicAlertDialog(onDismissRequest) {
		Surface(shape = RoundedCornerShape(15.dp)) {
			Column {
				Text(
					title,
					fontSize = 20.sp,
					modifier = Modifier.padding(
						start = 24.dp,
						end = 24.dp,
						bottom = 16.dp,
						top = 24.dp
					)
				)
				Column(modifier = Modifier.padding(horizontal = 24.dp)) {
					content()
				}
				Row(modifier = Modifier
					.padding(24.dp)
					.align(Alignment.End)) {
					TextButton(
						{
						
						},
						modifier = Modifier.padding(end = 10.dp)
					) {
						Text(dismissText ?: stringResource(R.string.dialog_cancel))
					}
					TextButton({
						onConfirmRequest()
					}
					) {
						Text(confirmText ?: stringResource(R.string.dialog_confirm))
					}
				}
			}
		}
	}
}
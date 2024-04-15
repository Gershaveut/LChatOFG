package com.gershaveut.service.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LifecycleCoroutineScope
import com.gershaveut.service.R
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TextInputDialogFragment(private val required: String, private val onConfirm: (String) -> Unit) : DialogFragment() {
	
	@OptIn(DelicateCoroutinesApi::class)
	@SuppressLint("InflateParams")
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val view = layoutInflater.inflate(R.layout.dialog_text_input, null)
		
		val textInput: EditText = view.findViewById(R.id.editTextInput)
		textInput.hint = required
		
		return AlertDialog.Builder(requireActivity())
				.setTitle(R.string.text_input_title)
				.setView(view)
				.setNegativeButton(R.string.dialog_cancel, null)
				.setPositiveButton(R.string.dialog_confirm) { _, _ ->
					GlobalScope.launch {
						onConfirm(textInput.text.toString())
					}
				}
				.create()
	}
}
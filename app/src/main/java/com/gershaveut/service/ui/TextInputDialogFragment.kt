package com.gershaveut.service.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LifecycleCoroutineScope
import com.gershaveut.service.R
import com.gershaveut.service.chatOFG.Message
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.Serializable

class TextInputDialogFragment() : DialogFragment() {
	private lateinit var required: String
	private lateinit var onConfirmListener: OnConfirmListener
	
	private lateinit var textInput: EditText
	
	constructor(required: String, onConfirmListener: OnConfirmListener) : this() {
		this.required = required
		this.onConfirmListener = onConfirmListener
	}
	
	override fun onSaveInstanceState(outState: Bundle) {
		outState.putString("required", textInput.hint.toString())
		outState.putSerializable("onConfirmListener", onConfirmListener)
		
		super.onSaveInstanceState(outState)
	}
	
	@RequiresApi(Build.VERSION_CODES.TIRAMISU)
	@OptIn(DelicateCoroutinesApi::class)
	@SuppressLint("InflateParams")
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val view = layoutInflater.inflate(R.layout.dialog_text_input, null)
		
		textInput = view.findViewById(R.id.editTextInput)
		textInput.hint = savedInstanceState?.getString("required") ?: required
		
		return AlertDialog.Builder(requireActivity())
				.setTitle(R.string.text_input_title)
				.setView(view)
				.setNegativeButton(R.string.dialog_cancel, null)
				.setPositiveButton(R.string.dialog_confirm) { _, _ ->
					GlobalScope.launch {
						(savedInstanceState?.getSerializable("onConfirmListener", OnConfirmListener::class.java) ?: onConfirmListener).onConfirm(textInput.text.toString())
					}
				}
				.create()
	}
	
	interface OnConfirmListener : Serializable {
		fun onConfirm(text: String)
	}
}
package com.gershaveut.service.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.gershaveut.service.R
import com.gershaveut.service.databinding.FragmentCoBinding

class TextInputDialogFragment(private val required: String) : DialogFragment() {
	var text: String? = null
	
	private lateinit var textInput: EditText
	
	@SuppressLint("InflateParams")
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		return AlertDialog.Builder(requireActivity())
				.setTitle(R.string.text_input_title)
				.setView(layoutInflater.inflate(R.layout.dialog_text_input, null))
				.setNegativeButton(R.string.dialog_cancel, null)
				.setPositiveButton(R.string.dialog_confirm) { _, _ ->
					text = textInput.text.toString()
				}
				.create()
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		textInput = view.findViewById(R.id.editTextInput)
		textInput.hint = required
	}
}
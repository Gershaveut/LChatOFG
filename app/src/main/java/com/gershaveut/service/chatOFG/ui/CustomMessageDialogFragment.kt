package com.gershaveut.service.chatOFG.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.gershaveut.coapikt.Message
import com.gershaveut.coapikt.MessageType
import com.gershaveut.service.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CustomMessageDialogFragment : DialogFragment() {
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val view = layoutInflater.inflate(R.layout.dialog_custom_message, null)
		
		val layoutCustomMessageType = view.findViewById<LinearLayout>(R.id.layout_custom_message_type)
		val editCustomMessageText = view.findViewById<EditText>(R.id.edit_custom_message_text)
		val spinnerMessageType = view.findViewById<Spinner>(R.id.spinner_message_type)
		
		val checkCustomMessage = view.findViewById<CheckBox>(R.id.check_custom_message)
		val editCustomMessage = view.findViewById<EditText>(R.id.edit_custom_message)
		
		checkCustomMessage.setOnCheckedChangeListener { _, isChecked ->
			if (isChecked) {
				layoutCustomMessageType.visibility = View.GONE
				editCustomMessage.visibility = View.VISIBLE
			} else {
				layoutCustomMessageType.visibility = View.VISIBLE
				editCustomMessage.visibility = View.GONE
			}
		}
		
		spinnerMessageType.adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_dropdown_item, MessageType.entries.toTypedArray())
		
		return AlertDialog.Builder(requireActivity())
			.setTitle(R.string.co_custom_message)
			.setView(view)
			.setPositiveButton(R.string.co_send) { _, _ ->
				lifecycleScope.launch(Dispatchers.IO) {
					(parentFragmentManager.primaryNavigationFragment as COFragment).coClient!!.sendMessage(
						if (checkCustomMessage.isChecked) {
							Message.parseMessage(editCustomMessage.text.toString())
						} else {
							Message(editCustomMessageText.text.toString(), spinnerMessageType.selectedItem as MessageType)
						}.toString()
					)
				}
			}
			.setNegativeButton(R.string.dialog_cancel, null)
			.create()
	}
}
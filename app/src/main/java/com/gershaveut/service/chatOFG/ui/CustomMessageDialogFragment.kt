package com.gershaveut.service.chatOFG.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
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
		
		val spinnerMessageType = view.findViewById<Spinner>(R.id.spinner_message_type)
		
		spinnerMessageType.adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_dropdown_item, MessageType.entries.toTypedArray())
		
		return AlertDialog.Builder(requireActivity())
			.setTitle(R.string.co_custom_message)
			.setView(view)
			.setPositiveButton(R.string.co_send) { _, _ ->
				lifecycleScope.launch(Dispatchers.IO) {
					(parentFragmentManager.primaryNavigationFragment as COFragment).coClient.sendMessage(Message(view.findViewById<EditText>(R.id.edit_custom_message_text).text.toString(), spinnerMessageType.selectedItem as MessageType).toString())
				}
			}
			.setNegativeButton(R.string.dialog_cancel, null)
			.create()
	}
}
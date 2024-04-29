package com.gershaveut.service.ui

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

abstract class TextInputDialogFragment : DialogFragment(), TextInputDialog.OnConfirmListener {
	open val required: String? = null
	
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		return TextInputDialog(requireActivity(), required, this)
	}
}
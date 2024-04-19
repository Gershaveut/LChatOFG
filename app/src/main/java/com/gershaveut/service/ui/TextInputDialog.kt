package com.gershaveut.service.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.widget.EditText
import com.gershaveut.service.R

@SuppressLint("InflateParams")
class TextInputDialog : AlertDialog, DialogInterface.OnClickListener {
	private var onConfirmListener: OnConfirmListener?
	
	private var editTextInput: EditText
	
	constructor(context: Context) : this(context, null, null)
	
	constructor(context: Context, required: String?, onConfirmListener: OnConfirmListener?) : super(context) {
		this.onConfirmListener = onConfirmListener
		
		val view = layoutInflater.inflate(R.layout.dialog_text_input, null)
		
		setTitle(context.getString(R.string.text_input_title))
		setView(view)
		
		setButton(BUTTON_POSITIVE, context.getString(R.string.dialog_confirm), this)
		setButton(BUTTON_NEGATIVE, context.getString(R.string.dialog_cancel), this)
		
		editTextInput = view.findViewById(R.id.editTextInput)
		
		editTextInput.hint = required
	}
	
	override fun onClick(dialog: DialogInterface?, which: Int) {
		when (which) {
			BUTTON_POSITIVE -> onConfirmListener?.onConfirm(editTextInput.text.toString())
			BUTTON_NEGATIVE -> cancel()
		}
	}
	
	interface OnConfirmListener {
		fun onConfirm(reason: String)
	}
}
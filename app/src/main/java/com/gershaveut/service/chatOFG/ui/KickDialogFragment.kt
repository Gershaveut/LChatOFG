package com.gershaveut.service.chatOFG.ui

import android.app.Dialog
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.gershaveut.service.R
import com.gershaveut.service.ui.TextInputDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class KickDialogFragment() : TextInputDialogFragment() {
	override val required: String get() = getString(R.string.co_reason)
	
	private lateinit var userName: String
	
	constructor(userName: String) : this() {
		this.userName = userName
	}
	
	override fun onSaveInstanceState(outState: Bundle) {
		outState.putString("userName", userName)
		
		super.onSaveInstanceState(outState)
	}
	
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		if (savedInstanceState != null) {
			userName = savedInstanceState.getString("userName").toString()
		}
		
		return super.onCreateDialog(savedInstanceState)
	}
	
	override fun onConfirm(text: String) {
		lifecycleScope.launch(Dispatchers.IO) {
			(parentFragmentManager.primaryNavigationFragment!!.childFragmentManager.primaryNavigationFragment as COFragment).coClient!!.kick(userName, text)
		}
	}
}
package com.gershaveut.service.chatOFG.ui

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.gershaveut.service.R
import com.gershaveut.service.ui.TextInputDialogFragment
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class KickDialogFragment() : DialogFragment(), TextInputDialogFragment.OnConfirmListener {
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
		
		return TextInputDialogFragment(requireActivity(), getString(R.string.co_reason), this)
	}
	
	@OptIn(DelicateCoroutinesApi::class)
	override fun onConfirm(reason: String) {
		GlobalScope.launch {
			(parentFragmentManager.primaryNavigationFragment!!.childFragmentManager.primaryNavigationFragment as COFragment).coClient.kick(userName, reason)
		}
	}
}
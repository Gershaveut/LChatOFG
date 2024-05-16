package com.gershaveut.service.chatOFG.ui

import androidx.lifecycle.lifecycleScope
import com.gershaveut.service.R
import com.gershaveut.service.ui.TextInputDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BroadcastDialogFragment : TextInputDialogFragment() {
	override val required: String get() = getString(R.string.co_message)
	
	override fun onConfirm(text: String) {
		lifecycleScope.launch(Dispatchers.IO) {
			(parentFragmentManager.primaryNavigationFragment as COFragment).coClient!!.broadcast(text)
		}
	}
}

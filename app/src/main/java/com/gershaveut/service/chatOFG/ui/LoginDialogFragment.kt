package com.gershaveut.service.chatOFG.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.gershaveut.service.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.InetSocketAddress

class LoginDialogFragment : DialogFragment() {
	@SuppressLint("InflateParams")
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		super.onCreate(savedInstanceState)
		
		return AlertDialog.Builder(requireActivity())
			.setTitle(R.string.login_login)
			.setView(layoutInflater.inflate(R.layout.dialog_login, null))
			.setCancelable(false)
			.setNegativeButton(R.string.dialog_cancel) { dialog, _ ->
				dialog.cancel()
			}
			.setPositiveButton(R.string.co_connect, null)
			.create()
	}
	
	override fun onResume() {
		super.onResume()
		val dialog = dialog as AlertDialog?
		
		if (dialog != null) {
			val positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE) as Button
			
			val view = dialog.window!!.decorView
			
			val editHostname = view.findViewById<EditText>(R.id.editHostname)
			val editPort = view.findViewById<EditText>(R.id.editPort)
			val editName = view.findViewById<EditText>(R.id.editName)
			val checkNoName = view.findViewById<CheckBox>(R.id.check_no_name)
			
			val coFragment = parentFragmentManager.primaryNavigationFragment as COFragment
			
			checkNoName.setOnCheckedChangeListener { _, isChecked ->
				editName.isEnabled = !isChecked
			}
			
			positiveButton.setOnClickListener {
				fun snackbar(resId: Int) {
					Snackbar.make(view, resId, 1000).show()
				}
				
				val hostname = editHostname.text.toString()
				val port = editPort.text.toString()
				var name: String? = editName.text.toString()
				
				if (hostname.isNotEmpty() && port.isNotEmpty() && name?.isNotEmpty() != false) {
					if (checkNoName.isChecked)
						name = null
					
					lifecycleScope.launch(Dispatchers.IO) {
						coFragment.startCoService(requireContext())
						
						if (!coFragment.coClient!!.isConnecting) {
							if (coFragment.tryConnect(InetSocketAddress(hostname, port.toInt()), name)) {
								dialog.dismiss()
							} else
								snackbar(R.string.login_error_connect)
						} else
							snackbar(R.string.login_error_connecting)
					}
				} else {
					snackbar(R.string.login_error_fields)
				}
			}
		}
	}
}
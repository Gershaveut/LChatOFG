package com.gershaveut.service.chatOFG.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.gershaveut.service.R
import com.gershaveut.service.chatOFG.COClient
import com.gershaveut.service.coTag
import com.gershaveut.service.debug
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
			
			val editIpAddress = view.findViewById<EditText>(R.id.editIpAddress)
			val editPort = view.findViewById<EditText>(R.id.editPort)
			val editName = view.findViewById<EditText>(R.id.editName)
			
			val coFragment = parentFragmentManager.primaryNavigationFragment as COFragment
			
			positiveButton.setOnClickListener {
				fun snackbar(resId: Int) {
					Snackbar.make(view, resId, 1000).show()
				}
				
				val ipAddress = editIpAddress.text.toString()
				val port = editPort.text.toString()
				val name = editName.text.toString()
				
				if (ipAddress.isNotEmpty() && port.isNotEmpty() && name.isNotEmpty()) {
					coFragment.coClient.name = editName.text.toString()
					
					lifecycleScope.launch(Dispatchers.IO) {
						val ip = InetSocketAddress(ipAddress, port.toInt())
						
						if (!coFragment.coClient.isConnecting) {
							if (coFragment.coClient.tryConnect(ip)) {
								Log.i(coTag, "Connected to $ip")
								
								dialog.dismiss()
							} else
								snackbar(R.string.login_error_connect)
						} else
							snackbar(R.string.login_error_connecting)
					}
				} else {
					snackbar(R.string.login_error_fields)
					
					if (debug) {
						editIpAddress.append("10.0.0.192")
						editPort.append("7500")
						editName.append("User")
					}
				}
			}
		}
	}
}
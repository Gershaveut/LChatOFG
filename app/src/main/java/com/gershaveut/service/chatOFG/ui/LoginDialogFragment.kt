package com.gershaveut.service.chatOFG.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.nfc.Tag
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.gershaveut.service.R
import com.gershaveut.service.chatOFG.COClient
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.*
import java.net.InetSocketAddress

class LoginDialogFragment(onTextChange: (String) -> Unit) : DialogFragment() {
	private val coClient: COClient = COClient(onTextChange)
	
	@SuppressLint("InflateParams")
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val dialog = AlertDialog.Builder(activity)
			.setTitle(R.string.login_login)
			.setView(layoutInflater.inflate(R.layout.dialog_login, null))
			.setCancelable(false)
			.setNegativeButton(R.string.login_cancel) { dialog, _ ->
				dialog.cancel()
			}
			.setPositiveButton(R.string.login_connect, null)
			.create()
		
		dialog.setCanceledOnTouchOutside(false)
		
		return dialog
	}
	
	fun showAndGetCOClient(manager: FragmentManager, tag: String?): COClient {
		super.show(manager, tag)
		
		return coClient
	}
	
	@OptIn(DelicateCoroutinesApi::class)
	override fun onResume() {
		super.onResume()
		val dialog = dialog as AlertDialog?
		
		if (dialog != null) {
			val positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE) as Button
			val view = dialog.window!!.decorView
			
			val editIpAddress = view.findViewById<EditText>(R.id.editIpAddress)
			val editPort = view.findViewById<EditText>(R.id.editPort)
			val editName = view.findViewById<EditText>(R.id.editName)
			
			positiveButton.setOnClickListener {
				fun snackbar(resId: Int) {
					Snackbar.make(view, resId, 1000).show()
				}
				
				val ipAddress = editIpAddress.text.toString()
				val port = editPort.text.toString()
				val name = editName.text.toString()
				
				if (coClient.socket.isConnected)
					coClient.disconnected()
				
				if (ipAddress.isNotEmpty() && port.isNotEmpty() && name.isNotEmpty()) {
					coClient.name = editName.text.toString()
					
					positiveButton.isEnabled = false
					GlobalScope.launch {
						if (coClient.tryConnect(InetSocketAddress(ipAddress, port.toInt())))
							dialog.dismiss()
						else
							snackbar(R.string.login_error_connect)
						
						positiveButton.isEnabled = true
					}
				} else
					snackbar(R.string.login_error_fields)
			}
		}
	}
}
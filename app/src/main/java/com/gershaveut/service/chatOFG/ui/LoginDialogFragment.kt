package com.gershaveut.service.chatOFG.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.gershaveut.service.R
import com.gershaveut.service.chatOFG.COClient
import com.google.android.material.snackbar.Snackbar
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
				
				fun snackbar(text: String) {
					Snackbar.make(view, text, 1000).show()
				}
				
				val ipAddress = editIpAddress.text.toString()
				val port = editPort.text.toString()
				val name = editName.text.toString()
				
				if (coClient.socket.isConnected)
					coClient.disconnected()
				
				if (ipAddress.isNotEmpty() || port.isNotEmpty() || name.isNotEmpty()) {
					coClient.name = editName.text.toString()
					
					try {
						coClient.connect(InetSocketAddress(ipAddress, port.toInt()))
						
						dialog.dismiss()
					} catch (e: Exception) {
						snackbar(e.toString())
					}
				} else
					snackbar(R.string.login_error_fields)
			}
		}
	}
}
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
import com.gershaveut.service.chatOFG.TextSetter
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketAddress


class LoginDialogFragment(textSetter: TextSetter) : DialogFragment() {
	private val coClient: COClient = COClient(textSetter)
	
	@SuppressLint("InflateParams")
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val view = layoutInflater.inflate(R.layout.dialog_login, null)
		
		val editIpAddress = view.findViewById<EditText>(R.id.editIpAddress)
		val editPort = view.findViewById<EditText>(R.id.editPort)
		val editName = view.findViewById<EditText>(R.id.editName)
		
		val dialog = AlertDialog.Builder(activity)
			.setTitle(R.string.login_login)
			.setView(view)
			.setCancelable(false)
			.setNegativeButton(R.string.login_cancel) { dialog, _ ->
				dialog.cancel()
			}
			.setPositiveButton(R.string.login_connect) { dialog, which ->
				val ipAddress = editIpAddress.text.toString()
				val port: Int = editPort.text.toString().toInt()
				val name = editName.text.toString()
				
				coClient.name = name
				coClient.connect(InetSocketAddress.createUnresolved(ipAddress, port))
			}
			.create()
		
		dialog.setCanceledOnTouchOutside(false)
		
		return dialog
	}
	
	override fun onResume() {
		super.onResume()
		val dialog = dialog as AlertDialog?
		
		if (dialog != null) {
			val positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE) as Button
			
			positiveButton.setOnClickListener {
				val wantToCloseDialog = false
				
				if (wantToCloseDialog)
					dialog.dismiss()
			}
		}
	}
}
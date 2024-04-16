package com.gershaveut.service.chatOFG.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.findFragment
import com.gershaveut.service.R
import com.gershaveut.service.chatOFG.COClient
import com.gershaveut.service.coTag
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.InetSocketAddress

class LoginDialogFragment : DialogFragment() {
	private lateinit var coClient: COClient
	
	@SuppressLint("InflateParams")
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		super.onCreate(savedInstanceState);
		
		val dialog = AlertDialog.Builder(requireActivity())
			.setTitle(R.string.login_login)
			.setView(layoutInflater.inflate(R.layout.dialog_login, null))
			.setCancelable(false)
			.setNegativeButton(R.string.dialog_cancel) { dialog, _ ->
				dialog.cancel()
			}
			.setPositiveButton(R.string.co_connect, null)
			.create()
		
		dialog.setCanceledOnTouchOutside(false)
		
		return dialog
	}
	
	@OptIn(DelicateCoroutinesApi::class)
	override fun onCancel(dialog: DialogInterface) {
		super.onCancel(dialog)
		
		GlobalScope.launch {
			coClient.disconnect()
		}
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		coClient = view.findFragment<COFragment>().coClient
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
				
				if (ipAddress.isNotEmpty() && port.isNotEmpty() && name.isNotEmpty()) {
					coClient.name = editName.text.toString()
					
					positiveButton.isEnabled = false
					GlobalScope.launch {
						val ip = InetSocketAddress(ipAddress, port.toInt())
						
						if (coClient.tryConnect(ip)) {
							Log.i(coTag, "Connected to $ip")
							dialog.dismiss()
						} else
							snackbar(R.string.login_error_connect)
						
						requireActivity().runOnUiThread {
							positiveButton.isEnabled = true
						}
					}
				} else
					snackbar(R.string.login_error_fields)
			}
		}
	}
}
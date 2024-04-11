package com.gershaveut.service.chatOFG.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import com.gershaveut.service.R
import com.gershaveut.service.chatOFG.COClient
import com.gershaveut.service.coTag
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.InetSocketAddress

class LoginDialogFragment(private val coFragment: COFragment, onTextChange: (String) -> Unit) : DialogFragment() {
	private val coClient: COClient = COClient(onTextChange, { e ->
		Log.e(coTag, e.toString())
	} ) {
		Log.i(coTag, "Disconnected")
	}
	
	@SuppressLint("InflateParams")
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val dialog = AlertDialog.Builder(activity)
			.setTitle(R.string.login_login)
			.setView(layoutInflater.inflate(R.layout.dialog_login, null))
			.setCancelable(false)
			.setNegativeButton(R.string.login_cancel) { dialog, _ ->
				coFragment.findNavController().navigate("null")
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
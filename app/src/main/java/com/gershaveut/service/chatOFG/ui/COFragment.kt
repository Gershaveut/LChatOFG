package com.gershaveut.service.chatOFG.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.ViewSwitcher
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import com.gershaveut.service.R
import com.gershaveut.service.chatOFG.COClient
import com.gershaveut.service.chatOFG.MessageType
import com.gershaveut.service.coTag
import com.gershaveut.service.databinding.FragmentCoBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class COFragment : Fragment() {
	
	private var _binding: FragmentCoBinding? = null
	val binding get() = _binding!!
	
	private var coClient: COClient? = null
	
	@OptIn(DelicateCoroutinesApi::class)
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		_binding = FragmentCoBinding.inflate(inflater, container, false)
		val root: View = binding.root
		
		val coChat = binding.coContent.coChat
		
		val viewChat = coChat.viewChat
		val editMessage = coChat.editMessage
		val buttonSend = coChat.buttonSend
		val chatScrollView = coChat.chatScrollView
		val viewSwitcher = binding.viewSwitcher
		
		val loginDialog = LoginDialogFragment(this) { message ->
			Log.d(coTag, "receive_message: $message")
			
			requireActivity().runOnUiThread {
				when (message.messageType) {
					else -> viewChat.append("\n" + message.text)
				}
			}
		}
		
		buttonSend.setOnClickListener {
			chatScrollView.fullScroll(View.FOCUS_DOWN)
			
			GlobalScope.launch {
				val message = editMessage.text.toString()
				
				if (!coClient!!.trySendMessage(message))
					Snackbar.make(root, R.string.co_error_send, 1000).show()
				else
					Log.d(coTag, "send_message: $message")
				
				requireActivity().runOnUiThread {
					editMessage.text = null
				}
			}
		}
		
		binding.coContent.buttonDisconnect.setOnClickListener {
			disconnect()
			
			GlobalScope.launch {
				coClient!!.disconnect()
			}
		}
		
		binding.coMenu.buttonConnect.setOnClickListener {
			viewSwitcher.showNext()
			
			coClient = loginDialog.showAndGetCOClient(parentFragmentManager, null)
		}
		
		return root
	}
	
	fun disconnect() {
		binding.viewSwitcher.showPrevious()
		
		val coChat = binding.coContent.coChat
		
		coChat.viewChat.text = null
		coChat.editMessage.text = null
		binding.coContent.coContent.closeDrawers()
	}
	
	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}
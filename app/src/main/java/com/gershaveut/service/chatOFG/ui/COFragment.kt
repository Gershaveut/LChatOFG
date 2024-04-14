package com.gershaveut.service.chatOFG.ui

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gershaveut.service.R
import com.gershaveut.service.chatOFG.COClient
import com.gershaveut.service.chatOFG.Message
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
	
	val users: ArrayList<User> get() = (binding.coContent.recyclerUsers.adapter as UserAdapter).users
	//TODO: When you re-register, the screen changes
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
		
		fun snackbar(text: String) {
			Snackbar.make(root, text, 1000).show()
		}
		
		val loginDialog = LoginDialogFragment(this, COClient(object : COClient.COClientListener {
			override fun onMessage(message: Message) {
				Log.d(coTag, "receive_message: $message")
				
				val user = User(message.text.split(' ')[0])
				
				requireActivity().runOnUiThread {
					when (message.messageType) {
						MessageType.Error -> snackbar(message.text)
						MessageType.Join -> users.add(user) //TODO: Doesn't appear right away
						MessageType.Leave -> users.remove(user)
						else -> viewChat.append(if (viewChat.text.isEmpty()) message.text else "\n" + message.text)
					}
				}
			}
			
			override fun onException(exception: Exception) {
				Log.e(coTag, exception.toString())
			}
			
			override fun onDisconnected(cause: String?) {
				Log.i(coTag, "Disconnected")
				
				requireActivity().runOnUiThread {
					disconnect()
					
					if (cause != null)
						AlertDialog.Builder(activity)
							.setTitle(R.string.co_disconnected)
							.setMessage(cause)
							.create().show()
				}
			}
		}))
		
		buttonSend.setOnClickListener {
			chatScrollView.fullScroll(View.FOCUS_DOWN)
			
			GlobalScope.launch {
				val message = Message(editMessage.text.toString())
				
				if (message.text.isNotEmpty()) {
					if (coClient!!.trySendMessage(message))
						Log.d(coTag, "send_message: $message")
					else
						snackbar(requireActivity().getString(R.string.co_error_send))
				}
				
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
		users.clear()
	}
	
	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
	
	companion object {
		var coClient: COClient? = null
	}
}
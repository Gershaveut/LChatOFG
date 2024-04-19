package com.gershaveut.service.chatOFG.ui

import android.annotation.SuppressLint
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
import java.net.InetSocketAddress

class COFragment : Fragment(), COClient.Listener {
	private var _binding: FragmentCoBinding? = null
	val binding get() = _binding!!
	
	lateinit var coClient: COClient
	
	private fun snackbar(text: String) {
		Snackbar.make(binding.root, text, 1000).show()
	}
	
	@OptIn(DelicateCoroutinesApi::class)
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		super.onCreateView(inflater, container, savedInstanceState)
		
		_binding = FragmentCoBinding.inflate(inflater, container, false)
		
		val root: View = binding.root
		
		val coChat = binding.coContent.coChat
		
		val viewChat = coChat.viewChat
		val editMessage = coChat.editMessage
		val buttonSend = coChat.buttonSend
		val chatScrollView = coChat.chatScrollView
		val viewSwitcher = binding.viewSwitcher
		
		val loginDialog = LoginDialogFragment()
		
		val userAdapter = UserAdapter(requireActivity(), ArrayList())
		
		coClient = COClient(this)
		
		binding.coContent.recyclerUsers.adapter = userAdapter
		
		buttonSend.setOnClickListener {
			GlobalScope.launch {
				val message = Message(editMessage.text.toString())
				
				if (message.text.isNotEmpty()) {
					if (coClient.trySendMessage(message)) {
						Log.d(coTag, "send_message: $message")
						chatScrollView.fullScroll(View.FOCUS_DOWN)
					} else
						snackbar(requireActivity().getString(R.string.co_error_send))
				}
				
				requireActivity().runOnUiThread {
					editMessage.text = null
				}
			}
		}
		
		binding.coContent.buttonDisconnect.setOnClickListener {
			GlobalScope.launch {
				coClient.disconnect()
			}
		}
		
		binding.coMenu.buttonConnect.setOnClickListener {
			binding.coContent.coContent.closeDrawers()
			
			loginDialog.show(parentFragmentManager, null)
		}
		
		if (savedInstanceState != null) {
			viewChat.text = savedInstanceState.getCharSequence("viewChat")
			
			val coClientData = savedInstanceState.getStringArrayList("coClientData")
			
			if (coClientData != null) {
				coClient.name = coClientData[2]
				
				GlobalScope.launch {
					if (coClient.tryConnect(InetSocketAddress(coClientData[0], coClientData[1].toInt())))
						viewSwitcher.showNext()
				}
			}
		}
		
		return root
	}
	
	override fun onSaveInstanceState(outState: Bundle) {
		outState.putCharSequence("viewChat", binding.coContent.coChat.viewChat.text)
		
		outState.remove("coClientData")
		
		if (coClient.isConnected)
			outState.putStringArrayList("coClientData",
				arrayListOf(
					coClient.socket.inetAddress.toString().trim('/'),
					coClient.socket.port.toString(),
					coClient.name
				)
			)
		
		super.onSaveInstanceState(outState)
	}
	
	override fun onDestroyView() {
		super.onDestroyView()
		
		_binding = null
	}
	
	@OptIn(DelicateCoroutinesApi::class)
	override fun onDestroy() {
		super.onDestroy()
		
		GlobalScope.launch {
			if (coClient.isConnected)
				coClient.silentDisconnect()
		}
	}
	
	override fun onMessage(message: Message) {
		val userAdapter = binding.coContent.recyclerUsers.adapter as UserAdapter
		val coChat = binding.coContent.coChat
		
		Log.d(coTag, "receive_message: $message")
		
		val userName = message.text.split(' ')[0]
		
		requireActivity().runOnUiThread {
			val users = userAdapter.users
			
			when (message.messageType) {
				MessageType.Error -> snackbar(message.text)
				MessageType.Join -> {
					if (!users.equals(userName)) {
						users.add(userName)
						userAdapter.notifyItemInserted(users.size - 1)
					}
				}
				
				MessageType.Leave -> {
					if (!users.equals(userName)) {
						val index = users.indexOf(userName)
						
						users.removeAt(index)
						userAdapter.notifyItemRemoved(index)
					}
				}
				
				else -> {
					coChat.viewChat.append(if (coChat.viewChat.text.isEmpty()) message.text else "\n" + message.text)
					coChat.chatScrollView.fullScroll(View.FOCUS_DOWN)
				}
			}
			
			binding.coContent.recyclerUsers.refreshDrawableState()
		}
	}
	
	override fun onException(exception: Exception) {
		Log.e(coTag, exception.toString())
	}
	
	@SuppressLint("NotifyDataSetChanged")
	@OptIn(DelicateCoroutinesApi::class)
	override fun onDisconnected(reason: String?) {
		Log.i(coTag, "Disconnected")
		
		requireActivity().runOnUiThread {
			binding.viewSwitcher.showPrevious()
			
			val coChat = binding.coContent.coChat
			
			coChat.viewChat.text = null
			coChat.editMessage.text = null
			binding.coContent.coContent.closeDrawers()
			
			val userAdapter = binding.coContent.recyclerUsers.adapter as UserAdapter
			
			userAdapter.users.clear()
			userAdapter.notifyDataSetChanged()
			
			if (reason != null)
				AlertDialog.Builder(activity)
					.setTitle(R.string.co_disconnected)
					.setMessage(reason)
					.setPositiveButton(R.string.co_reconnect) { _, _ ->
						binding.viewSwitcher.showNext()
						
						GlobalScope.launch {
							coClient.reconnect()
						}
					}
					.create().show()
		}
	}
}
package com.gershaveut.service.chatOFG.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.gershaveut.coapikt.Message
import com.gershaveut.coapikt.MessageType
import com.gershaveut.service.R
import com.gershaveut.service.chatOFG.COClient
import com.gershaveut.service.coTag
import com.gershaveut.service.databinding.FragmentCoBinding
import com.gershaveut.service.service.COService
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.SocketAddress

class COFragment : Fragment(), COClient.Listener, ServiceConnection {
	private var _binding: FragmentCoBinding? = null
	private val binding get() = _binding!!
	
	private lateinit var coService: COService
	lateinit var coClient: COClient
	
	private fun snackbar(text: String) {
		Snackbar.make(binding.root, text, 1000).show()
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		super.onCreateView(inflater, container, savedInstanceState)
		
		_binding = FragmentCoBinding.inflate(inflater, container, false)
		
		val root: View = binding.root
		
		val coChat = binding.coContent.coChat
		
		val editMessage = coChat.editMessage
		val buttonSend = coChat.buttonSend
		val chatScrollView = coChat.chatScrollView
		
		binding.coContent.recyclerUsers.adapter = UserAdapter(requireActivity(), ArrayList())
		
		buttonSend.setOnClickListener {
			lifecycleScope.launch(Dispatchers.IO) {
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
			lifecycleScope.launch(Dispatchers.IO) {
				coClient.disconnect()
			}
		}
		
		binding.coMenu.buttonConnect.setOnClickListener {
			binding.coContent.coContent.closeDrawers()
			
			LoginDialogFragment().show(parentFragmentManager, null)
		}
		
		return root
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		
		if (savedInstanceState != null) {
			binding.coContent.coChat.viewChat.text = savedInstanceState.getCharSequence("viewChat")
		}
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		Intent(requireActivity(), COService::class.java).also {
			requireActivity().bindService(it, this, Context.BIND_AUTO_CREATE)
		}
	}
	
	override fun onSaveInstanceState(outState: Bundle) {
		outState.putCharSequence("viewChat", binding.coContent.coChat.viewChat.text)
		
		super.onSaveInstanceState(outState)
	}
	
	override fun onDestroyView() {
		super.onDestroyView()
		
		_binding = null
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
	
	override fun onException(exception: String) {
		Log.e(coTag, exception)
	}
	
	override fun onConnected(endpoint: SocketAddress) {
		requireActivity().runOnUiThread {
			binding.viewSwitcher.showNext()
		}
	}
	
	@SuppressLint("NotifyDataSetChanged")
	override fun onDisconnected(reason: String?) {
		Log.i(coTag, "Disconnected")
		
		activity?.runOnUiThread {
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
						lifecycleScope.launch(Dispatchers.IO) {
							coClient.reconnect()
						}
					}
					.create().show()
		}
	}
	
	override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
		coService = (service as COService.LocalBinder).getService()
		coClient = coService.coClient
		coClient.listener = this
		
		if (coClient.isConnected)
			binding.viewSwitcher.showNext()
	}
	
	override fun onServiceDisconnected(name: ComponentName?) {
	}
}
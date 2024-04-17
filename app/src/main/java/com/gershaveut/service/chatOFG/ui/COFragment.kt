package com.gershaveut.service.chatOFG.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.gershaveut.service.R
import com.gershaveut.service.chatOFG.COClient
import com.gershaveut.service.chatOFG.Message
import com.gershaveut.service.chatOFG.MessageType
import com.gershaveut.service.coTag
import com.gershaveut.service.databinding.CoChatBinding
import com.gershaveut.service.databinding.CoContentBinding
import com.gershaveut.service.databinding.FragmentCoBinding
import com.gershaveut.service.ui.TextInputDialogFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.Serializable
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketAddress

class COFragment : Fragment() {
	
	private var _binding: FragmentCoBinding? = null
	val binding get() = _binding!!
	
	lateinit var coClient: COClient
	
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
		
		fun snackbar(text: String) {
			Snackbar.make(root, text, 1000).show()
		}
		
		val loginDialog = LoginDialogFragment()
		
		val userAdapter = UserAdapter(requireActivity(), savedInstanceState?.getStringArrayList("users") ?: ArrayList())
		
		coClient = COClient(object : COClient.Listener {
			override fun onMessage(message: Message) {
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
							viewChat.append(if (viewChat.text.isEmpty()) message.text else "\n" + message.text)
							chatScrollView.fullScroll(View.FOCUS_DOWN)
						}
					}
					
					binding.coContent.recyclerUsers.refreshDrawableState()
				}
			}
			
			override fun onException(exception: Exception) {
				Log.e(coTag, exception.toString())
			}
			
			override fun onDisconnected(reason: String?) {
				Log.i(coTag, "Disconnected")
				
				requireActivity().runOnUiThread {
					disconnect()
					
					if (reason != null)
						AlertDialog.Builder(activity)
							.setTitle(R.string.co_disconnected)
							.setMessage(reason)
							.setPositiveButton(R.string.co_reconnect) { _, _ ->
								viewSwitcher.showNext()
								
								GlobalScope.launch {
									coClient.reconnect()
								}
							}
							.create().show()
				}
			}
		})
		
		userAdapter.coClient = coClient
		binding.coContent.recyclerUsers.adapter = userAdapter
		
		buttonSend.setOnClickListener {
			chatScrollView.fullScroll(View.FOCUS_DOWN)
			
			GlobalScope.launch {
				val message = Message(editMessage.text.toString())
				
				if (message.text.isNotEmpty()) {
					if (coClient.trySendMessage(message))
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
			GlobalScope.launch {
				coClient.disconnect()
			}
		}
		
		binding.coMenu.buttonConnect.setOnClickListener {
			viewSwitcher.showNext()
			
			loginDialog.show(parentFragmentManager, null)
		}
		
		if (savedInstanceState != null) {
			if (savedInstanceState.getBoolean("chatOpen"))
				viewSwitcher.showNext()
			
			viewChat.text = savedInstanceState.getCharSequence("viewChat")
			
			if (!coClient.socket.isConnected) {
				val coClientData = savedInstanceState.getStringArrayList("coClientData")
				
				coClient.name = coClientData!![1]
				
				GlobalScope.launch {
					coClient.connect(InetSocketAddress(InetAddress.getByName(coClientData[0])))
				}
			}
		}
		
		return root
	}
	
	override fun onSaveInstanceState(outState: Bundle) {
		outState.putBoolean("chatOpen", binding.viewSwitcher.currentView == binding.coContent.root)
		outState.putCharSequence("viewChat", binding.coContent.coChat.viewChat.text)
		outState.putStringArrayList("users", (binding.coContent.recyclerUsers.adapter as UserAdapter).users)
		
		outState.putStringArrayList("coClientData", arrayListOf(coClient.socket.remoteSocketAddress.toString(), coClient.name))
		
		super.onSaveInstanceState(outState)
	}
	
	@SuppressLint("NotifyDataSetChanged")
	fun disconnect() {
		binding.viewSwitcher.showPrevious()
		
		val coChat = binding.coContent.coChat
		
		coChat.viewChat.text = null
		coChat.editMessage.text = null
		binding.coContent.coContent.closeDrawers()
		
		val userAdapter: UserAdapter = binding.coContent.recyclerUsers.adapter as UserAdapter
		
		userAdapter.users.clear()
		userAdapter.notifyDataSetChanged()
	}
	
	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}
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

class COFragment : Fragment() {
	
	private var _binding: FragmentCoBinding? = null
	val binding get() = _binding!!
	
	private lateinit var activity: Activity
	
	private val userAdapter: UserAdapter get() = binding.coContent.recyclerUsers.adapter as UserAdapter
	//TODO: When you re-register, the screen changes
	@OptIn(DelicateCoroutinesApi::class)
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		super.onCreateView(inflater, container, savedInstanceState)
		
		activity = requireActivity()
		_binding = FragmentCoBinding.inflate(inflater, container, false)
		
		val root: View = binding.root
		
		val coChat = binding.coContent.coChat
		
		val viewChat = coChat.viewChat
		val editMessage = coChat.editMessage
		val buttonSend = coChat.buttonSend
		val chatScrollView = coChat.chatScrollView
		val viewSwitcher = binding.viewSwitcher
		
		if (savedInstanceState != null) {
			if (savedInstanceState.getBoolean("chatOpen"))
				viewSwitcher.showNext()
			
			viewChat.text = savedInstanceState.getCharSequence("viewChat")
		}
		
		fun snackbar(text: String) {
			Snackbar.make(root, text, 1000).show()
		}
		
		val loginDialog = LoginDialogFragment(this, COClient(object : COClient.COClientListener {
			override fun onMessage(message: Message) {
				Log.d(coTag, "receive_message: $message")
				
				val userName = message.text.split(' ')[0]
				
				activity.runOnUiThread {
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
				
				activity.runOnUiThread {
					disconnect()
					
					if (reason != null)
						AlertDialog.Builder(activity)
							.setTitle(R.string.co_disconnected)
							.setMessage(reason)
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
						snackbar(activity.getString(R.string.co_error_send))
				}
				
				activity.runOnUiThread {
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
	
	override fun onSaveInstanceState(outState: Bundle) {
		outState.putBoolean("chatOpen", binding.viewSwitcher.currentView == binding.coContent.root)
		outState.putCharSequence("viewChat", binding.coContent.coChat.viewChat.text)
		outState.putStringArrayList("users", (binding.coContent.recyclerUsers.adapter as UserAdapter).users)
		
		super.onSaveInstanceState(outState)
	}
	
	override fun onViewStateRestored(savedInstanceState: Bundle?) {
		if (savedInstanceState != null) {
			userAdapter.users = savedInstanceState.getStringArrayList("users")!!
		}
		
		super.onViewStateRestored(savedInstanceState)
	}
	
	@SuppressLint("NotifyDataSetChanged")
	fun disconnect() {
		binding.viewSwitcher.showPrevious()
		
		val coChat = binding.coContent.coChat
		
		coChat.viewChat.text = null
		coChat.editMessage.text = null
		binding.coContent.coContent.closeDrawers()
		userAdapter.users.clear()
		userAdapter.notifyDataSetChanged()
	}
	
	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
	
	companion object {
		var coClient: COClient? = null
	}
}
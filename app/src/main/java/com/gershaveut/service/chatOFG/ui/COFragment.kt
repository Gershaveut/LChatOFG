package com.gershaveut.service.chatOFG.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Debug
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
import com.gershaveut.service.chatOFG.Connection
import com.gershaveut.service.coTag
import com.gershaveut.service.databinding.FragmentCoBinding
import com.gershaveut.service.service.COService
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.SocketAddress

class COFragment : Fragment(), COClient.Listener, ServiceConnection {
	private var _binding: FragmentCoBinding? = null
	private val binding get() = _binding!!
	
	lateinit var coClient: COClient
	private lateinit var preferences: SharedPreferences
	private val connectionsType = object : TypeToken<ArrayList<Connection>>() {}.type
	
	
	private val root: View get() = binding.root
	
	private val coMenu get() = binding.coMenu
	private val coContent get() = binding.coContent
	private val coChat get() = coContent.coChat
	
	private val connectingBar get() = coMenu.connectingBar
	private val viewSwitcher get() = binding.viewSwitcher
	private val recyclerUsers get() = coContent.recyclerUsers
	private val recyclerConnections get() = coMenu.recyclerConnections
	
	private val userAdapter: UserAdapter get() = recyclerUsers.adapter as UserAdapter
	private val connectionAdapter: ConnectionAdapter get() = recyclerConnections.adapter as ConnectionAdapter
	
	private val editMessage get() = coChat.editMessage
	private val viewChat get() = coChat.viewChat
	private val buttonSend get() = coChat.buttonSend
	private val chatScrollView get() = coChat.chatScrollView
	
	fun snackbar(text: String) {
		Snackbar.make(binding.root, text, 1000).show()
	}
	
	fun snackbar(resId: Int) {
		Snackbar.make(binding.root, resId, 1000).show()
	}
	
	override fun onAttach(context: Context) {
		super.onAttach(context)
		
		preferences = context.getSharedPreferences(coTag, Context.MODE_PRIVATE)
		
		Intent(requireActivity(), COService::class.java).also {
			context.bindService(it, this, Context.BIND_AUTO_CREATE)
		}
	}
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		super.onCreateView(inflater, container, savedInstanceState)
		
		_binding = FragmentCoBinding.inflate(inflater, container, false)
		
		recyclerUsers.adapter = UserAdapter(requireActivity(), ArrayList())
		recyclerConnections.adapter = ConnectionAdapter(requireActivity(), ArrayList(), this)
		
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
		
		coMenu.buttonConnect.setOnClickListener {
			coContent.coContent.closeDrawers()
			
			LoginDialogFragment().show(parentFragmentManager, null)
		}
		
		coContent.buttonDisconnect.setOnClickListener {
			lifecycleScope.launch(Dispatchers.IO) {
				coClient.disconnect()
			}
		}
		
		coContent.buttonBroadcast.setOnClickListener {
			BroadcastDialogFragment().show(parentFragmentManager, null)
		}
		
		coContent.buttonCustomMessage.setOnClickListener {
			CustomMessageDialogFragment().show(parentFragmentManager, null)
		}
		
		connectionAdapter.connections = Gson().fromJson(preferences.getString("connections", ""), connectionsType)
		
		if (savedInstanceState != null) {
			viewChat.text = savedInstanceState.getCharSequence("viewChat")
			userAdapter.users = savedInstanceState.getStringArrayList("recyclerUsers")!!
		}
		
		if (Debug.isDebuggerConnected()) {
			connectionAdapter.registerConnection(Connection("192.168.1.82", 7500, "User"))
			connectionAdapter.registerConnection(Connection("192.168.1.120", 7500, "User"))
			connectionAdapter.registerConnection(Connection("10.0.0.193", 7500, "User"))
		}
		
		return root
	}
	
	override fun onSaveInstanceState(outState: Bundle) {
		outState.putCharSequence("viewChat", viewChat.text)
		outState.putStringArrayList("recyclerUsers", userAdapter.users)
		
		Log.i(coTag, Gson().toJson(connectionAdapter.connections, connectionsType))
		
		preferences.edit()
			.putString("connections", Gson().toJson(connectionAdapter.connections, connectionsType))
			.apply()
		
		super.onSaveInstanceState(outState)
	}
	
	override fun onDestroyView() {
		super.onDestroyView()
		
		_binding = null
	}
	
	suspend fun tryConnect(endpoint: SocketAddress) : Boolean {
		requireActivity().runOnUiThread {
			connectingBar.visibility = View.VISIBLE
		}
		
		val result = coClient.tryConnect(endpoint)
		
		requireActivity().runOnUiThread {
			connectingBar.visibility = View.INVISIBLE
		}
		
		return result
	}
	
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
						
						if (index != -1) {
							userAdapter.notifyItemRemoved(index)
							users.removeAt(index)
						}
					}
				}
				MessageType.Broadcast -> {
					AlertDialog.Builder(activity)
						.setTitle(R.string.co_broadcast)
						.setMessage(message.text)
						.create().show()
				}
				else -> {
					viewChat.append(if (viewChat.text.isEmpty()) message.text else "\n" + message.text)
					chatScrollView.fullScroll(View.FOCUS_DOWN)
				}
			}
			
			recyclerUsers.refreshDrawableState()
		}
	}
	
	override fun onException(exception: String) {
		Log.e(coTag, exception)
	}
	
	override fun onConnected(endpoint: SocketAddress) {
		Log.i(coTag, "Connected to $endpoint")
		
		requireActivity().runOnUiThread {
			viewSwitcher.showNext()
			
			val splitSocketAddress = coClient.lastConnect.toString().split(':')
			
			connectionAdapter.registerConnection(Connection(splitSocketAddress[0].replace("/", ""), splitSocketAddress[1].toInt(), coClient.name!!))
		}
	}
	
	@SuppressLint("NotifyDataSetChanged")
	override fun onDisconnected(reason: String?) {
		Log.i(coTag, "Disconnected")
		
		activity?.runOnUiThread {
			viewSwitcher.showPrevious()
			
			viewChat.text = null
			editMessage.text = null
			coContent.coContent.closeDrawers()
			
			userAdapter.users.clear()
			userAdapter.notifyDataSetChanged()
			
			if (reason != null) {
				val reconnectDialog = AlertDialog.Builder(activity)
				
				reconnectDialog
					.setTitle(R.string.co_disconnected)
					.setMessage(reason)
					.setPositiveButton(R.string.co_reconnect) { _, _ ->
						lifecycleScope.launch(Dispatchers.IO) {
							if (!coClient.tryReconnect())
								requireActivity().runOnUiThread {
									reconnectDialog.show()
								}
						}
					}
					.create().show()
			}
		}
	}
	
	override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
		val coService = (service as COService.LocalBinder).getService()
		
		coClient = coService.coClient
		coClient.listener = this
		
		if (coClient.isConnected)
			viewSwitcher.showNext()
	}
	
	override fun onServiceDisconnected(name: ComponentName?) {
	}
}
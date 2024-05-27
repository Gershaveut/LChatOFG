package com.gershaveut.service.chatOFG.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.gershaveut.coapikt.Message
import com.gershaveut.service.MainActivity
import com.gershaveut.service.R
import com.gershaveut.service.chatOFG.COClient
import com.gershaveut.service.chatOFG.Connection
import com.gershaveut.service.coTag
import com.gershaveut.service.databinding.FragmentCoBinding
import com.gershaveut.service.service.COService
import com.google.android.material.snackbar.Snackbar
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.InetSocketAddress
import java.net.SocketAddress

class COFragment : Fragment(), COClient.Listener, ServiceConnection {
	private var _binding: FragmentCoBinding? = null
	private val binding get() = _binding!!
	
	lateinit var coClient: COClient
	
	private lateinit var preferences: SharedPreferences
	private val connectionsType = object : TypeToken<ArrayList<Connection>>() {}.type
	private var broadcastDialog: AlertDialog? = null
	
	var connections = listOf(Connection("test1", 932, "Test"), Connection("test2", 567, null))
	var users = listOf("User1", "User2")
	
	/*
	private val root: View get() = binding.root
	
	private val coMenu get() = binding.coMenu
	private val coContent get() = binding.coContent
	private val coChat get() = coContent.coChat
	
	private val connectingBar get() = coMenu.connectingBar
	private val viewSwitcher get() = binding.viewSwitcher
	
	private val menuSideSheet get() = coContent.menuSideSheet
	private val recyclerUsers get() = coContent.recyclerUsers
	private val recyclerConnections get() = coMenu.recyclerConnections
	
	private val userAdapter: UserAdapter get() = recyclerUsers.adapter as UserAdapter
	private val connectionAdapter: ConnectionAdapter get() = recyclerConnections.adapter as ConnectionAdapter
	
	private val editMessage get() = coChat.editMessage
	private val viewChat get() = coChat.viewChat
	private val buttonSend get() = coChat.buttonSend
	private val chatScrollView get() = coChat.chatScrollView
	*/
	
	@Composable
	fun RightModalNavigationDrawer(
		modifier: Modifier = Modifier,
		drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
		gesturesEnabled: Boolean = true,
		scrimColor: Color = DrawerDefaults.scrimColor,
		drawerContent: @Composable ColumnScope.() -> Unit,
		content: @Composable () -> Unit,
	) {
		CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
			ModalNavigationDrawer(
				modifier = modifier,
				drawerState = drawerState,
				gesturesEnabled = gesturesEnabled,
				scrimColor = scrimColor,
				drawerContent = {
					CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
						Column(
							content = drawerContent
						)
					}
				},
				content = {
					CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
						content()
					}
				},
			)
			
		}
	}
	
	@OptIn(ExperimentalLayoutApi::class)
	@Preview(device = "spec:width=411dp,height=891dp")
	@Composable
	fun Menu() {
		Surface(modifier = Modifier.fillMaxSize()) {
			Column(
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				LinearProgressIndicator(
					modifier = Modifier
						.padding(bottom = 20.dp)
						.fillMaxWidth()
				)
				Text(
					stringResource(R.string.co_no_connection),
					textAlign = TextAlign.Center,
					modifier = Modifier.padding(bottom = 20.dp)
				)
				Button(
					{
						//coContent.coContent.closeDrawers()
						
						LoginDialogFragment().show(parentFragmentManager, null)
					}, modifier = Modifier.padding(bottom = 16.dp)
				) {
					Text(stringResource(R.string.co_connect), fontSize = 18.sp)
				}
				
				HorizontalDivider()
				
				Text(
					stringResource(R.string.co_history), modifier = Modifier.padding(top = 16.dp)
				)
				LazyColumn(
					modifier = Modifier.padding(top = 16.dp)
				) {
					items(connections) { connection ->
						FlowColumn(
							modifier = Modifier
								.fillMaxWidth()
								.height(35.dp)
						) {
							Text(connection.userName ?: "null", modifier = Modifier.weight(1f))
							Text(
								"${connection.hostname}:${connection.port}",
								fontSize = 12.sp,
								color = Color.Gray,
								modifier = Modifier.weight(1f)
							)
							
							IconButton(
								{
									val popupMenu = PopupMenu(context, requireView())
									
									popupMenu.menu.add(
										0,
										ConnectionAdapter.MenuID.Connect.ordinal,
										Menu.NONE,
										R.string.co_connect
									)
									popupMenu.menu.add(
										0,
										ConnectionAdapter.MenuID.Remove.ordinal,
										Menu.NONE,
										R.string.co_remove
									)
									
									popupMenu.setOnMenuItemClickListener {
										when (it.itemId) {
											ConnectionAdapter.MenuID.Connect.ordinal -> (context as MainActivity).lifecycleScope.launch(
												Dispatchers.IO
											) {
												if (!tryConnect(
														InetSocketAddress(
															connection.hostname, connection.port
														), connection.userName
													)
												) snackbar(R.string.login_error_connect)
											}
											
											ConnectionAdapter.MenuID.Remove.ordinal -> {
												val index = connections.indexOf(connection)
												
												//connections.removeAt(index)
											}
										}
										
										return@setOnMenuItemClickListener true
									}
									
									popupMenu.show()
								},
								modifier = Modifier.size(30.dp),
							) {
								Icon(Icons.Outlined.MoreVert, null)
							}
						}
					}
				}
			}
		}
	}
	
	@OptIn(ExperimentalLayoutApi::class)
	@Preview(device = "spec:width=411dp,height=891dp")
	@Composable
	fun Chat() {
		Surface(modifier = Modifier.fillMaxSize()) {
			RightModalNavigationDrawer(
				drawerContent = {
					ModalDrawerSheet(modifier = Modifier.width(250.dp)) {
						Column(horizontalAlignment = Alignment.CenterHorizontally) {
							Button(
								{}, modifier = Modifier.fillMaxWidth()
							) {
								Text(stringResource(R.string.co_disconnect))
							}
							Button(
								{}, modifier = Modifier.fillMaxWidth()
							) {
								Text(stringResource(R.string.co_broadcast_send))
							}
							Button(
								{}, modifier = Modifier.fillMaxWidth()
							) {
								Text(stringResource(R.string.co_custom_message))
							}
							
							Text(stringResource(R.string.co_users) + ":", textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 15.dp))
							
							LazyColumn {
								items(users) { user ->
									FlowRow {
										Text(user, modifier = Modifier.weight(1f))
										IconButton( {
											
											},
											modifier = Modifier.size(30.dp)
										) {
											Icon(Icons.Outlined.MoreVert, null)
										}
									}
								}
							}
						}
					}
				}) {
				FlowRow {
					Text(
						"", modifier = Modifier
							.fillMaxWidth()
							.height(700.dp)
					)
					TextField(stringResource(R.string.co_message), {})
					IconButton( {
					
					}
					) {
						Icon(Icons.AutoMirrored.Outlined.Send, null)
					}
				}
			}
		}
	}
	
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
			context.startService(it)
			context.bindService(it, this, Context.BIND_AUTO_CREATE)
		}
	}
	
	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
	): View {
		super.onCreateView(inflater, container, savedInstanceState)
		
		//_binding = FragmentCoBinding.inflate(inflater, container, false)
		
		val root = inflater.inflate(R.layout.co_content, container, false)
		
		root.findViewById<ComposeView>(R.id.co_drawer).apply {
			setContent {
				Menu()
			}
		}
		
		/*
		recyclerUsers.adapter = UserAdapter(requireActivity(), ArrayList())
		recyclerConnections.adapter = ConnectionAdapter(requireActivity(), ArrayList(), this)
		
		buttonSend.setOnClickListener {
			lifecycleScope.launch(Dispatchers.IO) {
				val text = editMessage.text.toString()
				
				if (text.isNotEmpty()) {
					if (coClient.trySendMessage(text)) {
						Log.d(coTag, "send_message: $text")
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
		*/
		
		return root
	}
	
	override fun onSaveInstanceState(outState: Bundle) {        /*
		outState.putCharSequence("viewChat", viewChat.text)
		outState.putStringArrayList("recyclerUsers", userAdapter.users)
		
		preferences.edit()
			.putString("connections", Gson().toJson(connectionAdapter.connections, connectionsType))
			.apply()
		*/
		
		super.onSaveInstanceState(outState)
	}
	
	override fun onDestroyView() {
		super.onDestroyView()
		
		_binding = null
	}
	
	suspend fun tryConnect(endpoint: SocketAddress, userName: String?): Boolean {
		requireActivity().runOnUiThread {
			//connectingBar.visibility = View.VISIBLE
		}
		
		coClient.name = userName
		val result = coClient.tryConnect(endpoint)
		
		requireActivity().runOnUiThread {
			//connectingBar.visibility = View.INVISIBLE
		}
		
		return result
	}
	
	override fun onMessage(message: Message) {
		Log.d(coTag, "receive_message: $message")
		
		requireActivity().runOnUiThread {
			/*
			val users = userAdapter.users
			
			when (message.messageType) {
				MessageType.Error -> snackbar(message.text)
				MessageType.Join -> {
					if (!users.contains(message.text)) {
						users.add(message.text)
						userAdapter.notifyItemInserted(users.size - 1)
					}
				}
				MessageType.Leave -> {
					if (users.contains(message.text)) {
						val index = users.indexOf(message.text)
						
						if (index != -1) {
							userAdapter.notifyItemRemoved(index)
							users.removeAt(index)
						}
					}
				}
				MessageType.Broadcast -> {
					if (broadcastDialog?.isShowing != true) {
						broadcastDialog = AlertDialog.Builder(activity)
							.setTitle(R.string.co_broadcast)
							.setMessage(message.text)
							.create()
						
						broadcastDialog!!.show()
					}
				}
				else -> {
					viewChat.append(SpannableStringBuilder().color(message.color ?: android.graphics.Color.BLACK) { append(if (viewChat.text.isEmpty()) message.text else "\n" + message.text) })
					
					chatScrollView.fullScroll(View.FOCUS_DOWN)
				}
			}
			
			recyclerUsers.refreshDrawableState()
			*/
		}
	}
	
	override fun onException(exception: String) {
		Log.e(coTag, exception)
	}
	
	override fun onConnected(endpoint: SocketAddress) {
		Log.i(coTag, "Connected to $endpoint")
		
		/*
		requireActivity().runOnUiThread {
			viewSwitcher.showNext()
			
			val splitSocketAddress = coClient.lastConnect.toString().split(':')
			val hostname = splitSocketAddress[0]
			
			connectionAdapter.registerConnection(Connection(hostname.split("/")[if (hostname.startsWith("/")) 1 else 0], splitSocketAddress[1].toInt(), coClient.name))
		}
		*/
	}
	
	@SuppressLint("NotifyDataSetChanged")
	override fun onDisconnected(reason: String?) {
		Log.i(coTag, "Disconnected")
		
		/*
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
		*/
	}
	
	override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
		val coService = (service as COService.LocalBinder).getService()
		
		coClient = coService.coClient
		coClient.listener = this
		
		//if (coClient.isConnected)
		//viewSwitcher.showNext()
	}
	
	override fun onServiceDisconnected(name: ComponentName?) {
	}
}
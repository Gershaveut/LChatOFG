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
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
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
	
	@OptIn(ExperimentalMaterial3Api::class)
	@Composable
	fun ServiceAlertDialog(title: String, onDismissRequest: () -> Unit, onConfirmRequest: () -> Unit, content: @Composable () -> Unit, dismissText: String? = null, confirmText: String? = null) {
		BasicAlertDialog(onDismissRequest) {
			Surface( shape = RoundedCornerShape(15.dp) ) {
				Column {
					Text(title, fontSize = 20.sp, modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 16.dp, top = 24.dp))
					Column( modifier = Modifier.padding(horizontal = 24.dp) ) {
						content()
					}
					Row(modifier = Modifier.padding(24.dp).align(Alignment.End)) {
						TextButton(
							{
							
							},
							modifier = Modifier.padding(end = 10.dp)
						) {
							Text(dismissText ?: stringResource(R.string.dialog_cancel))
						}
						TextButton({
							onConfirmRequest()
						}
						) {
							Text(confirmText ?: stringResource(R.string.dialog_confirm))
						}
					}
				}
			}
		}
	}
	
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
				val openLoginDialog = remember { mutableStateOf(false) }
				
				Button( {
						openLoginDialog.value = true
					}, modifier = Modifier.padding(bottom = 16.dp)
				) {
					Text(stringResource(R.string.co_connect), fontSize = 18.sp)
				}
				
				if (openLoginDialog.value) {
					ServiceAlertDialog(
						stringResource(R.string.login_login),
						confirmText = stringResource(R.string.login_login),
						onDismissRequest = {},
						onConfirmRequest = {},
						content = {
							val hostname = remember{mutableStateOf("")}
							val port = remember{mutableStateOf("")}
							val name = remember{mutableStateOf("")}
							
							TextField(
								hostname.value, { text ->
									hostname.value = text
								},
								placeholder = { Text(stringResource(R.string.login_hostname)) }
							)
							TextField(
								port.value, { text ->
									port.value = text
								},
								placeholder = { Text(stringResource(R.string.login_port)) }
							)
							TextField(
								name.value, { text ->
									name.value = text
								},
								placeholder = { Text(stringResource(R.string.login_name)) }
							)
						}
					)
				}
				
				HorizontalDivider()
				
				Text(
					stringResource(R.string.co_history), modifier = Modifier.padding(top = 16.dp)
				)
				LazyColumn(
					modifier = Modifier.padding(top = 16.dp)
				) {
					items(connections) { connection ->
						Box(
							modifier = Modifier.size(height = 35.dp, width = 350.dp)
						) {
							Column(modifier = Modifier.align(Alignment.CenterStart)) {
								Text(connection.userName ?: "null")
								Text(
									"${connection.hostname}:${connection.port}",
									fontSize = 12.sp,
									color = Color.Gray
								)
							}
							
							IconButton(
								{
									val popupMenu = PopupMenu(context, requireView())
									
									popupMenu.menu.add(
										0,
										MenuID.Connect.ordinal,
										Menu.NONE,
										R.string.co_connect
									)
									popupMenu.menu.add(
										0,
										MenuID.Remove.ordinal,
										Menu.NONE,
										R.string.co_remove
									)
									
									popupMenu.setOnMenuItemClickListener {
										when (it.itemId) {
											MenuID.Connect.ordinal -> (context as MainActivity).lifecycleScope.launch(
												Dispatchers.IO
											) {
												if (!tryConnect(
														InetSocketAddress(
															connection.hostname, connection.port
														), connection.userName
													)
												) snackbar(R.string.login_error_connect)
											}
											
											MenuID.Remove.ordinal -> {
												val index = connections.indexOf(connection)
												
												//connections.removeAt(index)
											}
										}
										
										return@setOnMenuItemClickListener true
									}
									
									popupMenu.show()
								},
								modifier = Modifier
									.size(30.dp)
									.align(Alignment.CenterEnd)
							) {
								Icon(Icons.Outlined.MoreVert, null)
							}
						}
					}
				}
			}
		}
	}
	
	@Preview(device = "spec:width=411dp,height=891dp")
	@Composable
	fun Chat() {
		Surface(modifier = Modifier.fillMaxSize()) {
			RightModalNavigationDrawer(
				drawerContent = {
					ModalDrawerSheet(modifier = Modifier.width(250.dp)) {
						Column(horizontalAlignment = Alignment.CenterHorizontally) {
							Button(
								{}, modifier = Modifier
									.fillMaxWidth()
									.padding(horizontal = 5.dp)
							) {
								Text(stringResource(R.string.co_disconnect))
							}
							Button(
								{}, modifier = Modifier
									.fillMaxWidth()
									.padding(horizontal = 5.dp)
							) {
								Text(stringResource(R.string.co_broadcast_send))
							}
							Button(
								{}, modifier = Modifier
									.fillMaxWidth()
									.padding(horizontal = 5.dp)
							) {
								Text(stringResource(R.string.co_custom_message))
							}
							
							Text(stringResource(R.string.co_users) + ":", textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 15.dp))
							
							LazyColumn {
								items(users) { user ->
									Box( modifier = Modifier
										.padding(horizontal = 5.dp)
										.fillMaxWidth() ) {
										Text(user, modifier = Modifier.align(Alignment.CenterStart))
										IconButton( {
											
											},
											modifier = Modifier
												.size(30.dp)
												.align(Alignment.CenterEnd)
										) {
											Icon(Icons.Outlined.MoreVert, null)
										}
									}
								}
							}
						}
					}
				}) {
				//Chat content
				
				Column {
					LazyColumn( modifier = Modifier.weight(15f) ) {
						items(listOf(
							ChatMessage("Ты поедешь в Пирогово, будешь меня бранить Сереже?", false),
							ChatMessage("Я ни с кем не говорил, ни с Таней, дочерью.", true),
							ChatMessage("Но с Таней, сестрой, говорил?", false),
							ChatMessage("Да.", true),
							ChatMessage("Что же она говорила?", false),
							ChatMessage("То же, что тебе… мне тебя защищала, тебе, вероятно, за меня говорила.", true),
							ChatMessage("Да, она ужасно строга была ко мне. Слишком строга. Я не заслуживаю.", false),
							ChatMessage("Пожалуйста, не будем говорить, уляжется, успокоится и, бог даст, уничтожится.", true),
							ChatMessage("Не могу я не говорить. Мне слишком тяжело жить под вечным страхом. Теперь, если он заедет, начнется опять. Он не говорил ничего, но, может быть, заедет.", false),
							ChatMessage("Только что надеялся успокоиться, как опять ты будто приготавливаешь меня к неприятному ожиданию.", true),
							ChatMessage("Что же мне делать? Это может быть, он сказал Тане. Я не звала. Может быть, он заедет.", false),
							ChatMessage("Заедет он или не заедет, неважно, даже твоя поездка не важна, важно, как я говорил тебе, два года назад говорил тебе, твое отношение к твоему чувству. Если бы ты признавала свое чувство нехорошим, ты бы не стала даже и вспоминать о том, заедет ли он, и говорить о нем.", true),
							ChatMessage("Ну, как же быть мне теперь?", false),
							ChatMessage("Покаяться в душе в своем чувстве.", true),
							ChatMessage("Не умею каяться и не понимаю, что это значит.", false),
							ChatMessage("Это значит обсудить самой с собой, хорошо ли то чувство, которое ты испытываешь к этому человеку, или дурное.", true),
							ChatMessage("Я никакого чувства не испытываю, ни хорошего, ни дурного.", false),
							ChatMessage("Это неправда.", true),
							ChatMessage("Чувство это так неважно, ничтожно.", false),
							ChatMessage("Все чувства, а потому и самое ничтожное, всегда или хорошие, или дурные в наших глазах, и потому и тебе надо решить, хорошее ли это было чувство, или дурное.", true),
							ChatMessage("Нечего решать, это чувство такое неважное, что оно не может быть дурным. Да и нет в нем ничего дурного.", false),
							ChatMessage("Нет, исключительное чувство старой замужней женщины к постороннему мужчине — дурное чувство.", true),
							ChatMessage("У меня нет чувства к мужчине, есть чувство к человеку.", false),
							ChatMessage("Да ведь человек этот мужчина.", true),
							ChatMessage("Он для меня не мужчина. Нет никакого чувства исключительного, а есть то, что после моего горя мне было утешение музыка, а к человеку нет никакого особенного чувства.", false),
							ChatMessage("Зачем говорить неправду?", true),
							ChatMessage("Но хорошо. Это было. Я сделала дурно, что заехала, что огорчила тебя. Но теперь это кончено, я сделаю все, чтобы не огорчать тебя.", false),
							ChatMessage("Ты не можешь этого сделать потому, что все дело не в том, что ты сделаешь — заедешь, примешь, не примешь, дело все в твоем отношении к твоему чувству. Ты должна решить сама с собой, хорошее ли это, или дурное чувство.", true),
							ChatMessage("Да нет никакого.", false),
							ChatMessage("Это неправда. И вот это-то и дурно для тебя, что ты хочешь скрыть это чувство, чтобы удержать его. А до тех пор, пока ты не решишь, хорошее это чувство или дурное, и не признаешь, что оно дурное, ты будешь не в состоянии не делать мне больно. Если ты признаешь, как ты признаешь теперь, что чувство это хорошее, то никогда не будешь в силах не желать удовлетворения этого чувства, то есть видеться, а желая, ты невольно будешь делать то, чтобы видеться. Если ты будешь избегать случаев видеться, то тебе будет тоска, тяжело. Стало быть, все дело в том, чтобы решить, какое это чувство, дурное или хорошее.", true)
						)) { chatMessage ->
							@Composable
							fun messageContent(message: ChatMessage) {
								Column( horizontalAlignment = Alignment.CenterHorizontally ) {
									if (message.owner != null) {
										Text(message.owner!!, color = Color(41, 150, 201), fontSize = 15.sp, modifier = Modifier
											.padding(top = 10.dp, start = 10.dp, end = 10.dp)
											.align(Alignment.Start))
									}
									
									if (message.id != null) {
										Image(ImageBitmap.imageResource(message.id!!), null)
									}
									
									Text(message.text, modifier = Modifier
										.padding(
											top = if (message.owner == null) 10.dp else 0.dp,
											start = 10.dp,
											bottom = 10.dp,
											end = 10.dp
										)
										.align(Alignment.Start))
								}
							}
							
							val chatBoxModifier = Modifier.sizeIn(maxWidth = 350.dp).padding(top = 5.dp, start = 5.dp)
							
							if (chatMessage.isRemote) {
								Box(
									modifier = chatBoxModifier
										.background(
											color = Color(238, 238, 238),
											shape = RoundedCornerShape(10.dp)
										)
								) {
									messageContent(chatMessage)
								}
							} else {
								Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = if (LocalConfiguration.current.screenWidthDp > 600) Arrangement.Start else Arrangement.End) {
									Box(
										modifier = chatBoxModifier
											.background(
												color = Color(199, 225, 252),
												shape = RoundedCornerShape(10.dp)
											)
									) {
										messageContent(chatMessage)
									}
								}
							}
						}
					}
					
					Row {
						val message = remember{mutableStateOf("")}
						
						TextField(
							message.value, { text ->
								message.value = text
							},
							modifier = Modifier
								.weight(1f)
								.fillMaxWidth(),
							placeholder = { Text(stringResource(R.string.co_message)) }
						)
						IconButton( {
							
							},
							modifier = Modifier.size(50.dp)
						) {
							Icon(Icons.AutoMirrored.Outlined.Send, null)
						}
					}
				}
			}
		}
	}
	
	class ChatMessage(var text: String, var isRemote: Boolean = true, @DrawableRes var id: Int? = null, var owner: String? = null)
	
	enum class MenuID {
		Connect,
		Remove
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
		
		val root = inflater.inflate(R.layout.fragment_co, container, false)
		
		root.findViewById<ComposeView>(R.id.co_compose_view).apply {
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
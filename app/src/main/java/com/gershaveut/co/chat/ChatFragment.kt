package com.gershaveut.co.chat

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.gershaveut.co.COClient
import com.gershaveut.co.MainActivity
import com.gershaveut.co.R
import com.gershaveut.co.coTag
import com.gershaveut.co.components.COAlertDialog
import com.gershaveut.co.components.RightModalNavigationDrawer
import com.gershaveut.co.service.COService
import com.gershaveut.coapikt.Message
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.SocketAddress

class ChatFragment : Fragment(), COClient.Listener, ServiceConnection {
	lateinit var coClient: COClient
	
	private lateinit var preferences: SharedPreferences
	private val connectionsType = object : TypeToken<ArrayList<Connection>>() {}.type
	
	var connections = listOf(Connection("test1", 932, "Test"), Connection("test2", 567, null))
	var users = listOf("User1", "User2")
	
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
				
				Button(
					{
						openLoginDialog.value = true
					}, modifier = Modifier.padding(bottom = 16.dp)
				) {
					Text(stringResource(R.string.co_connect), fontSize = 18.sp)
				}
				
				if (openLoginDialog.value) {
					COAlertDialog(
						stringResource(R.string.login_login),
						confirmText = stringResource(R.string.login_login),
						onDismissRequest = {},
						onConfirmRequest = {},
						content = {
							val hostname = remember { mutableStateOf("") }
							val port = remember { mutableStateOf("") }
							val name = remember { mutableStateOf("") }
							
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
												//if (!tryConnect(InetSocketAddress(connection.hostname, connection.port), connection.userName))
												//snackbar(R.string.login_error_connect)
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
							
							Text(
								stringResource(R.string.co_users) + ":",
								textAlign = TextAlign.Center,
								modifier = Modifier.padding(vertical = 15.dp)
							)
							
							LazyColumn {
								items(users) { user ->
									Box(
										modifier = Modifier
											.padding(horizontal = 5.dp)
											.fillMaxWidth()
									) {
										Text(user, modifier = Modifier.align(Alignment.CenterStart))
										IconButton(
											{
											
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
					LazyColumn(modifier = Modifier.weight(15f)) {
						items(
							listOf(
								ChatMessage(
									"Ты поедешь в Пирогово, будешь меня бранить Сереже?",
									false
								),
								ChatMessage("Я ни с кем не говорил, ни с Таней, дочерью.", true),
								ChatMessage("Но с Таней, сестрой, говорил?", false),
								ChatMessage("Да.", true),
								ChatMessage("Что же она говорила?", false),
								ChatMessage(
									"То же, что тебе… мне тебя защищала, тебе, вероятно, за меня говорила.",
									true
								),
								ChatMessage(
									"Да, она ужасно строга была ко мне. Слишком строга. Я не заслуживаю.",
									false
								),
								ChatMessage(
									"Пожалуйста, не будем говорить, уляжется, успокоится и, бог даст, уничтожится.",
									true
								),
								ChatMessage(
									"Не могу я не говорить. Мне слишком тяжело жить под вечным страхом. Теперь, если он заедет, начнется опять. Он не говорил ничего, но, может быть, заедет.",
									false
								),
								ChatMessage(
									"Только что надеялся успокоиться, как опять ты будто приготавливаешь меня к неприятному ожиданию.",
									true
								),
								ChatMessage(
									"Что же мне делать? Это может быть, он сказал Тане. Я не звала. Может быть, он заедет.",
									false
								),
								ChatMessage(
									"Заедет он или не заедет, неважно, даже твоя поездка не важна, важно, как я говорил тебе, два года назад говорил тебе, твое отношение к твоему чувству. Если бы ты признавала свое чувство нехорошим, ты бы не стала даже и вспоминать о том, заедет ли он, и говорить о нем.",
									true
								),
								ChatMessage("Ну, как же быть мне теперь?", false),
								ChatMessage("Покаяться в душе в своем чувстве.", true),
								ChatMessage("Не умею каяться и не понимаю, что это значит.", false),
								ChatMessage(
									"Это значит обсудить самой с собой, хорошо ли то чувство, которое ты испытываешь к этому человеку, или дурное.",
									true
								),
								ChatMessage(
									"Я никакого чувства не испытываю, ни хорошего, ни дурного.",
									false
								),
								ChatMessage("Это неправда.", true),
								ChatMessage("Чувство это так неважно, ничтожно.", false),
								ChatMessage(
									"Все чувства, а потому и самое ничтожное, всегда или хорошие, или дурные в наших глазах, и потому и тебе надо решить, хорошее ли это было чувство, или дурное.",
									true
								),
								ChatMessage(
									"Нечего решать, это чувство такое неважное, что оно не может быть дурным. Да и нет в нем ничего дурного.",
									false
								),
								ChatMessage(
									"Нет, исключительное чувство старой замужней женщины к постороннему мужчине — дурное чувство.",
									true
								),
								ChatMessage(
									"У меня нет чувства к мужчине, есть чувство к человеку.",
									false
								),
								ChatMessage("Да ведь человек этот мужчина.", true),
								ChatMessage(
									"Он для меня не мужчина. Нет никакого чувства исключительного, а есть то, что после моего горя мне было утешение музыка, а к человеку нет никакого особенного чувства.",
									false
								),
								ChatMessage("Зачем говорить неправду?", true),
								ChatMessage(
									"Но хорошо. Это было. Я сделала дурно, что заехала, что огорчила тебя. Но теперь это кончено, я сделаю все, чтобы не огорчать тебя.",
									false
								),
								ChatMessage(
									"Ты не можешь этого сделать потому, что все дело не в том, что ты сделаешь — заедешь, примешь, не примешь, дело все в твоем отношении к твоему чувству. Ты должна решить сама с собой, хорошее ли это, или дурное чувство.",
									true
								),
								ChatMessage("Да нет никакого.", false),
								ChatMessage(
									"Это неправда. И вот это-то и дурно для тебя, что ты хочешь скрыть это чувство, чтобы удержать его. А до тех пор, пока ты не решишь, хорошее это чувство или дурное, и не признаешь, что оно дурное, ты будешь не в состоянии не делать мне больно. Если ты признаешь, как ты признаешь теперь, что чувство это хорошее, то никогда не будешь в силах не желать удовлетворения этого чувства, то есть видеться, а желая, ты невольно будешь делать то, чтобы видеться. Если ты будешь избегать случаев видеться, то тебе будет тоска, тяжело. Стало быть, все дело в том, чтобы решить, какое это чувство, дурное или хорошее.",
									true
								)
							)
						) { chatMessage ->
							@Composable
							fun messageContent(message: ChatMessage) {
								Column(horizontalAlignment = Alignment.CenterHorizontally) {
									if (message.owner != null) {
										Text(
											message.owner!!,
											color = Color(41, 150, 201),
											fontSize = 15.sp,
											modifier = Modifier
												.padding(top = 10.dp, start = 10.dp, end = 10.dp)
												.align(Alignment.Start)
										)
									}
									
									if (message.id != null) {
										Image(ImageBitmap.imageResource(message.id!!), null)
									}
									
									Text(
										message.text, modifier = Modifier
											.padding(
												top = if (message.owner == null) 10.dp else 0.dp,
												start = 10.dp,
												bottom = 10.dp,
												end = 10.dp
											)
											.align(Alignment.Start)
									)
								}
							}
							
							val chatBoxModifier =
								Modifier
									.sizeIn(maxWidth = 350.dp)
									.padding(top = 5.dp, start = 5.dp)
							
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
								Row(
									modifier = Modifier.fillMaxWidth(),
									horizontalArrangement = if (LocalConfiguration.current.screenWidthDp > 600) Arrangement.Start else Arrangement.End
								) {
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
						val message = remember { mutableStateOf("") }
						
						TextField(
							message.value, { text ->
								message.value = text
							},
							modifier = Modifier
								.weight(1f)
								.fillMaxWidth(),
							placeholder = { Text(stringResource(R.string.co_message)) }
						)
						IconButton(
							{
							
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
	
	data class ChatMessage(
		var text: String,
		var isRemote: Boolean = true,
		@DrawableRes var id: Int? = null,
		var owner: String? = null
	)
	
	enum class MenuID {
		Connect,
		Remove
	}
	
	data class Connection(val hostname: String, val port: Int, val userName: String?)
	
	override fun onAttach(context: Context) {
		super.onAttach(context)
		
		preferences = context.getSharedPreferences(coTag, Context.MODE_PRIVATE)
		
		Intent(requireActivity(), COService::class.java).also {
			context.startService(it)
			context.bindService(it, this, Context.BIND_AUTO_CREATE)
		}
	}
	
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		super.onCreateView(inflater, container, savedInstanceState)
		
		val root = inflater.inflate(R.layout.fragment_co, container, false)
		
		root.findViewById<ComposeView>(R.id.co_compose_view).apply {
			setContent {
				Menu()
			}
		}
		
		return root
	}
	
	suspend fun tryConnect(endpoint: SocketAddress, userName: String?): Boolean {
		
		coClient.name = userName
		val result = coClient.tryConnect(endpoint)
		
		return result
	}
	
	override fun onMessage(message: Message) {
		Log.d(coTag, "receive_message: $message")
	}
	
	override fun onException(exception: String) {
		Log.e(coTag, exception)
	}
	
	override fun onConnected(endpoint: SocketAddress) {
		Log.i(coTag, "Connected to $endpoint")
	}
	
	@SuppressLint("NotifyDataSetChanged")
	override fun onDisconnected(reason: String?) {
		Log.i(coTag, "Disconnected")
	}
	
	override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
		val coService = (service as COService.LocalBinder).getService()
		
		coClient = coService.coClient
		coClient.listener = this
	}
	
	override fun onServiceDisconnected(name: ComponentName?) {
	}
}
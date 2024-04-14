package com.gershaveut.service.chatOFG

import kotlinx.coroutines.*
import java.io.*
import java.net.Socket
import java.net.SocketAddress

class COClient(private val event: COClientListener) {
	var name: String? = null
	
	private var socket: Socket = Socket()
	
	private var reader: BufferedReader? = null
	private var writer: PrintWriter? = null
	
	@OptIn(DelicateCoroutinesApi::class)
	suspend fun connect(endpoint: SocketAddress) = coroutineScope {
		if (socket.isClosed)
			socket = Socket()
		
		socket.connect(endpoint)
		
		GlobalScope.launch {
			receiveMessageHandler()
		}
	}
	
	suspend fun tryConnect(endpoint: SocketAddress): Boolean {
		try {
			connect(endpoint)
		} catch (e: Exception) {
			event.onException(e)
			
			withContext(Dispatchers.IO) {
				socket.close()
			}
			
			return false
		}
		
		return true
	}
	
	fun disconnect(cause: String?) {
		reader!!.close()
		writer!!.close()
		socket.close()
		event.onDisconnected(cause)
	}
	
	fun disconnect() {
		disconnect(null)
	}
	
	fun sendMessage(message: Message) {
		writer!!.println(message)
	}
	
	fun trySendMessage(text: Message): Boolean {
			try {
				sendMessage(text)
			} catch (e: Exception) {
				event.onException(e)
				
				return false
			}
			
			return true
	}
	
	fun kick(user: String, cause: String) {
		sendMessage(Message("$cause:$user", MessageType.Kick))
	}
	
	fun kick(user: String) {
		kick(user, "")
	}
	
	private fun receiveMessageHandler() {
		reader = BufferedReader(InputStreamReader(socket.getInputStream()))
		writer = PrintWriter(socket.getOutputStream(), true)
		
		writer!!.println(name!!)
		
		var cause: String? = "The remote host forcibly terminated the connection."
		
		try {
			while (socket.isConnected) {
				val message = Message.createMessageFromText(reader!!.readLine())
				
				if (message.equals(MessageType.Kick)) {
					cause = message.text
					break
				}
				
				event.onMessage(message)
			}
		} catch (e: Exception) {
			event.onException(e)
		}
		
		if (!socket.isClosed)
			disconnect(cause)
	}
	
	interface COClientListener {
		fun onMessage(message: Message)
		fun onException(exception: Exception)
		fun onDisconnected(cause: String?)
	}
}

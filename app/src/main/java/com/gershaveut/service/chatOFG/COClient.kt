package com.gershaveut.service.chatOFG

import kotlinx.coroutines.*
import java.io.*
import java.net.Socket
import java.net.SocketAddress

class COClient(private val event: COClientListener) : Serializable {
	var name: String? = null
	
	private var socket: Socket = Socket()
	
	private var reader: BufferedReader? = null
	private var writer: PrintWriter? = null
	
	@Throws(IOException::class)
	@OptIn(DelicateCoroutinesApi::class)
	suspend fun connect(endpoint: SocketAddress) = coroutineScope {
		if (socket.isClosed)
			socket = Socket()
		
		socket.connect(endpoint)
		
		GlobalScope.launch {
			receiveMessageHandler()
		}
	}
	
	suspend fun tryConnect(endpoint: SocketAddress) : Boolean {
		try {
			connect(endpoint)
		} catch (_: Exception) {
			withContext(Dispatchers.IO) {
				socket.close()
			}
			
			return false
		}
		
		return true
	}
	
	@Throws(IOException::class, NullPointerException::class)
	suspend fun disconnect(reason: String?) = coroutineScope {
		event.onDisconnected(reason)
		
		socket.close()
		reader!!.close()
		writer!!.close()
	}
	
	suspend fun disconnect() {
		disconnect(null)
	}
	
	suspend fun tryDisconnect(reason: String?) : Boolean {
		try {
			disconnect(reason)
		} catch (_: Exception) {
			return false
		}
		
		return true
	}
	
	suspend fun tryDisconnect() : Boolean {
		return tryDisconnect(null)
	}
	
	@Throws(IOException::class)
	suspend fun reconnect() {
		connect(socket.remoteSocketAddress)
	}
	
	@Throws(NullPointerException::class)
	fun sendMessage(message: Message) {
		writer!!.println(message)
	}
	
	fun trySendMessage(text: Message): Boolean {
			try {
				sendMessage(text)
			} catch (_: Exception) {
				return false
			}
			
			return true
	}
	
	@Throws(NullPointerException::class)
	fun kick(user: String, reason: String) {
		sendMessage(Message("$reason:$user", MessageType.Kick))
	}
	
	private suspend fun receiveMessageHandler() = coroutineScope {
		reader = BufferedReader(InputStreamReader(socket.getInputStream()))
		writer = PrintWriter(socket.getOutputStream(), true)
		
		writer!!.println(name!!)
		
		var reason: String? = "The remote host forcibly terminated the connection."
		
		try {
			while (socket.isConnected) {
				val message = Message.createMessageFromText(reader!!.readLine())
				
				if (message.equals(MessageType.Kick)) {
					reason = message.text
					break
				}
				
				event.onMessage(message)
			}
		} catch (e: Exception) {
			event.onException(e)
		}
		
		if (!socket.isClosed)
			disconnect(reason)
	}
	
	interface COClientListener {
		fun onMessage(message: Message)
		fun onException(exception: Exception)
		fun onDisconnected(reason: String?)
	}
}

package com.gershaveut.service.chatOFG

import com.gershaveut.coapikt.Message
import com.gershaveut.coapikt.MessageType
import com.gershaveut.ock.detailedException
import kotlinx.coroutines.*
import java.io.*
import java.net.Socket
import java.net.SocketAddress

class COClient(var listener: Listener?) {
	var name: String? = null
	private var socket: Socket = Socket()
	
	lateinit var lastConnect: SocketAddress
	
	private var reader: BufferedReader? = null
	private var writer: PrintWriter? = null
	private var connected: Boolean = false
	private var connecting: Boolean = false
	
	val isConnected get() = connected
	val isConnecting get() = connecting
	
	@Throws(IOException::class)
	suspend fun connect(endpoint: SocketAddress) = coroutineScope {
		socket = Socket()
		
		connecting = true
		
		try {
			socket.connect(endpoint)
		} finally {
			connecting = false
		}
		
		lastConnect = socket.remoteSocketAddress
		connected = true
		listener?.onConnected(endpoint)
		
		Thread { receiveMessageHandler() }.apply {
			name = "ReceiveMessageHandler"
			start()
		}
	}
	
	suspend fun tryConnect(endpoint: SocketAddress): Boolean {
		try {
			connect(endpoint)
		} catch (e: Exception) {
			listener?.onException(detailedException(e))
			
			withContext(Dispatchers.IO) {
				socket.close()
			}
			
			return false
		}
		
		return true
	}
	
	fun silentDisconnect() {
		socket.close()
		reader?.close()
		writer?.close()
		
		connected = false
	}
	
	fun trySilentDisconnect(): Boolean {
		try {
			silentDisconnect()
		} catch (e: Exception) {
			listener?.onException(detailedException(e))
			
			return false
		}
		
		return true
	}
	
	@Throws(IOException::class, NullPointerException::class)
	fun disconnect(reason: String?) {
		listener?.onDisconnected(reason)
		
		silentDisconnect()
	}
	
	fun disconnect() {
		disconnect(null)
	}
	
	fun tryDisconnect(reason: String?): Boolean {
		try {
			disconnect(reason)
		} catch (e: Exception) {
			listener?.onException(detailedException(e))
			
			return false
		}
		
		return true
	}
	
	fun tryDisconnect(): Boolean {
		return tryDisconnect(null)
	}
	
	@Throws(IOException::class)
	suspend fun reconnect() {
		connect(lastConnect)
	}
	
	suspend fun tryReconnect() : Boolean {
		try {
			reconnect()
		} catch (e: Exception) {
			listener?.onException(detailedException(e))
			
			return false
		}
		
		return true
	}
	
	@Throws(NullPointerException::class)
	fun sendMessage(text: String) {
		writer!!.println(text)
	}
	
	fun trySendMessage(text: String): Boolean {
		try {
			sendMessage(text)
		} catch (e: Exception) {
			listener?.onException(detailedException(e))
			
			return false
		}
		
		return true
	}
	
	@Throws(NullPointerException::class)
	fun kick(user: String, reason: String) {
		sendMessage(Message("$user:$reason", MessageType.Kick).toString())
	}
	
	@Throws(NullPointerException::class)
	fun broadcast(text: String) {
		sendMessage(Message(text, MessageType.Broadcast).toString())
	}
	
	private fun receiveMessageHandler() {
		reader = BufferedReader(InputStreamReader(socket.getInputStream()))
		writer = PrintWriter(socket.getOutputStream(), true)
		
		name?.let { writer!!.println(it) }
		
		var reason: String? = "The remote host forcibly terminated the connection."
		
		try {
			while (socket.isConnected) {
				val message = Message.createMessageFromText(reader!!.readLine())
				
				if (message.messageType == MessageType.Kick) {
					reason = message.text
					break
				}
				
				listener?.onMessage(message)
			}
		} catch (_: IOException) {
		
		} catch (e: Exception) {
			listener?.onException(detailedException(e))
		}
		
		if (!socket.isClosed)
			disconnect(reason)
	}
	
	interface Listener {
		fun onMessage(message: Message)
		fun onException(exception: String)
		fun onConnected(endpoint: SocketAddress)
		fun onDisconnected(reason: String?)
	}
}
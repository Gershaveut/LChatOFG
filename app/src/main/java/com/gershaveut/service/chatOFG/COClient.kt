package com.gershaveut.service.chatOFG

import com.gershaveut.service.detailedException
import kotlinx.coroutines.*
import java.io.*
import java.net.Socket
import java.net.SocketAddress

class COClient(private val listener: Listener?) {
	var name: String? = null
	var socket: Socket = Socket()
	@OptIn(DelicateCoroutinesApi::class)
	private val receiveMessageHandler = newFixedThreadPoolContext(1, "ReceiveMessageHandler")
	
	private var reader: BufferedReader? = null
	private var writer: PrintWriter? = null
	private var connected: Boolean = false
	private var connecting: Boolean = false
	
	val isConnected get() = connected
	val isConnecting get() = connecting
	
	@Throws(IOException::class)
	suspend fun connect(endpoint: SocketAddress) = coroutineScope {
		if (socket.isClosed)
			socket = Socket()
		
		connecting = true
		
		try {
			socket.connect(endpoint)
		} finally {
			connecting = false
		}
		
		listener?.onConnected(endpoint)
		connected = true
		
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
		reader!!.close()
		writer!!.close()
		
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
		connect(socket.remoteSocketAddress)
	}
	
	@Throws(NullPointerException::class)
	fun sendMessage(message: Message) {
		writer!!.println(message)
	}
	
	fun trySendMessage(text: Message): Boolean {
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
		sendMessage(Message("$reason:$user", MessageType.Kick))
	}
	
	private fun receiveMessageHandler() {
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
				
				listener?.onMessage(message)
			}
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

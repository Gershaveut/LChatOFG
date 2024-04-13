package com.gershaveut.service.chatOFG

import android.util.Log
import com.gershaveut.service.coTag
import kotlinx.coroutines.*
import java.io.*
import java.net.Socket
import java.net.SocketAddress

class COClient(private val onTextChange: (Message) -> Unit,  private val onException: ((Exception) -> Unit)?,  private val onDisconnected: (() -> Unit)?) {
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
			onException?.invoke(e)
			
			withContext(Dispatchers.IO) {
				socket.close()
			}
			
			return false
		}
		
		return true
	}
	
	fun disconnect() {
		reader!!.close()
		writer!!.close()
		socket.close()
		onDisconnected?.invoke()
	}
	
	fun sendMessage(text: String) {
		writer!!.println(text)
	}
	
	fun trySendMessage(text: String): Boolean {
			try {
				sendMessage(text)
			} catch (e: Exception) {
				onException?.invoke(e)
				
				return false
			}
			
			return true
	}
	
	private fun receiveMessageHandler() {
		reader = BufferedReader(InputStreamReader(socket.getInputStream()))
		writer = PrintWriter(socket.getOutputStream(), true)
		
		writer!!.println(name!!)
		
		try {
			while (socket.isConnected) {
				onTextChange.invoke(Message(reader!!.readLine()))
			}
		} catch (e: Exception) {
			onException?.invoke(e)
		}
		
		disconnect()
	}
}

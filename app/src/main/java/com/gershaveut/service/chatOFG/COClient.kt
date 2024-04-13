package com.gershaveut.service.chatOFG

import android.util.Log
import com.gershaveut.service.coTag
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import java.net.SocketAddress

class COClient(private val onTextChange: (String) -> Unit,  private val onException: ((Exception) -> Unit)?,  private val onDisconnected: (() -> Unit)?) {
	var name: String? = null
	
	var socket: Socket = Socket()
	
	private var reader: BufferedReader? = null
	private var writer: BufferedWriter? = null
	
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
		writer!!.write(text)
		writer!!.flush()
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
		writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
		
		writer!!.write(name!!)
		writer!!.flush()
		
		try {
			while (socket.isConnected) {
				val text = reader!!.readLine()
				
				if (text.isNullOrEmpty())
					continue
				
				onTextChange.invoke(text)
			}
		} catch (e: Exception) {
			onException?.invoke(e)
		}
		
		disconnect()
	}
}

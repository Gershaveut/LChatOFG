package com.gershaveut.service.chatOFG

import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import java.net.SocketAddress

class COClient(private val onTextChange: (String) -> Unit) {
	var name: String? = null
	
	var socket: Socket = Socket()
	
	private var reader: BufferedReader? = null
	private var writer: BufferedWriter? = null
	
	@OptIn(DelicateCoroutinesApi::class)
	suspend fun connect(endpoint: SocketAddress) = coroutineScope {
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
			println(e)
			
			return false
		}
		
		return true
	}
	
	fun disconnected() {
		reader!!.close()
		writer!!.close()
		socket.close()
	}
	
	fun sendMessage(text: String) {
		writer!!.write(text)
		writer!!.flush()
	}
	
	fun trySendMessage(text: String): Boolean {
		if (writer != null) {
			try {
				sendMessage(text)
			} catch (_: Exception) {
				return false
			}
			
			return true
		} else
			return false
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
			//Log.e("error", e.toString())
		}
		
		disconnected()
	}
}

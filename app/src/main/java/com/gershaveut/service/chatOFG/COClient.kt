package com.gershaveut.service.chatOFG

import android.util.Log
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import java.net.SocketAddress

class COClient(private val onTextChange: (String) -> Unit) {
	var name: String? = null
	val socket: Socket = Socket()
	
	private var reader: BufferedReader? = null
	private var writer: BufferedWriter? = null
	
	@OptIn(DelicateCoroutinesApi::class)
	fun connect(endpoint: SocketAddress) {
			GlobalScope.launch {
				socket.connect(endpoint)
				receiveMessageHandler()
			}
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
	
	private suspend fun receiveMessageHandler() = coroutineScope{
		reader = BufferedReader(InputStreamReader(socket.getInputStream()))
		writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
		
		writer!!.write(name!!)
		writer!!.flush()
		writer!!.close()
		
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

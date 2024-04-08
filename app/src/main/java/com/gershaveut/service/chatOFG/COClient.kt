package com.gershaveut.service.chatOFG

import com.gershaveut.service.MainActivity
import com.gershaveut.service.chatOFG.ui.COFragment
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.*
import java.net.Socket
import java.net.SocketAddress
import java.util.EventListener
import java.util.EventObject
import kotlin.properties.Delegates

class COClient(private val textSetter: TextSetter) {
	var name: String? = null
	private val socket: Socket = Socket()
	
	@OptIn(DelicateCoroutinesApi::class)
	fun connect(endpoint: SocketAddress) {
		if (name != null) {
			socket.connect(endpoint)
			
			GlobalScope.launch {
				receiveMessageHandler()
			}
		}
		else
			println("No name assigned")
	}
	
	private suspend fun receiveMessageHandler() = coroutineScope{
		val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
		val writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
		
		writer.write(name!!)
		writer.flush()
		
		while (true) {
			val text = reader.readLine()
			
			if (text.isNullOrEmpty())
				continue
			
			textSetter.appendLine(text)
		}
	}
}

/*
class ChatOFGClient(private val logger: Logger) {
	private var client: Socket? = null
	private var name: String = ""
	private var reader: BufferedReader? = null
	private var writer: BufferedWriter? = null
	
	var receiveMessage: ((Message) -> Unit)? = null
	var connectionLost: (() -> Unit)? = null
	
	fun connect(remote: String, port: Int, name: String) {
		this.name = name
		
		disconnect()
		
		client = Socket(remote, port)
		
		reader = BufferedReader(InputStreamReader(client?.getInputStream()))
		writer = BufferedWriter(OutputStreamWriter(client?.getOutputStream()))
		
		reader?.let {
			Thread {
				receiveMessageHandler()
			}.start()
			
			writer?.write(name)
			writer?.newLine()
			writer?.flush()
		}
	}
	
	fun disconnect() {
		client?.close()
		reader?.close()
		writer?.close()
	}
	
	fun sendMessage(message: Message) {
		writer?.write(message.toString())
		writer?.newLine()
		writer?.flush()
	}
	
	fun isConnected(): Boolean {
		return client?.isConnected ?: false
	}
	
	private fun receiveMessageHandler() {
		while (isConnected()) {
			try {
				val text = reader?.readLine()
				
				if (text.isNullOrBlank()) {
					continue
				}
				
				val message = Message(text)
				
				if (message == MessageType.Kick) {
					logger.write("Вы были исключены по причине: $message", LoggerLevel.Warn)
				} else {
					logger.write(message.text, LoggerLevel.Info)
				}
				
				receiveMessage?.invoke(message)
			} catch (ex: Exception) {
				logger.write(ex.message ?: "Unknown error", LoggerLevel.Error)
			}
		}
		
		connectionLost?.invoke()
		logger.write("Соединение потерянно", LoggerLevel.Warn)
	}
}
*/

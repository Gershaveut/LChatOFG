package com.gershaveut.service

import com.gershaveut.service.chatOFG.COClient
import java.net.InetSocketAddress

suspend fun main() {
	val coClient = COClient { text ->
		println(text)
	}
	
	coClient.name = "name"
	coClient.connect(InetSocketAddress("127.0.0.1", 7500))
	
	while (true) {
		if (coClient.socket.isConnected)
			coClient.sendMessage(readln())
	}
}
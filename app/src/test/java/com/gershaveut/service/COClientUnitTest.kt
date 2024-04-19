package com.gershaveut.service

import com.gershaveut.service.chatOFG.COClient
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.net.InetSocketAddress

class COClientUnitTest {
	@Test
	fun connecting() {
		val coClient = COClient(null)
		coClient.name = "test"
		
		runBlocking {
			coClient.connect(InetSocketAddress("127.0.0.1", 7500))
		}
	}
}
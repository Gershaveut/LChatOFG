package com.gershaveut.service.chatOFG

import java.io.Serializable
import java.net.SocketAddress

data class Connection(val socketAddress: SocketAddress, val userName: String) : Serializable
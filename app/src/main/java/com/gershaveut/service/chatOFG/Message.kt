package com.gershaveut.service.chatOFG

class Message(var text: String, var messageType: MessageType) {
	
	constructor(text: String) : this(text, MessageType.Message) {
		this.text = text.substring(text.indexOf(':') + 1)
		
		try {
			messageType = MessageType.valueOf(text.split(':')[0])
		} catch (_: Exception) {
		
		}
	}
	
	override fun toString(): String {
		return "$messageType:$text"
	}
}

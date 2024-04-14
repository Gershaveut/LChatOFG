package com.gershaveut.service.chatOFG

class Message(var text: String, var messageType: MessageType = MessageType.Message) {
	
	override fun equals(other: Any?): Boolean {
		when (other) {
			is MessageType -> return messageType == other
			is String -> return text == other
		}
		
		return super.equals(other)
	}
	
	override fun toString(): String {
		return "$messageType:$text"
	}
	
	override fun hashCode(): Int {
		var result = text.hashCode()
		result = 31 * result + messageType.hashCode()
		return result
	}
	
	companion object {
		fun createMessageFromText(text: String) : Message {
			return try {
				Message(text.substring(text.indexOf(':') + 1), MessageType.valueOf(text.split(':')[0]))
			} catch (_: Exception) {
				Message(text)
			}
		}
	}
}

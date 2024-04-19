package com.gershaveut.service

import android.os.Debug

const val coTag: String = "ChatOFG"
var debug: Boolean = Debug.isDebuggerConnected()

fun detailedException(exception: Exception) : String {
	var text = exception.message.toString()
	
	for(line in exception.stackTrace) {
		text += "\n		at $line"
	}
	
	return text
}
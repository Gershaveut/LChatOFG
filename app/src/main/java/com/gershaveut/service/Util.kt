package com.gershaveut.service

import android.os.Debug

const val coTag: String = "ChatOFG"

const val coChatId: String = "co_chat_id"

val debug get() = Debug.isDebuggerConnected()
var notificationId: Int = 1
	get() {
		return field++
	}
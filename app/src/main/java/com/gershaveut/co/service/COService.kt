package com.gershaveut.service.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.gershaveut.service.chatOFG.COClient

class COService : Service() {
	private val binder = LocalBinder()
	
	lateinit var coClient: COClient
	
	inner class LocalBinder : Binder() {
		fun getService() = this@COService
	}
	
	override fun onBind(intent: Intent): IBinder {
		return binder
	}
	
	override fun onCreate() {
		coClient = COClient(null)
	}
	
	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		return START_STICKY
	}
	
	override fun onDestroy() {
		if (coClient.isConnected)
			coClient.silentDisconnect()
	}
}
package com.gershaveut.service.service

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.view.View
import com.gershaveut.service.R
import com.gershaveut.service.chatOFG.COClient
import com.gershaveut.service.coChatId
import com.gershaveut.service.notificationId

class COService : Service() {
	private val binder = LocalBinder()
	
	lateinit var coClient: COClient
	lateinit var notification: Notification
	
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
		notification = Notification.Builder(this, coChatId)
			.setContentTitle(getString(R.string.co_service))
			.setContentText(intent?.getStringExtra("endpoint"))
			.setSmallIcon(R.drawable.ic_menu_chat_ofg)
			.setOngoing(true)
			.setVisibility(Notification.VISIBILITY_SECRET)
			.build()
		
		startForeground(notificationId, notification)
		
		return START_STICKY
	}
	
	override fun onDestroy() {
		if (coClient.isConnected)
			coClient.silentDisconnect()
	}
}
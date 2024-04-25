package com.gershaveut.service.chatOFG

import android.os.Parcel
import android.os.Parcelable

data class Connection(val hostname: String, val port: Int, val userName: String) : Parcelable {
	constructor(parcel: Parcel) : this(parcel.readString()!!, parcel.readInt(), parcel.readString()!!) {
	
	}
	
	override fun describeContents(): Int {
		return 0
	}
	
	override fun writeToParcel(dest: Parcel, flags: Int) {
		dest.writeString(hostname);
		dest.writeInt(port);
		dest.writeString(userName);
	}
	
	companion object CREATOR : Parcelable.Creator<Connection> {
		override fun createFromParcel(parcel: Parcel): Connection {
			val hostname = parcel.readString()!!
			val port = parcel.readInt()
			val userName = parcel.readString()!!
			
			return Connection(hostname, port, userName)
		}
		
		override fun newArray(size: Int): Array<Connection?> {
			return arrayOfNulls(size)
		}
	}
}
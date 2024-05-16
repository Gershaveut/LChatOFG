package com.gershaveut.service.chatOFG.ui

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.gershaveut.service.MainActivity
import com.gershaveut.service.R
import com.gershaveut.service.chatOFG.Connection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.InetSocketAddress

class ConnectionAdapter(private val context: Context, var connections: ArrayList<Connection>, private val coFragment: COFragment) : RecyclerView.Adapter<ConnectionAdapter.ViewHolder>() {
	
	override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
		val view: View = LayoutInflater.from(viewGroup.context).inflate(R.layout.co_connection, viewGroup, false)
		return ViewHolder(view)
	}
	
	fun registerConnection(connection: Connection) {
		if (!connections.contains(connection)) {
			connections.add(connection)
			notifyItemInserted(connections.size - 1)
		}
	}
	
	@SuppressLint("SetTextI18n")
	override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
		val connection = connections[position]
		
		viewHolder.viewName.text = connection.userName ?: context.getString(R.string.login_no_name)
		viewHolder.viewDescription.text = "${connection.hostname}:${connection.port}"
		
		viewHolder.buttonActions.setOnClickListener {
			val popupMenu = PopupMenu(context, viewHolder.itemView)
			
			popupMenu.menu.add(0, MenuID.Connect.ordinal, Menu.NONE, R.string.co_connect)
			popupMenu.menu.add(0, MenuID.Remove.ordinal, Menu.NONE, R.string.co_remove)
			
			popupMenu.setOnMenuItemClickListener {
				when (it.itemId) {
					MenuID.Connect.ordinal -> (context as MainActivity).lifecycleScope.launch(Dispatchers.IO) {
						if (!coFragment.tryConnect(InetSocketAddress(connection.hostname, connection.port), connection.userName))
							coFragment.snackbar(R.string.login_error_connect)
					}
					MenuID.Remove.ordinal -> {
						val index = connections.indexOf(connection)
						
						notifyItemRemoved(index)
						connections.removeAt(index)
					}
				}
				
				return@setOnMenuItemClickListener true
			}
			
			popupMenu.show()
		}
	}
	
	override fun getItemCount(): Int {
		return connections.size
	}
	
	class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
		val viewName: TextView = view.findViewById(R.id.viewConnectionName)
		val viewDescription: TextView = view.findViewById(R.id.viewConnectionDescription)
		val buttonActions: ImageButton = view.findViewById(R.id.buttonConnectionActions)
	}
	
	enum class MenuID {
		Connect,
		Remove
	}
}
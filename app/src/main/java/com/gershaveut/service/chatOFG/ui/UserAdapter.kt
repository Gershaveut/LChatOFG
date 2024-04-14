package com.gershaveut.service.chatOFG.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gershaveut.service.R
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class UserAdapter(private val context: Context, val users: ArrayList<User>) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {
	override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
		val view: View = LayoutInflater.from(viewGroup.context).inflate(R.layout.co_user, viewGroup, false)
		return ViewHolder(view)
	}
	
	@OptIn(DelicateCoroutinesApi::class)
	override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
		val user = users[position]
		
		viewHolder.userName.text = user.name
		
		viewHolder.userActions.setOnClickListener {
			val popupMenu = PopupMenu(context, viewHolder.itemView)
			
			popupMenu.menu.add(0, MenuID.Kick.ordinal, Menu.NONE, R.string.co_kick)
			
			popupMenu.setOnMenuItemClickListener {
				when (it.itemId) {
					MenuID.Kick.ordinal -> GlobalScope.launch {
						COFragment.coClient!!.kick(user.name) //TODO: Request a reason
					}
				}
				
				return@setOnMenuItemClickListener true
			}
			
			popupMenu.show()
		}
	}
	
	override fun getItemCount(): Int {
		return users.size
	}
	
	class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
		val userName: TextView = view.findViewById(R.id.user_name)
		val userActions: ImageButton = view.findViewById(R.id.user_actions)
	}
	
	enum class MenuID {
		Kick
	}
}
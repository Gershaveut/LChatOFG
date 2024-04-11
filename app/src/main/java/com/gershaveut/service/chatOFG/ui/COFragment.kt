package com.gershaveut.service.chatOFG.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import com.gershaveut.service.R
import com.gershaveut.service.chatOFG.COClient
import com.gershaveut.service.coTag
import com.gershaveut.service.databinding.FragmentCoBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class COFragment : Fragment() {
	
	private var _binding: FragmentCoBinding? = null
	private val binding get() = _binding!!
	
	private var coClient: COClient? = null
	
	@OptIn(DelicateCoroutinesApi::class)
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		_binding = FragmentCoBinding.inflate(inflater, container, false)
		val root: View = binding.root
		
		val viewChat = binding.viewChat
		val editMessage = binding.editMessage
		val buttonSend = binding.buttonSend
		val chatScrollView = binding.chatScrollView
		
		val loginDialog = LoginDialogFragment(this) { text ->
			Log.d(coTag, "receive_message: $text")
			viewChat.append(text)
		}
		
		if (coClient == null)
			coClient = loginDialog.showAndGetCOClient(parentFragmentManager, null)
		
		buttonSend.setOnClickListener {
			chatScrollView.fullScroll(View.FOCUS_DOWN)
			editMessage.text = null
			
			GlobalScope.launch {
				val message = editMessage.text.toString()
				
				if (!coClient!!.trySendMessage(message))
					Snackbar.make(root, R.string.co_error_send, 1000).show()
				else
					Log.d(coTag, "send_message: $message")
			}
		}
		
		binding.buttonDisconnect.setOnClickListener {
			coClient!!.disconnect()
		}
		
		return root
	}
	
	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}
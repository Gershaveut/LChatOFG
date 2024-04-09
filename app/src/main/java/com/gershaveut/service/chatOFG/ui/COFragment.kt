package com.gershaveut.service.chatOFG.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.gershaveut.service.R
import com.gershaveut.service.coTag
import com.gershaveut.service.databinding.FragmentChatOfgBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class COFragment : Fragment() {
	
	private var _binding: FragmentChatOfgBinding? = null
	private val binding get() = _binding!!
	
	@OptIn(DelicateCoroutinesApi::class)
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		//val COViewModel = ViewModelProvider(this)[COViewModel::class.java]
		
		_binding = FragmentChatOfgBinding.inflate(inflater, container, false)
		val root: View = binding.root
		
		
		val viewChat = binding.viewChat
		val editMessage = binding.editMessage
		val buttonSend = binding.buttonSend
		val chatScrollView = binding.chatScrollView
		
		val coClient = LoginDialogFragment { text ->
			Log.d(coTag, "receive_message: $text")
			viewChat.append(text)
		}.showAndGetCOClient(parentFragmentManager, null)
		
		buttonSend.setOnClickListener {
			chatScrollView.fullScroll(View.FOCUS_DOWN)
			editMessage.text = null
			
			GlobalScope.launch {
				val message = editMessage.text.toString()
				
				if (!coClient.trySendMessage(message))
					Snackbar.make(root, R.string.co_error_send, 1000).show()
				else
					Log.d(coTag, "send_message: $message")
			}
		}
		
		return root
	}
	
	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}
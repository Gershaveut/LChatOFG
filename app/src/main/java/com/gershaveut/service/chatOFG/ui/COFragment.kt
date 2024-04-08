package com.gershaveut.service.chatOFG.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.gershaveut.service.chatOFG.COClient
import com.gershaveut.service.chatOFG.TextSetter
import com.gershaveut.service.databinding.FragmentChatOfgBinding

class COFragment : Fragment() {
	
	private var _binding: FragmentChatOfgBinding? = null
	private val binding get() = _binding!!
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		val COViewModel = ViewModelProvider(this).get(COViewModel::class.java)
		
		_binding = FragmentChatOfgBinding.inflate(inflater, container, false)
		val root: View = binding.root
		
		
		val chat: TextView = binding.editTextTextMultiLine
		
		val int = TextSetter {
		
		}
		
		LoginDialogFragment(TextSetter {
		
		}).show(parentFragmentManager, null)
		
		return root
	}
	
	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}
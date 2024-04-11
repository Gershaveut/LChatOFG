package com.gershaveut.service.chatOFG.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gershaveut.service.R
import com.gershaveut.service.databinding.FragmentCoLoginBinding

class COLoginFragment : Fragment() {
	
	private var _binding: FragmentCoLoginBinding? = null
	private val binding get() = _binding!!
	
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		_binding = FragmentCoLoginBinding.inflate(inflater, container, false)
		val root: View = binding.root
		
		binding.button.setOnClickListener {
			findNavController().navigate(R.id.action_COLoginFragment_to_COFragment)
		}
		
		return root
	}
}
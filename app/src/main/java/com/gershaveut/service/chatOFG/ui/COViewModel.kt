package com.gershaveut.service.chatOFG.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class COViewModel : ViewModel() {
	private val _text = MutableLiveData<String>().apply { value = "" }
	val text: LiveData<String> = _text
}
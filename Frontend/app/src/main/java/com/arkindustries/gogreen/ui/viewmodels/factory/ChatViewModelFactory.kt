package com.arkindustries.gogreen.ui.viewmodels.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.arkindustries.gogreen.ui.repositories.ChatRepository
import com.arkindustries.gogreen.ui.viewmodels.ChatViewModel

class ChatViewModelFactory(private val chatRepository: ChatRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel (chatRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

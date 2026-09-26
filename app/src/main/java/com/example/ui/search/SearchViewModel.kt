package com.example.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SalimApplication
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.repository.ConversationRepository
import com.example.data.repository.MessagingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class SearchUiState(
    val query: String = "",
    val matchingConversations: List<ConversationEntity> = emptyList(),
    val matchingMessages: List<MessageEntity> = emptyList()
)

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val conversationRepository: ConversationRepository =
        (application as SalimApplication).conversationRepository
    private val messagingRepository: MessagingRepository =
        (application as SalimApplication).messagingRepository

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val uiState: StateFlow<SearchUiState> = combine(
        _query,
        conversationRepository.getActiveConversations(),
        messagingRepository.getStarredMessages()
    ) { q, allConvs, _ ->
        if (q.isBlank()) {
            SearchUiState(query = q)
        } else {
            val matchingConvs = allConvs.filter {
                (it.recipientName?.contains(q, ignoreCase = true) == true) ||
                        it.recipientAddress.contains(q, ignoreCase = true) ||
                        (it.lastMessageText?.contains(q, ignoreCase = true) == true)
            }
            SearchUiState(
                query = q,
                matchingConversations = matchingConvs
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchUiState()
    )

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
    }
}

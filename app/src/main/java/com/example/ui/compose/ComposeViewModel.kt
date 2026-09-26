package com.example.ui.compose

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SalimApplication
import com.example.data.repository.ConversationRepository
import com.example.telephony.ContactInfo
import com.example.telephony.ContactResolver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ComposeUiState(
    val query: String = "",
    val contacts: List<ContactInfo> = emptyList(),
    val isSearching: Boolean = false
)

class ComposeViewModel(application: Application) : AndroidViewModel(application) {

    private val conversationRepository: ConversationRepository =
        (application as SalimApplication).conversationRepository

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _contacts = MutableStateFlow<List<ContactInfo>>(emptyList())
    private val _isSearching = MutableStateFlow(false)

    val uiState: StateFlow<ComposeUiState> = combine(
        _query,
        _contacts,
        _isSearching
    ) { q, list, searching ->
        ComposeUiState(query = q, contacts = list, isSearching = searching)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ComposeUiState()
    )

    init {
        search("")
    }

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
        search(newQuery)
    }

    private fun search(q: String) {
        viewModelScope.launch {
            _isSearching.value = true
            try {
                val results = ContactResolver.searchContacts(getApplication(), q)
                _contacts.value = results
            } finally {
                _isSearching.value = false
            }
        }
    }

    suspend fun getOrCreateConversationId(address: String, name: String?): Long {
        return conversationRepository.getOrCreateConversation(address, name)
    }
}

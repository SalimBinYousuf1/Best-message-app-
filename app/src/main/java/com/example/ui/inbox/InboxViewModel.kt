package com.example.ui.inbox

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SalimApplication
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.preferences.SalimUserPreferences
import com.example.data.repository.ConversationRepository
import com.example.telephony.DefaultSmsRoleManager
import com.example.telephony.SmsSyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class InboxFilter {
    ALL,
    UNREAD,
    STARRED,
    ARCHIVED
}

data class InboxUiState(
    val filter: InboxFilter = InboxFilter.ALL,
    val conversations: List<ConversationEntity> = emptyList(),
    val pinnedConversations: List<ConversationEntity> = emptyList(),
    val isDefaultSmsApp: Boolean = true,
    val unreadCountTotal: Int = 0,
    val isSyncing: Boolean = false,
    val preferences: SalimUserPreferences = SalimUserPreferences()
)

class InboxViewModel(application: Application) : AndroidViewModel(application) {

    private val conversationRepository: ConversationRepository =
        (application as SalimApplication).conversationRepository
    private val preferences = (application as SalimApplication).preferences

    private val _filter = MutableStateFlow(InboxFilter.ALL)
    val filter: StateFlow<InboxFilter> = _filter.asStateFlow()

    private val _isDefaultSmsApp = MutableStateFlow(DefaultSmsRoleManager.isDefaultSmsApp(application))
    val isDefaultSmsApp: StateFlow<Boolean> = _isDefaultSmsApp.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    val uiState: StateFlow<InboxUiState> = combine(
        _filter,
        conversationRepository.getActiveConversations(),
        conversationRepository.getArchivedConversations(),
        conversationRepository.getUnreadConversations(),
        combine(_isDefaultSmsApp, _isSyncing, preferences.preferencesFlow) { isDefault, syncing, prefs ->
            Triple(isDefault, syncing, prefs)
        }
    ) { filter, active, archived, unread, triple ->
        val (isDefault, syncing, prefs) = triple
        val list = when (filter) {
            InboxFilter.ALL -> active
            InboxFilter.UNREAD -> unread
            InboxFilter.STARRED -> active.filter { it.isPinned }
            InboxFilter.ARCHIVED -> archived
        }
        val pinned = active.filter { it.isPinned && !it.isArchived }
        val totalUnread = active.sumOf { it.unreadCount }

        InboxUiState(
            filter = filter,
            conversations = list,
            pinnedConversations = pinned,
            isDefaultSmsApp = isDefault,
            unreadCountTotal = totalUnread,
            isSyncing = syncing,
            preferences = prefs
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InboxUiState()
    )

    init {
        refreshDefaultSmsStatus()
        syncSms()
    }

    fun setFilter(filter: InboxFilter) {
        _filter.value = filter
    }

    fun refreshDefaultSmsStatus() {
        _isDefaultSmsApp.value = DefaultSmsRoleManager.isDefaultSmsApp(getApplication())
    }

    fun syncSms() {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                SmsSyncManager.syncDeviceMessages(getApplication())
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun togglePin(conversation: ConversationEntity) {
        viewModelScope.launch {
            conversationRepository.setPinned(conversation.id, !conversation.isPinned)
        }
    }

    fun toggleArchive(conversation: ConversationEntity) {
        viewModelScope.launch {
            conversationRepository.setArchived(conversation.id, !conversation.isArchived)
        }
    }

    fun toggleMute(conversation: ConversationEntity) {
        viewModelScope.launch {
            conversationRepository.setMuted(conversation.id, !conversation.isMuted)
        }
    }

    fun deleteConversation(id: Long) {
        viewModelScope.launch {
            conversationRepository.deleteConversation(id)
        }
    }
}

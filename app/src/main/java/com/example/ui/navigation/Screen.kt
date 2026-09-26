package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Inbox : Screen("inbox")
    object Conversation : Screen("conversation/{conversationId}/{recipientAddress}") {
        fun createRoute(conversationId: Long, recipientAddress: String): String =
            "conversation/$conversationId/${java.net.URLEncoder.encode(recipientAddress, "UTF-8")}"
    }
    object Compose : Screen("compose")
    object Search : Screen("search")
    object Scheduled : Screen("scheduled")
    object Settings : Screen("settings")
    object Onboarding : Screen("onboarding")
}

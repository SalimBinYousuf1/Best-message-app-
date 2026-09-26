package com.example.ui.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.ui.compose.ComposeScreen
import com.example.ui.compose.ComposeViewModel
import com.example.ui.conversation.ConversationScreen
import com.example.ui.conversation.ConversationViewModel
import com.example.ui.inbox.InboxScreen
import com.example.ui.inbox.InboxViewModel
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.scheduled.ScheduledListScreen
import com.example.ui.scheduled.ScheduledViewModel
import com.example.ui.search.SearchScreen
import com.example.ui.search.SearchViewModel
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.SettingsViewModel

@Composable
fun SalimNavGraph(
    navController: NavHostController,
    startDestination: String,
    onCompleteOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onCompleteOnboarding = {
                    onCompleteOnboarding()
                    navController.navigate(Screen.Inbox.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Inbox.route) {
            val inboxViewModel: InboxViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    return InboxViewModel(application) as T
                }
            })

            InboxScreen(
                viewModel = inboxViewModel,
                onNavigateToConversation = { convId, address ->
                    navController.navigate(Screen.Conversation.createRoute(convId, address))
                },
                onNavigateToCompose = {
                    navController.navigate(Screen.Compose.route)
                },
                onNavigateToSearch = {
                    navController.navigate(Screen.Search.route)
                },
                onNavigateToScheduled = {
                    navController.navigate(Screen.Scheduled.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.Conversation.route,
            arguments = listOf(
                navArgument("conversationId") { type = NavType.LongType },
                navArgument("recipientAddress") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getLong("conversationId") ?: 0L
            val encodedAddress = backStackEntry.arguments?.getString("recipientAddress") ?: ""
            val recipientAddress = java.net.URLDecoder.decode(encodedAddress, "UTF-8")

            val convViewModel: ConversationViewModel = viewModel(
                key = "conv_$conversationId",
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return ConversationViewModel(application, conversationId, recipientAddress) as T
                    }
                }
            )

            ConversationScreen(
                viewModel = convViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Compose.route) {
            val composeViewModel: ComposeViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    return ComposeViewModel(application) as T
                }
            })

            ComposeScreen(
                viewModel = composeViewModel,
                onNavigateToConversation = { convId, address ->
                    navController.navigate(Screen.Conversation.createRoute(convId, address)) {
                        popUpTo(Screen.Compose.route) { inclusive = true }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Search.route) {
            val searchViewModel: SearchViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    return SearchViewModel(application) as T
                }
            })

            SearchScreen(
                viewModel = searchViewModel,
                onNavigateToConversation = { convId, address ->
                    navController.navigate(Screen.Conversation.createRoute(convId, address))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Scheduled.route) {
            val scheduledViewModel: ScheduledViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    return ScheduledViewModel(application) as T
                }
            })

            ScheduledListScreen(
                viewModel = scheduledViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            val settingsViewModel: SettingsViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(application) as T
                }
            })

            SettingsScreen(
                viewModel = settingsViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

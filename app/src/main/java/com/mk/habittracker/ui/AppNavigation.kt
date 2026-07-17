package com.mk.habittracker.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.mk.habittracker.ui.login.LoginScreen
import com.mk.habittracker.ui.login.LoginViewModel
import com.mk.habittracker.ui.login.SignupScreen
import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute : NavKey

@Serializable
data object AddHabitRoute : NavKey

@Serializable
data class HabitDetailRoute(
    val habitId: String,
) : NavKey

@Serializable
data object LoginRoute : NavKey

@Serializable
data object SignupRoute : NavKey

@Suppress("ktlint:compose:vm-injection-check")
@Composable
fun AppNavigation(
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val user by viewModel.authState.collectAsStateWithLifecycle()

    // this is all that's needed to drive transitions between login and home
    val backStack = if (user == null) {
        rememberNavBackStack(LoginRoute)
    } else {
        rememberNavBackStack(HomeRoute)
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
        entryProvider =
            entryProvider {
                entry<HomeRoute> {
                    MainScreen(
                        onHabitClick = { backStack.add(HabitDetailRoute(it)) },
                        onAddHabit = { backStack.add(AddHabitRoute) },
                        onLogout = viewModel::signOut,
                    )
                }
                // TODO: container animation
                entry<AddHabitRoute>(
                    metadata =
                        DialogSceneStrategy.dialog(
                            DialogProperties(usePlatformDefaultWidth = false),
                        ),
                ) {
                    AddHabitScreen(
                        onDismiss = { backStack.removeLastOrNull() },
                    )
                }
                entry<HabitDetailRoute> { key ->
                    // use assistedDI with vm scoped to entry (not activity) by
                    // rememberViewModelStoreNavEntryDecorator decorator
                    HabitDetailScreen(
                        vm =
                            hiltViewModel<HabitDetailViewModel, HabitDetailViewModel.Factory>(
                                creationCallback = { it.create(key.habitId) },
                            ),
                        onBack = { backStack.removeLastOrNull() },
                    )
                }
                entry<LoginRoute> {
                    LoginScreen(
                        onSignupClick = { backStack.add(SignupRoute) },
                    )
                }
                entry<SignupRoute> {
                    SignupScreen(
                        onCancel = { backStack.removeLastOrNull() }
                    )
                }
            },
    )
}

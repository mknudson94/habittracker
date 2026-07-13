package com.mk.habittracker.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable

@Serializable
data object MainRoute : NavKey

@Serializable
data object AddHabitRoute : NavKey

@Serializable
data class HabitDetailRoute(
    val habitId: String,
) : NavKey

@Suppress("ktlint:compose:vm-injection-check")
@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(MainRoute)

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
                entry<MainRoute> {
                    MainScreen(
                        onHabitClick = { backStack.add(HabitDetailRoute(it)) },
                        onAddHabit = { backStack.add(AddHabitRoute) },
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
            },
    )
}

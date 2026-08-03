package com.mk.habittracker.feature.addhabit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mk.habittracker.core.ui.ButtonSize
import com.mk.habittracker.core.ui.FullSheet
import com.mk.habittracker.core.ui.HabitButton
import com.mk.habittracker.feature.pairnfc.NfcPairingBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(
    onDismiss: () -> Unit,
    vm: AddHabitViewModel = hiltViewModel(),
) {
    AddHabitScreen(
        habitId = vm.habitId,
        nameTextFieldState = vm.name,
        onSaveHabit = vm::saveHabit,
        onDismiss = onDismiss,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(
    habitId: String,
    nameTextFieldState: TextFieldState,
    onSaveHabit: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val nfcBottomSheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    FullSheet(
        title = "Add a habit",
        onBack = onDismiss,
        modifier = modifier,
        footerContent = {
            HabitButton(
                modifier = Modifier.fillMaxWidth(),
                text = "Save habit",
                onClick = onSaveHabit,
                size = ButtonSize.Medium,
            )
        },
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                state = nameTextFieldState,
                label = { Text("name") },
            )

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showBottomSheet = true },
                shape = RoundedCornerShape(16.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.outline_build_circle_24),
                        contentDescription = "",
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Link NFC tag (optional)")
                        Text(
                            text = "Use a physical tag as a trigger",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Icon(
                        painter = painterResource(R.drawable.outline_add_24),
                        contentDescription = "",
                    )
                }
            }
        }
    }
    if (showBottomSheet) {
        NfcPairingBottomSheet(
            habitId = habitId,
            sheetState = nfcBottomSheetState,
            onDismiss = { showBottomSheet = false },
        )
    }
}

@Composable
@Preview
private fun AddHabitScreenPreview() {
    AddHabitScreen(
        habitId = "",
        nameTextFieldState = TextFieldState("Brush teeth"),
        onDismiss = {},
        onSaveHabit = {},
    )
}

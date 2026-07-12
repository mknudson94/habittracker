package mk.habittracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.habittracker.R

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
) {
    val nfcBottomSheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    FullSheet(
        title = "Add a habit",
        onBack = onDismiss,
        footerContent = {
            Button(
                modifier = Modifier.fillMaxWidth(),
                text = "Save habit",
                onClick = onSaveHabit,
                size = ButtonSize.Medium,
            )
        }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                state = nameTextFieldState,
                label = { Text("name") },
            )

            // TODO: hide this when device not NFC capable
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showBottomSheet = true }
            ) {
                Icon(
                    painter = painterResource(R.drawable.outline_build_circle_24),
                    contentDescription = "",
                )
                Spacer(Modifier.width(8.dp))
                Text("Link NFC tag (optional)")
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
@Preview(showBackground = true)
private fun AddHabitScreenPreview() {
    AddHabitScreen(
        habitId = "",
        nameTextFieldState = TextFieldState("Brush teeth"),
        onDismiss = {},
        onSaveHabit = {},
    )
}

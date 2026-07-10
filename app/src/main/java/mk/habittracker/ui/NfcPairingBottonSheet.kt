package mk.habittracker.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.habittracker.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NfcPairingBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    vm: PairNewNfcTagViewModel = hiltViewModel(),
) {
    val state by vm.pairingState.collectAsStateWithLifecycle()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (state) {
                PairNfcTagState.ReadyToScan -> {
                    Icon(
                        painter = painterResource(R.drawable.outline_info_24),
                        contentDescription = "",
                    )
                    Text("Ready to scan", style = MaterialTheme.typography.displaySmall)
                    Text("Hold your phone near the NFC tag")
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
                is PairNfcTagState.ConfirmOverwrite -> {
                    Icon(
                        painter = painterResource(R.drawable.outline_warning_24),
                        contentDescription = "",
                    )
                    Text("Caution: this will overwrite the current contents of the tag. Are you sure?", style = MaterialTheme.typography.displaySmall)
                    if ((state as PairNfcTagState.ConfirmOverwrite).confirmed) {
                        Text("Hold your phone near the NFC tag")
                    } else {
                        TextButton(vm::confirmOverwrite) {
                            Text("Yes")
                        }
                    }
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
                PairNfcTagState.Success -> {
                    Text("success")
                }
                is PairNfcTagState.Error -> {
                    Text("error: ${(state as PairNfcTagState.Error).message}")
                }
                PairNfcTagState.Idle -> { Text("idle") }
            }
        }
    }
}

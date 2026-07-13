package com.mk.habittracker.ui

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
import com.mk.habittracker.R

@Suppress("ktlint:compose:vm-injection-check")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NfcPairingBottomSheet(
    habitId: String,
    sheetState: SheetState,
    onDismiss: () -> Unit,
) {
    val vm =
        hiltViewModel<NfcPairingViewModel, NfcPairingViewModel.Factory> { factory ->
            factory.create(habitId)
        }
    val pairingState by vm.pairingState.collectAsStateWithLifecycle()
    NfcPairingBottomSheet(
        pairingState = pairingState,
        sheetState = sheetState,
        onConfirmOverwrite = vm::confirmOverwrite,
        onDismiss = onDismiss,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NfcPairingBottomSheet(
    pairingState: PairNfcTagState,
    sheetState: SheetState,
    onConfirmOverwrite: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (pairingState) {
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
                    Text(
                        "Caution: this will overwrite the current contents of the tag. " +
                            "Are you sure?",
                        style = MaterialTheme.typography.displaySmall,
                    )
                    if (pairingState.confirmed) {
                        Text("Hold your phone near the NFC tag")
                    } else {
                        TextButton(onConfirmOverwrite) {
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
                    Text("error: ${pairingState.message}")
                }
            }
        }
    }
}

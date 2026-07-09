package mk.habittracker.ui

import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(
    onDismiss: () -> Unit
) {
    val nfcBottomSheetState = rememberModalBottomSheetState()
    // TODO: use scope to close sheet state - https://developer.android.com/develop/ui/compose/components/bottom-sheets#control-sheet-state
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    FullSheet(
        title = "Add a habit",
        onBack = onDismiss,
    ) {
        Column {
            Text("Add habit", style = MaterialTheme.typography.displayMedium)
        }
        // TODO: hide this when device not NFC capable
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { showBottomSheet = true}
        ) {
            Icon(
                imageVector = Icons.Default.Build,
                contentDescription = "",
            )
            Text("Link NFC tag (optional)")
        }
    }
    if (showBottomSheet) {
        NfcBottomSheet(
            sheetState = nfcBottomSheetState,
            onDismiss = { showBottomSheet = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullSheet(
    title: String,
    onBack: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "back",
                        )
                    }
                },
                expandedHeight = TopAppBarDefaults.TopAppBarExpandedHeight
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            content()
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NfcBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
) {
    val vm = hiltViewModel<AddHabitViewModel>()
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
                        imageVector = Icons.Default.Info, contentDescription = ""
                    )
                    Text("Ready to scan", style = MaterialTheme.typography.displaySmall)
                    Text("Hold your phone near the NFC tag")
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
                is PairNfcTagState.ConfirmOverwrite -> {
                    Icon(
                        imageVector = Icons.Default.Warning, contentDescription = ""
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

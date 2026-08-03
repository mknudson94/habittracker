package com.mk.habittracker.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.TextObfuscationMode.Companion.RevealLastTyped
import androidx.compose.foundation.text.input.TextObfuscationMode.Companion.Visible
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecureTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch

@Composable
fun SignupScreen(
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    SignupScreen(
        onCancel = onCancel,
        onSignup = viewModel::signUp,
        modifier = modifier,
    )
}

@Composable
fun SignupScreen(
    onCancel: () -> Unit,
    onSignup: (email: String, password: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val email = TextFieldState("")
    val password = TextFieldState("")
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Create an account",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Start tracking your daily routines",
                style = MaterialTheme.typography.titleMedium
            )
            TextField(
                modifier = Modifier.fillMaxWidth(),
                state = email,
                label = { Text("email") },
                placeholder = { Text("you@example.com") },
            )
            PasswordField(
                modifier = Modifier.fillMaxWidth(),
                password = password,
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    scope.launch {
                        onSignup(email.text.toString(), password.text.toString())
                    }
                }
            ) {
                Text("Create account")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HorizontalDivider(Modifier.weight(1f))
                Text("or")
                HorizontalDivider(Modifier.weight(1f))
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {}
            ) {
                Text("Continue with Google")
            }
            val annotatedText = buildAnnotatedString {
                append("Already have an account?  ")

                pushLink(
                    LinkAnnotation.Clickable(
                        tag = "Sign in",
                        linkInteractionListener = {
                            onCancel()
                        },
                    )
                )
                pushStyle(
                    SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    )
                )
                append(text = "Sign in")
            }
            Text(annotatedText)
        }
    }
}

@Composable
private fun PasswordField(
    password: TextFieldState,
    modifier: Modifier = Modifier,
) {
    var isRevealed by remember { mutableStateOf(false) }
    SecureTextField(
        modifier = modifier,
        state = password,
        textObfuscationMode = if (isRevealed) Visible else RevealLastTyped,
        label = { Text("password") },
        placeholder = { Text("••••••••") },
        trailingIcon = {
            IconButton(
                onClick = { isRevealed = !isRevealed }
            ) {
                if (isRevealed) {
                    Icon(
                        painter = painterResource(R.drawable.eye_closed),
                        contentDescription = "hide password"
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.eye),
                        contentDescription = "reveal password"
                    )
                }
            }
        }
    )
}

@Composable
@Preview(showBackground = true, device = Devices.PHONE)
private fun SignupScreenPreview() {
    SignupScreen(
        onCancel = {},
        onSignup = {_, _ -> },
    )
}

package com.mk.habittracker.feature.auth

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecureTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onSignupClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    LoginScreen(
        onSignupClick = onSignupClick,
        onSignInWithGoogle = viewModel::signInWithGoogle,
        onSignIn = viewModel::signIn,
        modifier = modifier,
    )

}

@Composable
fun LoginScreen(
    onSignupClick: () -> Unit,
    onSignInWithGoogle: (Context) -> Unit,
    onSignIn: (email: String, password: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val email = TextFieldState("")
    val password = TextFieldState("")
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(80.dp))
            Text("Welcome back", style = MaterialTheme.typography.displayMedium)
            Text("Sign in to sync your habits", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(24.dp))
            TextField(
                state = email,
                label = { Text("email") },
                placeholder = { Text("you@example.com") },
            )
            SecureTextField(
                state = password,
                label = { Text("password") },
                placeholder = { Text("password") },
            )
            Spacer(Modifier.height(12.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    scope.launch {
                        onSignIn(email.text.toString(), password.text.toString())
                    }
                }
            ) {
                Text("Sign in")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HorizontalDivider(Modifier.weight(1f))
                Text("or", modifier = Modifier.padding(horizontal = 8.dp))
                HorizontalDivider(Modifier.weight(1f))
            }
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onSignInWithGoogle(context) }
            ) {
                Icon(
                    painter = painterResource(R.drawable.google),
                    contentDescription = "google logo",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Continue with Google")
            }
            val annotatedText = buildAnnotatedString {
                append("Don't have an account?  ")

                pushLink(
                    LinkAnnotation.Clickable(
                        tag = "Sign up",
                        linkInteractionListener = {
                            onSignupClick()
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
                append(text = "Sign up")
            }
            Text(annotatedText)
        }
    }

}

@Composable
@Preview(showBackground = true)
private fun LoginScreenPreview() {
    Box(Modifier.padding(16.dp)) {
        LoginScreen(
            onSignupClick = {},
            onSignInWithGoogle = { _ -> },
            onSignIn = { _, _ -> },
        )
    }
}

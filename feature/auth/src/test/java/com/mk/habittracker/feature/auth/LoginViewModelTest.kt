package com.mk.habittracker.feature.auth

import android.content.Context
import app.cash.turbine.test
import com.google.android.gms.tasks.Tasks
import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class LoginViewModelTest {
    private val auth: FirebaseAuth = mockk(relaxed = true)
    private val googleSignInManager: GoogleSignInManager = mockk(relaxed = true)
    private val firebaseUser: FirebaseUser = mockk()
    private val authResult: AuthResult = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { auth.currentUser } returns null
        every { authResult.user } returns firebaseUser

        viewModel = LoginViewModel(auth, googleSignInManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial authState is null`() = runTest {
        viewModel.authState.test {
            assertThat(awaitItem()).isNull()
        }
    }

    @Test
    fun `authState updates when Firebase auth listener triggers`() = runTest {
        val listenerSlot = slot<FirebaseAuth.AuthStateListener>()
        verify { auth.addAuthStateListener(capture(listenerSlot)) }

        viewModel.authState.test {
            assertThat(awaitItem()).isNull() // initial

            every { auth.currentUser } returns firebaseUser
            listenerSlot.captured.onAuthStateChanged(auth)

            assertThat(awaitItem()).isEqualTo(firebaseUser)
        }
    }

    @Test
    fun `signIn updates authState on success`() = runTest {
        val email = "test@example.com"
        val password = "password"

        every {
            auth.signInWithEmailAndPassword(email, password)
        } returns Tasks.forResult(authResult)

        viewModel.authState.test {
            assertThat(awaitItem()).isNull() // Initial value
            viewModel.signIn(email, password)
            assertThat(awaitItem()).isEqualTo(firebaseUser)
        }
    }

    @Test
    fun `signUp updates authState on success`() = runTest {
        val email = "test@example.com"
        val password = "password"

        every {
            auth.createUserWithEmailAndPassword(email, password)
        } returns Tasks.forResult(authResult)

        viewModel.authState.test {
            // Initial value
            assertThat(awaitItem()).isNull()
            viewModel.signUp(email, password)
            assertThat(awaitItem()).isEqualTo(firebaseUser)
        }
    }

    @Test
    fun `signOut calls auth signOut and clears google credentials`() = runTest {
        every { auth.currentUser } returns firebaseUser
        // Re-create to pick up the user
        viewModel = LoginViewModel(auth, googleSignInManager)

        viewModel.signOut()

        verify { auth.signOut() }
        coVerify { googleSignInManager.clearCredentials() }
    }

    @Test
    fun `signInWithGoogle updates authState on success`() = runTest {
        mockkStatic(GoogleAuthProvider::class)
        val context: Context = mockk()
        val idToken = "id-token"
        val credential = mockk<com.google.firebase.auth.AuthCredential>()

        coEvery {
            googleSignInManager.requestGoogleCredential(any(), any(), any())
        } returns idToken
        every {
            GoogleAuthProvider.getCredential(idToken, null)
        } returns credential
        every {
            auth.signInWithCredential(credential)
        } returns Tasks.forResult(authResult)

        viewModel.authState.test {
            assertThat(awaitItem()).isNull()
            viewModel.signInWithGoogle(context)
            assertThat(awaitItem()).isEqualTo(firebaseUser)
        }
    }
}

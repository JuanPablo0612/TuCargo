package com.juanpablo0612.tucargo.features.auth.presentation.login

import androidx.compose.foundation.text.input.insert
import com.juanpablo0612.tucargo.domain.model.AppError
import com.juanpablo0612.tucargo.domain.model.User
import com.juanpablo0612.tucargo.domain.model.UserRole
import com.juanpablo0612.tucargo.domain.usecase.LoginUseCase
import com.juanpablo0612.tucargo.testutil.FakeAuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel
    private lateinit var repository: FakeAuthRepository
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeAuthRepository()
        viewModel = LoginViewModel(LoginUseCase(repository))
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun login_success_updatesState() = runTest {
        repository.loginResult = Result.success(User(id = "123", role = UserRole.DRIVER))
        
        viewModel.emailState.edit { insert(0, "test@example.com") }
        viewModel.passwordState.edit { insert(0, "password123") }
        
        viewModel.onAction(LoginAction.Login)
        
        assertTrue(viewModel.uiState.value.isLoading)
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(UserRole.DRIVER, viewModel.uiState.value.successRole)
        assertNull(viewModel.uiState.value.authError)
    }

    @Test
    fun login_failure_updatesError() = runTest {
        repository.loginResult = Result.failure(AppError.Auth.InvalidCredentials)
        
        viewModel.emailState.edit { insert(0, "test@example.com") }
        viewModel.passwordState.edit { insert(0, "wrong") }
        
        viewModel.onAction(LoginAction.Login)
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(LoginError.InvalidCredentials, viewModel.uiState.value.authError)
        assertNull(viewModel.uiState.value.successRole)
    }
}

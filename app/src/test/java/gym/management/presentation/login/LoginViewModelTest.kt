package gym.management.presentation.login

import gym.management.domain.model.User
import gym.management.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk()
        viewModel = LoginViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `estado inicial e Idle`() {
        assertEquals(LoginUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `login com sucesso emite Success`() = runTest {
        coEvery { authRepository.login(any(), any()) } returns Result.success(User("uid", "test@test.com"))

        viewModel.login("test@test.com", "senha123")

        assertEquals(LoginUiState.Success, viewModel.uiState.value)
    }

    @Test
    fun `login com falha emite Error com mensagem`() = runTest {
        coEvery { authRepository.login(any(), any()) } returns Result.failure(Exception("Credenciais inválidas"))

        viewModel.login("test@test.com", "senhaerrada")

        assertEquals(LoginUiState.Error("Credenciais inválidas"), viewModel.uiState.value)
    }

    @Test
    fun `login com falha sem mensagem emite erro padrao`() = runTest {
        coEvery { authRepository.login(any(), any()) } returns Result.failure(Exception())

        viewModel.login("test@test.com", "senha")

        assertEquals(LoginUiState.Error("Erro desconhecido"), viewModel.uiState.value)
    }

    @Test
    fun `resetState volta para Idle`() = runTest {
        coEvery { authRepository.login(any(), any()) } returns Result.success(User("uid", "test@test.com"))
        viewModel.login("test@test.com", "senha123")

        viewModel.resetState()

        assertEquals(LoginUiState.Idle, viewModel.uiState.value)
    }
}

package gym.management.presentation.register

import gym.management.domain.model.User
import gym.management.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
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
class RegisterViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk()
        viewModel = RegisterViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `estado inicial e Idle`() {
        assertEquals(RegisterUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `senhas diferentes emitem Error sem chamar repositorio`() = runTest {
        viewModel.register("test@test.com", "senha1", "senha2")

        assertEquals(RegisterUiState.Error("As senhas não coincidem"), viewModel.uiState.value)
        coVerify(exactly = 0) { authRepository.register(any(), any()) }
    }

    @Test
    fun `registro com sucesso emite Success`() = runTest {
        coEvery { authRepository.register(any(), any()) } returns Result.success(User("uid", "test@test.com"))

        viewModel.register("test@test.com", "senha123", "senha123")

        assertEquals(RegisterUiState.Success, viewModel.uiState.value)
    }

    @Test
    fun `registro com falha emite Error com mensagem do repositorio`() = runTest {
        coEvery { authRepository.register(any(), any()) } returns Result.failure(Exception("Email já em uso"))

        viewModel.register("test@test.com", "senha123", "senha123")

        assertEquals(RegisterUiState.Error("Email já em uso"), viewModel.uiState.value)
    }

    @Test
    fun `registro com falha sem mensagem emite erro padrao`() = runTest {
        coEvery { authRepository.register(any(), any()) } returns Result.failure(Exception())

        viewModel.register("test@test.com", "senha", "senha")

        assertEquals(RegisterUiState.Error("Erro ao criar conta"), viewModel.uiState.value)
    }

    @Test
    fun `resetState volta para Idle`() = runTest {
        coEvery { authRepository.register(any(), any()) } returns Result.failure(Exception("Erro"))
        viewModel.register("test@test.com", "senha", "senha")

        viewModel.resetState()

        assertEquals(RegisterUiState.Idle, viewModel.uiState.value)
    }
}

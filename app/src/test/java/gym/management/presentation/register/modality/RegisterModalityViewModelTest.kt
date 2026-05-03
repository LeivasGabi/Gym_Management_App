package gym.management.presentation.register.modality

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import gym.management.domain.model.Modality
import gym.management.domain.repository.ModalityRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterModalityViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var modalityRepository: ModalityRepository
    private lateinit var viewModel: RegisterModalityViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(FirebaseAuth::class)
        val mockAuth = mockk<FirebaseAuth>()
        val mockUser = mockk<FirebaseUser>()
        every { FirebaseAuth.getInstance() } returns mockAuth
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "test-uid"

        modalityRepository = mockk()
        viewModel = RegisterModalityViewModel(modalityRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(FirebaseAuth::class)
    }

    @Test
    fun `estado inicial e Idle`() {
        assertEquals(RegisterModalityUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `saveModality emite Success quando nao ha conflitos`() = runTest {
        every { modalityRepository.observeAll() } returns flowOf(emptyList())
        coEvery { modalityRepository.save(any()) } returns Result.success(Modality(id = "new"))

        viewModel.saveModality("Jiu-Jitsu", listOf("08:00"), "150,00", "Segunda")

        assertEquals(RegisterModalityUiState.Success, viewModel.uiState.value)
    }

    @Test
    fun `saveModality emite Conflict quando horario e dia conflitam com existente`() = runTest {
        val existing = Modality(id = "1", name = "Jiu-Jitsu", schedules = listOf("08:00"), frequency = "Segunda")
        every { modalityRepository.observeAll() } returns flowOf(listOf(existing))

        viewModel.saveModality("Outra Modalidade", listOf("08:00"), "100,00", "Segunda")

        val state = viewModel.uiState.value
        assertTrue(state is RegisterModalityUiState.Conflict)
        assertEquals(listOf("Jiu-Jitsu"), (state as RegisterModalityUiState.Conflict).conflictingNames)
    }

    @Test
    fun `saveModality nao conflita quando dias sao diferentes`() = runTest {
        val existing = Modality(id = "1", name = "Jiu-Jitsu", schedules = listOf("08:00"), frequency = "Segunda")
        every { modalityRepository.observeAll() } returns flowOf(listOf(existing))
        coEvery { modalityRepository.save(any()) } returns Result.success(Modality(id = "new"))

        viewModel.saveModality("Boxe", listOf("08:00"), "100,00", "Terça")

        assertEquals(RegisterModalityUiState.Success, viewModel.uiState.value)
    }

    @Test
    fun `saveModality nao conflita quando horarios sao diferentes`() = runTest {
        val existing = Modality(id = "1", name = "Jiu-Jitsu", schedules = listOf("08:00"), frequency = "Segunda")
        every { modalityRepository.observeAll() } returns flowOf(listOf(existing))
        coEvery { modalityRepository.save(any()) } returns Result.success(Modality(id = "new"))

        viewModel.saveModality("Jiu-Jitsu Avançado", listOf("10:00"), "100,00", "Segunda")

        assertEquals(RegisterModalityUiState.Success, viewModel.uiState.value)
    }

    @Test
    fun `saveModality emite Error quando repositorio falha`() = runTest {
        every { modalityRepository.observeAll() } returns flowOf(emptyList())
        coEvery { modalityRepository.save(any()) } returns Result.failure(Exception("Falha no servidor"))

        viewModel.saveModality("Boxe", listOf("10:00"), "120,00", "Terça")

        assertEquals(RegisterModalityUiState.Error("Falha no servidor"), viewModel.uiState.value)
    }

    @Test
    fun `preco com virgula e convertido para ponto`() = runTest {
        every { modalityRepository.observeAll() } returns flowOf(emptyList())
        var savedModality: Modality? = null
        coEvery { modalityRepository.save(any()) } answers {
            savedModality = firstArg()
            Result.success(firstArg())
        }

        viewModel.saveModality("Boxe", listOf("10:00"), "150,50", "Terça")

        assertEquals(150.50, savedModality?.price)
    }

    @Test
    fun `resetState volta para Idle`() = runTest {
        every { modalityRepository.observeAll() } returns flowOf(emptyList())
        coEvery { modalityRepository.save(any()) } returns Result.success(Modality())
        viewModel.saveModality("Boxe", listOf("10:00"), "120,00", "Terça")

        viewModel.resetState()

        assertEquals(RegisterModalityUiState.Idle, viewModel.uiState.value)
    }
}

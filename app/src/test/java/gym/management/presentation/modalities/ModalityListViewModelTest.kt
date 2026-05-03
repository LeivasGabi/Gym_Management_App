package gym.management.presentation.modalities

import gym.management.domain.model.Modality
import gym.management.domain.repository.ModalityRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ModalityListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `estado inicial e Loading antes de emitir`() {
        val repository = mockk<ModalityRepository>()
        every { repository.observeAll() } returns flowOf()
        val viewModel = ModalityListViewModel(repository)

        assertEquals(ModalityListUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `emite Success com lista de modalidades`() = runTest {
        val modalities = listOf(
            Modality(id = "1", name = "Jiu-Jitsu"),
            Modality(id = "2", name = "Boxe")
        )
        val repository = mockk<ModalityRepository>()
        every { repository.observeAll() } returns flowOf(modalities)
        val viewModel = ModalityListViewModel(repository)

        assertEquals(ModalityListUiState.Success(modalities), viewModel.uiState.value)
    }

    @Test
    fun `emite Success com lista vazia`() = runTest {
        val repository = mockk<ModalityRepository>()
        every { repository.observeAll() } returns flowOf(emptyList())
        val viewModel = ModalityListViewModel(repository)

        assertEquals(ModalityListUiState.Success(emptyList()), viewModel.uiState.value)
    }

    @Test
    fun `emite Error quando repositorio lanca excecao`() = runTest {
        val repository = mockk<ModalityRepository>()
        every { repository.observeAll() } returns flow { throw Exception("Erro de rede") }
        val viewModel = ModalityListViewModel(repository)

        assertEquals(ModalityListUiState.Error("Erro de rede"), viewModel.uiState.value)
    }

    @Test
    fun `emite erro padrao quando excecao sem mensagem`() = runTest {
        val repository = mockk<ModalityRepository>()
        every { repository.observeAll() } returns flow { throw Exception() }
        val viewModel = ModalityListViewModel(repository)

        assertEquals(ModalityListUiState.Error("Erro desconhecido"), viewModel.uiState.value)
    }
}

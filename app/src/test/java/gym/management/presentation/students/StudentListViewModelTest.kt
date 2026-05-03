package gym.management.presentation.students

import gym.management.domain.model.Modality
import gym.management.domain.model.Student
import gym.management.domain.repository.ModalityRepository
import gym.management.domain.repository.StudentRepository
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
class StudentListViewModelTest {

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
    fun `emite Success com nomes das modalidades mapeados`() = runTest {
        val modalities = listOf(
            Modality(id = "mod1", name = "Jiu-Jitsu"),
            Modality(id = "mod2", name = "Boxe")
        )
        val students = listOf(
            Student(id = "s1", name = "Ana", modalityIds = listOf("mod1", "mod2")),
            Student(id = "s2", name = "Pedro", modalityIds = listOf("mod1"))
        )
        val studentRepository = mockk<StudentRepository>()
        val modalityRepository = mockk<ModalityRepository>()
        every { studentRepository.observeAll() } returns flowOf(students)
        every { modalityRepository.observeAll() } returns flowOf(modalities)

        val viewModel = StudentListViewModel(studentRepository, modalityRepository)

        val state = viewModel.uiState.value as StudentListUiState.Success
        assertEquals(2, state.students.size)
        assertEquals(listOf("Jiu-Jitsu", "Boxe"), state.students[0].modalityNames)
        assertEquals(listOf("Jiu-Jitsu"), state.students[1].modalityNames)
    }

    @Test
    fun `modalidade desconhecida e ignorada no mapeamento`() = runTest {
        val modalities = listOf(Modality(id = "mod1", name = "Jiu-Jitsu"))
        val students = listOf(Student(id = "s1", name = "Ana", modalityIds = listOf("mod1", "mod_inexistente")))
        val studentRepository = mockk<StudentRepository>()
        val modalityRepository = mockk<ModalityRepository>()
        every { studentRepository.observeAll() } returns flowOf(students)
        every { modalityRepository.observeAll() } returns flowOf(modalities)

        val viewModel = StudentListViewModel(studentRepository, modalityRepository)

        val state = viewModel.uiState.value as StudentListUiState.Success
        assertEquals(listOf("Jiu-Jitsu"), state.students[0].modalityNames)
    }

    @Test
    fun `emite Success com lista vazia quando sem alunos`() = runTest {
        val studentRepository = mockk<StudentRepository>()
        val modalityRepository = mockk<ModalityRepository>()
        every { studentRepository.observeAll() } returns flowOf(emptyList())
        every { modalityRepository.observeAll() } returns flowOf(emptyList())

        val viewModel = StudentListViewModel(studentRepository, modalityRepository)

        assertEquals(StudentListUiState.Success(emptyList()), viewModel.uiState.value)
    }

    @Test
    fun `emite Error quando repositorio de alunos lanca excecao`() = runTest {
        val studentRepository = mockk<StudentRepository>()
        val modalityRepository = mockk<ModalityRepository>()
        every { studentRepository.observeAll() } returns flow { throw Exception("Falha na conexão") }
        every { modalityRepository.observeAll() } returns flowOf(emptyList())

        val viewModel = StudentListViewModel(studentRepository, modalityRepository)

        assertEquals(StudentListUiState.Error("Falha na conexão"), viewModel.uiState.value)
    }

    @Test
    fun `emite Error quando repositorio de modalidades lanca excecao`() = runTest {
        val studentRepository = mockk<StudentRepository>()
        val modalityRepository = mockk<ModalityRepository>()
        every { studentRepository.observeAll() } returns flowOf(emptyList())
        every { modalityRepository.observeAll() } returns flow { throw Exception("Timeout") }

        val viewModel = StudentListViewModel(studentRepository, modalityRepository)

        assertEquals(StudentListUiState.Error("Timeout"), viewModel.uiState.value)
    }
}

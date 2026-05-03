package gym.management.presentation.modalities

import gym.management.domain.model.Graduation
import gym.management.domain.model.Modality
import gym.management.domain.model.Student
import gym.management.domain.repository.GraduationRepository
import gym.management.domain.repository.ModalityRepository
import gym.management.domain.repository.StudentRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ModalityStudentsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var studentRepository: StudentRepository
    private lateinit var graduationRepository: GraduationRepository
    private lateinit var modalityRepository: ModalityRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        studentRepository = mockk()
        graduationRepository = mockk()
        modalityRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(modalityId: String = "mod1") = ModalityStudentsViewModel(
        modalityId, studentRepository, graduationRepository, modalityRepository
    )

    @Test
    fun `init emite Success com alunos ativos da modalidade`() = runTest {
        val modality = Modality(id = "mod1", name = "Jiu-Jitsu")
        val students = listOf(
            Student(id = "s1", name = "Ana", active = true, modalityIds = listOf("mod1")),
            Student(id = "s2", name = "Pedro", active = true, modalityIds = listOf("mod1")),
            Student(id = "s3", name = "João", active = false, modalityIds = listOf("mod1"))
        )
        every { modalityRepository.observeAll() } returns flowOf(listOf(modality))
        every { studentRepository.observeAll() } returns flowOf(students)
        every { graduationRepository.observeByModality("mod1") } returns flowOf(emptyList())

        val viewModel = createViewModel()

        val state = viewModel.uiState.value as ModalityStudentsUiState.Success
        assertEquals(2, state.students.size)
        assertEquals("Ana", state.students[0].student.name)
        assertEquals("Pedro", state.students[1].student.name)
    }

    @Test
    fun `init emite Error quando modalidade nao encontrada`() = runTest {
        every { modalityRepository.observeAll() } returns flowOf(emptyList())
        every { studentRepository.observeAll() } returns flowOf(emptyList())
        every { graduationRepository.observeByModality("mod1") } returns flowOf(emptyList())

        val viewModel = createViewModel()

        assertEquals(ModalityStudentsUiState.Error("Modalidade não encontrada"), viewModel.uiState.value)
    }

    @Test
    fun `init mapeia ultima graduacao de cada aluno`() = runTest {
        val modality = Modality(id = "mod1", name = "Jiu-Jitsu")
        val student = Student(id = "s1", name = "Ana", active = true, modalityIds = listOf("mod1"))
        val graduations = listOf(
            Graduation(id = "g1", studentId = "s1", modalityId = "mod1", belt = "Branca", date = 1000L),
            Graduation(id = "g2", studentId = "s1", modalityId = "mod1", belt = "Azul", date = 3000L)
        )
        every { modalityRepository.observeAll() } returns flowOf(listOf(modality))
        every { studentRepository.observeAll() } returns flowOf(listOf(student))
        every { graduationRepository.observeByModality("mod1") } returns flowOf(graduations)

        val viewModel = createViewModel()

        val state = viewModel.uiState.value as ModalityStudentsUiState.Success
        assertEquals("Azul", state.students[0].latestBelt)
    }

    @Test
    fun `aluno sem graduacao tem latestBelt nulo`() = runTest {
        val modality = Modality(id = "mod1", name = "Jiu-Jitsu")
        val student = Student(id = "s1", name = "Ana", active = true, modalityIds = listOf("mod1"))
        every { modalityRepository.observeAll() } returns flowOf(listOf(modality))
        every { studentRepository.observeAll() } returns flowOf(listOf(student))
        every { graduationRepository.observeByModality("mod1") } returns flowOf(emptyList())

        val viewModel = createViewModel()

        val state = viewModel.uiState.value as ModalityStudentsUiState.Success
        assertNull(state.students[0].latestBelt)
    }

    @Test
    fun `updateModality emite Success quando repositorio salva`() = runTest {
        val modality = Modality(id = "mod1", name = "Jiu-Jitsu", price = 150.0)
        every { modalityRepository.observeAll() } returns flowOf(listOf(modality))
        every { studentRepository.observeAll() } returns flowOf(emptyList())
        every { graduationRepository.observeByModality("mod1") } returns flowOf(emptyList())
        coEvery { modalityRepository.update(any()) } returns Result.success(Unit)

        val viewModel = createViewModel()
        viewModel.updateModality("Jiu-Jitsu Avançado", listOf("10:00"), "200,00", "Segunda, Quarta", true)

        assertEquals(ModalityEditSaveState.Success, viewModel.editSaveState.value)
    }

    @Test
    fun `updateModality emite Error quando repositorio falha`() = runTest {
        val modality = Modality(id = "mod1", name = "Jiu-Jitsu")
        every { modalityRepository.observeAll() } returns flowOf(listOf(modality))
        every { studentRepository.observeAll() } returns flowOf(emptyList())
        every { graduationRepository.observeByModality("mod1") } returns flowOf(emptyList())
        coEvery { modalityRepository.update(any()) } returns Result.failure(Exception("Erro ao atualizar"))

        val viewModel = createViewModel()
        viewModel.updateModality("Jiu-Jitsu", listOf("08:00"), "150,00", "Segunda", true)

        assertEquals(ModalityEditSaveState.Error("Erro ao atualizar"), viewModel.editSaveState.value)
    }

    @Test
    fun `updateModality nao faz nada quando uiState nao e Success`() = runTest {
        every { modalityRepository.observeAll() } returns flowOf(emptyList())
        every { studentRepository.observeAll() } returns flowOf(emptyList())
        every { graduationRepository.observeByModality("mod1") } returns flowOf(emptyList())

        val viewModel = createViewModel()
        viewModel.updateModality("Teste", listOf(), "100,00", "Segunda", true)

        assertEquals(ModalityEditSaveState.Idle, viewModel.editSaveState.value)
    }

    @Test
    fun `deleteModality emite Success quando repositorio deleta`() = runTest {
        val modality = Modality(id = "mod1", name = "Jiu-Jitsu")
        every { modalityRepository.observeAll() } returns flowOf(listOf(modality))
        every { studentRepository.observeAll() } returns flowOf(emptyList())
        every { graduationRepository.observeByModality("mod1") } returns flowOf(emptyList())
        coEvery { modalityRepository.delete("mod1") } returns Result.success(Unit)

        val viewModel = createViewModel()
        viewModel.deleteModality()

        assertEquals(ModalityDeleteState.Success, viewModel.deleteState.value)
    }

    @Test
    fun `deleteModality emite Error quando repositorio falha`() = runTest {
        val modality = Modality(id = "mod1", name = "Jiu-Jitsu")
        every { modalityRepository.observeAll() } returns flowOf(listOf(modality))
        every { studentRepository.observeAll() } returns flowOf(emptyList())
        every { graduationRepository.observeByModality("mod1") } returns flowOf(emptyList())
        coEvery { modalityRepository.delete("mod1") } returns Result.failure(Exception("Erro ao excluir"))

        val viewModel = createViewModel()
        viewModel.deleteModality()

        assertEquals(ModalityDeleteState.Error("Erro ao excluir"), viewModel.deleteState.value)
    }

    @Test
    fun `resetEditState volta para Idle`() = runTest {
        val modality = Modality(id = "mod1", name = "Jiu-Jitsu")
        every { modalityRepository.observeAll() } returns flowOf(listOf(modality))
        every { studentRepository.observeAll() } returns flowOf(emptyList())
        every { graduationRepository.observeByModality("mod1") } returns flowOf(emptyList())
        coEvery { modalityRepository.update(any()) } returns Result.success(Unit)

        val viewModel = createViewModel()
        viewModel.updateModality("Jiu-Jitsu", listOf("08:00"), "150,00", "Segunda", true)
        viewModel.resetEditState()

        assertEquals(ModalityEditSaveState.Idle, viewModel.editSaveState.value)
    }

    @Test
    fun `resetDeleteState volta para Idle`() = runTest {
        val modality = Modality(id = "mod1", name = "Jiu-Jitsu")
        every { modalityRepository.observeAll() } returns flowOf(listOf(modality))
        every { studentRepository.observeAll() } returns flowOf(emptyList())
        every { graduationRepository.observeByModality("mod1") } returns flowOf(emptyList())
        coEvery { modalityRepository.delete("mod1") } returns Result.success(Unit)

        val viewModel = createViewModel()
        viewModel.deleteModality()
        viewModel.resetDeleteState()

        assertEquals(ModalityDeleteState.Idle, viewModel.deleteState.value)
    }
}

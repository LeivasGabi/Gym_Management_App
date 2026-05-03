package gym.management.presentation.graduation

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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GraduationViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var graduationRepository: GraduationRepository
    private lateinit var studentRepository: StudentRepository
    private lateinit var modalityRepository: ModalityRepository
    private lateinit var viewModel: GraduationViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        graduationRepository = mockk()
        studentRepository = mockk()
        modalityRepository = mockk()
        viewModel = GraduationViewModel(graduationRepository, studentRepository, modalityRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `estado inicial e Loading`() {
        assertEquals(GraduationUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `estado inicial de saveState e Idle`() {
        assertEquals(GraduationSaveState.Idle, viewModel.saveState.value)
    }

    @Test
    fun `load emite Success com grupos por modalidade`() = runTest {
        val student = Student(id = "s1", name = "Ana", modalityIds = listOf("mod1", "mod2"))
        val modalities = listOf(
            Modality(id = "mod1", name = "Jiu-Jitsu"),
            Modality(id = "mod2", name = "Boxe")
        )
        val graduations = listOf(
            Graduation(id = "g1", studentId = "s1", modalityId = "mod1", belt = "Azul")
        )
        coEvery { studentRepository.getById("s1") } returns Result.success(student)
        every { graduationRepository.observeByStudent("s1") } returns flowOf(graduations)
        every { modalityRepository.observeAll() } returns flowOf(modalities)

        viewModel.load("s1")

        val state = viewModel.uiState.value as GraduationUiState.Success
        assertEquals("Ana", state.studentName)
        assertEquals(2, state.groups.size)
        assertEquals(1, state.groups.find { it.modality.id == "mod1" }?.graduations?.size)
        assertEquals(0, state.groups.find { it.modality.id == "mod2" }?.graduations?.size)
    }

    @Test
    fun `load emite Error quando aluno nao encontrado`() = runTest {
        coEvery { studentRepository.getById("s1") } returns Result.failure(Exception("Aluno não encontrado"))

        viewModel.load("s1")

        assertEquals(GraduationUiState.Error("Aluno não encontrado"), viewModel.uiState.value)
    }

    @Test
    fun `load filtra apenas modalidades do aluno`() = runTest {
        val student = Student(id = "s1", name = "Ana", modalityIds = listOf("mod1"))
        val modalities = listOf(
            Modality(id = "mod1", name = "Jiu-Jitsu"),
            Modality(id = "mod2", name = "Boxe")
        )
        coEvery { studentRepository.getById("s1") } returns Result.success(student)
        every { graduationRepository.observeByStudent("s1") } returns flowOf(emptyList())
        every { modalityRepository.observeAll() } returns flowOf(modalities)

        viewModel.load("s1")

        val state = viewModel.uiState.value as GraduationUiState.Success
        assertEquals(1, state.groups.size)
        assertEquals("mod1", state.groups[0].modality.id)
    }

    @Test
    fun `graduacoes sao ordenadas por data decrescente`() = runTest {
        val student = Student(id = "s1", name = "Ana", modalityIds = listOf("mod1"))
        val modalities = listOf(Modality(id = "mod1", name = "Jiu-Jitsu"))
        val graduations = listOf(
            Graduation(id = "g1", studentId = "s1", modalityId = "mod1", belt = "Branca", date = 1000L),
            Graduation(id = "g2", studentId = "s1", modalityId = "mod1", belt = "Azul", date = 3000L),
            Graduation(id = "g3", studentId = "s1", modalityId = "mod1", belt = "Roxa", date = 2000L)
        )
        coEvery { studentRepository.getById("s1") } returns Result.success(student)
        every { graduationRepository.observeByStudent("s1") } returns flowOf(graduations)
        every { modalityRepository.observeAll() } returns flowOf(modalities)

        viewModel.load("s1")

        val state = viewModel.uiState.value as GraduationUiState.Success
        val sorted = state.groups[0].graduations
        assertEquals("Azul", sorted[0].belt)
        assertEquals("Roxa", sorted[1].belt)
        assertEquals("Branca", sorted[2].belt)
    }

    @Test
    fun `addGraduation emite Success quando salvo com sucesso`() = runTest {
        coEvery { graduationRepository.save(any()) } returns Result.success(Graduation(id = "new"))

        viewModel.addGraduation(
            studentId = "s1",
            modalityId = "mod1",
            modalityName = "Jiu-Jitsu",
            belt = "Azul",
            generalGrade = "A",
            observation = "",
            date = 1000L
        )

        assertEquals(GraduationSaveState.Success, viewModel.saveState.value)
    }

    @Test
    fun `addGraduation emite Error quando repositorio falha`() = runTest {
        coEvery { graduationRepository.save(any()) } returns Result.failure(Exception("Falha ao salvar graduação"))

        viewModel.addGraduation("s1", "mod1", "Jiu-Jitsu", "Azul", "A", "", 1000L)

        assertEquals(GraduationSaveState.Error("Falha ao salvar graduação"), viewModel.saveState.value)
    }

    @Test
    fun `updateGraduation emite Success quando atualizado com sucesso`() = runTest {
        val graduation = Graduation(id = "g1", studentId = "s1", belt = "Azul")
        coEvery { graduationRepository.update(graduation) } returns Result.success(Unit)

        viewModel.updateGraduation(graduation)

        assertEquals(GraduationSaveState.Success, viewModel.saveState.value)
    }

    @Test
    fun `updateGraduation emite Error quando repositorio falha`() = runTest {
        val graduation = Graduation(id = "g1")
        coEvery { graduationRepository.update(graduation) } returns Result.failure(Exception("Erro ao atualizar"))

        viewModel.updateGraduation(graduation)

        assertEquals(GraduationSaveState.Error("Erro ao atualizar"), viewModel.saveState.value)
    }

    @Test
    fun `resetSaveState volta para Idle`() = runTest {
        coEvery { graduationRepository.save(any()) } returns Result.success(Graduation(id = "new"))
        viewModel.addGraduation("s1", "mod1", "Jiu-Jitsu", "Azul", "A", "", 1000L)

        viewModel.resetSaveState()

        assertEquals(GraduationSaveState.Idle, viewModel.saveState.value)
    }
}

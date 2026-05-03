package gym.management.presentation.students.detail

import gym.management.domain.model.Modality
import gym.management.domain.model.Student
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StudentProfileViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var studentRepository: StudentRepository
    private lateinit var modalityRepository: ModalityRepository
    private lateinit var viewModel: StudentProfileViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        studentRepository = mockk()
        modalityRepository = mockk()
        viewModel = StudentProfileViewModel(studentRepository, modalityRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `estado inicial de uiState e Loading`() {
        assertEquals(StudentProfileUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `estado inicial de saveState e Idle`() {
        assertEquals(SaveState.Idle, viewModel.saveState.value)
    }

    @Test
    fun `loadStudent emite Success com aluno e modalidades`() = runTest {
        val student = Student(id = "s1", name = "Ana", modalityIds = listOf("mod1"))
        val modalities = listOf(Modality(id = "mod1", name = "Jiu-Jitsu"))
        coEvery { studentRepository.getById("s1") } returns Result.success(student)
        every { modalityRepository.observeAll() } returns flowOf(modalities)

        viewModel.loadStudent("s1")

        val state = viewModel.uiState.value as StudentProfileUiState.Success
        assertEquals(student, state.student)
        assertEquals(modalities, state.availableModalities)
    }

    @Test
    fun `loadStudent emite Error quando aluno nao encontrado`() = runTest {
        coEvery { studentRepository.getById("s1") } returns Result.failure(Exception("Aluno não encontrado"))

        viewModel.loadStudent("s1")

        assertEquals(StudentProfileUiState.Error("Aluno não encontrado"), viewModel.uiState.value)
    }

    @Test
    fun `saveContactInfo emite Success e atualiza aluno no uiState`() = runTest {
        val student = Student(id = "s1", name = "Ana")
        coEvery { studentRepository.getById("s1") } returns Result.success(student)
        every { modalityRepository.observeAll() } returns flowOf(emptyList())
        coEvery { studentRepository.update(any()) } returns Result.success(Unit)
        viewModel.loadStudent("s1")

        viewModel.saveContactInfo(
            name = "Ana Atualizada",
            phone = "99999-9999",
            address = "Rua B, 456",
            emergencyContactName = "Contato",
            emergencyContact = "88888-8888",
            paymentDay = 15,
            modalityIds = listOf("mod1"),
            notes = "Observação",
            birthDate = "01/01/1990",
            registrationDate = 2000L
        )

        assertEquals(SaveState.Success, viewModel.saveState.value)
        val updated = (viewModel.uiState.value as StudentProfileUiState.Success).student
        assertEquals("Ana Atualizada", updated.name)
        assertEquals(15, updated.paymentDay)
    }

    @Test
    fun `saveContactInfo emite Error quando update falha`() = runTest {
        val student = Student(id = "s1", name = "Ana")
        coEvery { studentRepository.getById("s1") } returns Result.success(student)
        every { modalityRepository.observeAll() } returns flowOf(emptyList())
        coEvery { studentRepository.update(any()) } returns Result.failure(Exception("Falha ao salvar"))
        viewModel.loadStudent("s1")

        viewModel.saveContactInfo("Ana", "99999", "Rua A", "Contato", "99998", 10, listOf(), "", "01/01/1990", 1000L)

        assertEquals(SaveState.Error("Falha ao salvar"), viewModel.saveState.value)
    }

    @Test
    fun `saveContactInfo nao faz nada se uiState nao e Success`() = runTest {
        viewModel.saveContactInfo("Ana", "99999", "Rua A", "Contato", "99998", 10, listOf(), "", "01/01/1990", 1000L)

        assertEquals(SaveState.Idle, viewModel.saveState.value)
    }

    @Test
    fun `toggleActive desativa aluno ativo`() = runTest {
        val student = Student(id = "s1", name = "Ana", active = true)
        coEvery { studentRepository.getById("s1") } returns Result.success(student)
        every { modalityRepository.observeAll() } returns flowOf(emptyList())
        coEvery { studentRepository.update(any()) } returns Result.success(Unit)
        viewModel.loadStudent("s1")

        viewModel.toggleActive(false)

        val updated = (viewModel.uiState.value as StudentProfileUiState.Success).student
        assertEquals(false, updated.active)
    }

    @Test
    fun `toggleActive ativa aluno inativo`() = runTest {
        val student = Student(id = "s1", name = "Ana", active = false)
        coEvery { studentRepository.getById("s1") } returns Result.success(student)
        every { modalityRepository.observeAll() } returns flowOf(emptyList())
        coEvery { studentRepository.update(any()) } returns Result.success(Unit)
        viewModel.loadStudent("s1")

        viewModel.toggleActive(true)

        val updated = (viewModel.uiState.value as StudentProfileUiState.Success).student
        assertEquals(true, updated.active)
    }

    @Test
    fun `resetSaveState volta para Idle`() = runTest {
        val student = Student(id = "s1", name = "Ana")
        coEvery { studentRepository.getById("s1") } returns Result.success(student)
        every { modalityRepository.observeAll() } returns flowOf(emptyList())
        coEvery { studentRepository.update(any()) } returns Result.success(Unit)
        viewModel.loadStudent("s1")
        viewModel.saveContactInfo("Ana", "99999", "Rua A", "Contato", "99998", 10, listOf(), "", "01/01/1990", 1000L)

        viewModel.resetSaveState()

        assertEquals(SaveState.Idle, viewModel.saveState.value)
    }
}

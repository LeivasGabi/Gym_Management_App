package gym.management.presentation.register.student

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import gym.management.domain.model.Modality
import gym.management.domain.model.Student
import gym.management.domain.repository.ModalityRepository
import gym.management.domain.repository.StudentRepository
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
class RegisterStudentViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var studentRepository: StudentRepository
    private lateinit var modalityRepository: ModalityRepository
    private lateinit var viewModel: RegisterStudentViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(FirebaseAuth::class)
        val mockAuth = mockk<FirebaseAuth>()
        val mockUser = mockk<FirebaseUser>()
        every { FirebaseAuth.getInstance() } returns mockAuth
        every { mockAuth.currentUser } returns mockUser
        every { mockUser.uid } returns "test-uid"

        studentRepository = mockk()
        modalityRepository = mockk()
        every { modalityRepository.observeAll() } returns flowOf(emptyList())
        viewModel = RegisterStudentViewModel(studentRepository, modalityRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(FirebaseAuth::class)
    }

    @Test
    fun `estado inicial e Idle`() {
        assertEquals(RegisterStudentUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `modalidades sao carregadas do repositorio`() = runTest {
        val modalities = listOf(
            Modality(id = "1", name = "Jiu-Jitsu"),
            Modality(id = "2", name = "Boxe")
        )
        every { modalityRepository.observeAll() } returns flowOf(modalities)
        val vm = RegisterStudentViewModel(studentRepository, modalityRepository)

        assertEquals(modalities, vm.modalities.value)
        assertTrue(vm.modalitiesLoaded.value)
    }

    @Test
    fun `modalitiesLoaded e false antes de emitir`() {
        every { modalityRepository.observeAll() } returns flowOf()
        val vm = RegisterStudentViewModel(studentRepository, modalityRepository)

        assertEquals(false, vm.modalitiesLoaded.value)
    }

    @Test
    fun `saveStudent emite Success ao salvar com sucesso`() = runTest {
        coEvery { studentRepository.save(any()) } returns Result.success(Student(id = "new"))

        viewModel.saveStudent(
            name = "Ana",
            phone = "99999-9999",
            address = "Rua A, 123",
            birthDate = "01/01/1990",
            emergencyContactName = "Maria",
            emergencyContact = "88888-8888",
            paymentDay = 10,
            modalityIds = listOf("mod1"),
            registrationDate = 1000L
        )

        assertEquals(RegisterStudentUiState.Success, viewModel.uiState.value)
    }

    @Test
    fun `saveStudent emite Error quando repositorio falha`() = runTest {
        coEvery { studentRepository.save(any()) } returns Result.failure(Exception("Erro ao salvar aluno"))

        viewModel.saveStudent("Ana", "99999", "Rua A", "01/01/1990", "Maria", "99998", 10, listOf(), 1000L)

        assertEquals(RegisterStudentUiState.Error("Erro ao salvar aluno"), viewModel.uiState.value)
    }

    @Test
    fun `saveStudent usa userId do FirebaseAuth`() = runTest {
        var savedStudent: Student? = null
        coEvery { studentRepository.save(any()) } answers {
            savedStudent = firstArg()
            Result.success(firstArg())
        }

        viewModel.saveStudent("Ana", "99999", "Rua A", "01/01/1990", "Maria", "99998", 10, listOf(), 1000L)

        assertEquals("test-uid", savedStudent?.userId)
    }

    @Test
    fun `resetState volta para Idle`() = runTest {
        coEvery { studentRepository.save(any()) } returns Result.failure(Exception("Erro"))
        viewModel.saveStudent("Ana", "99999", "Rua A", "01/01/1990", "Maria", "99998", 10, listOf(), 1000L)

        viewModel.resetState()

        assertEquals(RegisterStudentUiState.Idle, viewModel.uiState.value)
    }
}

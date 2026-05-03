package gym.management.presentation.payment

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import gym.management.domain.model.Modality
import gym.management.domain.model.Payment
import gym.management.domain.model.Student
import gym.management.domain.repository.ModalityRepository
import gym.management.domain.repository.PaymentRepository
import gym.management.domain.repository.StudentRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PaymentViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var studentRepository: StudentRepository
    private lateinit var modalityRepository: ModalityRepository
    private lateinit var paymentRepository: PaymentRepository
    private lateinit var mockAuth: FirebaseAuth
    private lateinit var viewModel: PaymentViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        studentRepository = mockk()
        modalityRepository = mockk()
        paymentRepository = mockk()
        mockAuth = mockk()

        every { studentRepository.observeAll() } returns flowOf(emptyList())
        every { modalityRepository.observeAll() } returns flowOf(emptyList())
        every { paymentRepository.observeByMonth(any(), any()) } returns flowOf(emptyList())

        viewModel = PaymentViewModel(studentRepository, modalityRepository, paymentRepository, mockAuth)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `estado inicial e YearPicker com anos disponiveis`() = runTest {
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is PaymentScreenState.YearPicker)
        val years = (state as PaymentScreenState.YearPicker).years
        assertTrue(years.contains(PaymentViewModel.START_YEAR))
        assertTrue(years.isNotEmpty())
        job.cancel()
    }

    @Test
    fun `selectYear transiciona para MonthPicker`() = runTest {
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.selectYear(2026)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is PaymentScreenState.MonthPicker)
        assertEquals(2026, (state as PaymentScreenState.MonthPicker).year)
        job.cancel()
    }

    @Test
    fun `meses disponiveis para ano inicial partem de abril`() = runTest {
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.selectYear(PaymentViewModel.START_YEAR)
        advanceUntilIdle()

        val state = viewModel.uiState.value as PaymentScreenState.MonthPicker
        assertEquals(PaymentViewModel.START_MONTH, state.months.first())
        assertEquals(12, state.months.last())
        job.cancel()
    }

    @Test
    fun `meses disponiveis para anos posteriores vao de 1 a 12`() = runTest {
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.selectYear(PaymentViewModel.START_YEAR + 1)
        advanceUntilIdle()

        val state = viewModel.uiState.value as PaymentScreenState.MonthPicker
        assertEquals(1, state.months.first())
        assertEquals(12, state.months.last())
        assertEquals(12, state.months.size)
        job.cancel()
    }

    @Test
    fun `selectYear seguido de selectMonth transiciona para Detail`() = runTest {
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.selectYear(2026)
        viewModel.selectMonth(5)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is PaymentScreenState.Detail)
        val detail = state as PaymentScreenState.Detail
        assertEquals(2026, detail.year)
        assertEquals(5, detail.month)
        job.cancel()
    }

    @Test
    fun `goBack a partir de mes limpa mes e retorna true`() = runTest {
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        viewModel.selectYear(2026)
        viewModel.selectMonth(5)
        advanceUntilIdle()

        val result = viewModel.goBack()
        advanceUntilIdle()

        assertTrue(result)
        assertTrue(viewModel.uiState.value is PaymentScreenState.MonthPicker)
        job.cancel()
    }

    @Test
    fun `goBack a partir de ano limpa ano e retorna true`() = runTest {
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        viewModel.selectYear(2026)
        advanceUntilIdle()

        val result = viewModel.goBack()
        advanceUntilIdle()

        assertTrue(result)
        assertTrue(viewModel.uiState.value is PaymentScreenState.YearPicker)
        job.cancel()
    }

    @Test
    fun `goBack na tela inicial retorna false`() {
        val result = viewModel.goBack()
        assertFalse(result)
    }

    @Test
    fun `selectYear limpa mes selecionado anteriormente`() = runTest {
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()
        viewModel.selectYear(2026)
        viewModel.selectMonth(5)
        advanceUntilIdle()

        viewModel.selectYear(2027)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is PaymentScreenState.MonthPicker)
        assertEquals(2027, (viewModel.uiState.value as PaymentScreenState.MonthPicker).year)
        job.cancel()
    }

    @Test
    fun `Detail com alunos ativos e modalidades ativas calcula totais`() = runTest {
        val modality = Modality(id = "mod1", name = "Jiu-Jitsu", price = 200.0, active = true)
        val student = Student(id = "s1", name = "Ana", active = true, modalityIds = listOf("mod1"))
        val payment = Payment(id = "p1", studentId = "s1", modalityId = "mod1", amount = 200.0)

        every { modalityRepository.observeAll() } returns flowOf(listOf(modality))
        every { studentRepository.observeAll() } returns flowOf(listOf(student))
        every { paymentRepository.observeByMonth(2026, 5) } returns flowOf(listOf(payment))

        val vm = PaymentViewModel(studentRepository, modalityRepository, paymentRepository, mockAuth)
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()

        vm.selectYear(2026)
        vm.selectMonth(5)
        advanceUntilIdle()

        val state = vm.uiState.value as PaymentScreenState.Detail
        assertEquals(200.0, state.totalPaid, 0.001)
        assertEquals(200.0, state.totalExpected, 0.001)
        job.cancel()
    }

    @Test
    fun `Detail exclui alunos inativos do calculo`() = runTest {
        val modality = Modality(id = "mod1", name = "Jiu-Jitsu", price = 200.0, active = true)
        val students = listOf(
            Student(id = "s1", name = "Ana", active = true, modalityIds = listOf("mod1")),
            Student(id = "s2", name = "Pedro", active = false, modalityIds = listOf("mod1"))
        )
        every { modalityRepository.observeAll() } returns flowOf(listOf(modality))
        every { studentRepository.observeAll() } returns flowOf(students)
        every { paymentRepository.observeByMonth(2026, 5) } returns flowOf(emptyList())

        val vm = PaymentViewModel(studentRepository, modalityRepository, paymentRepository, mockAuth)
        val job = launch { vm.uiState.collect {} }
        advanceUntilIdle()
        vm.selectYear(2026)
        vm.selectMonth(5)
        advanceUntilIdle()

        val state = vm.uiState.value as PaymentScreenState.Detail
        assertEquals(1, state.groups.sumOf { it.students.size })
        assertEquals(200.0, state.totalExpected, 0.001)
        job.cancel()
    }
}

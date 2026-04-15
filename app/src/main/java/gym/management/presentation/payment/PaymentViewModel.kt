package gym.management.presentation.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import gym.management.data.repository.ModalityRepositoryImpl
import gym.management.data.repository.PaymentRepositoryImpl
import gym.management.data.repository.StudentRepositoryImpl
import gym.management.domain.model.Payment
import gym.management.domain.repository.ModalityRepository
import gym.management.domain.repository.PaymentRepository
import gym.management.domain.repository.StudentRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class PaymentViewModel(
    private val studentRepository: StudentRepository,
    private val modalityRepository: ModalityRepository,
    private val paymentRepository: PaymentRepository,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    companion object {
        const val START_YEAR = 2026
        const val START_MONTH = 4 // Abril

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                PaymentViewModel(
                    studentRepository = StudentRepositoryImpl(),
                    modalityRepository = ModalityRepositoryImpl(),
                    paymentRepository = PaymentRepositoryImpl()
                ) as T
        }
    }

    private val _selectedYear = MutableStateFlow<Int?>(null)
    private val _selectedMonth = MutableStateFlow<Int?>(null)
    private val _collapsedModalityIds = MutableStateFlow<Set<String>>(emptySet())

    val uiState: StateFlow<PaymentScreenState> = combine(
        _selectedYear,
        _selectedMonth,
        _collapsedModalityIds
    ) { year, month, collapsed ->
        Triple(year, month, collapsed)
    }.flatMapLatest { (year, month, collapsed) ->
        when {
            year == null -> flowOf(PaymentScreenState.YearPicker(availableYears()))

            month == null -> flowOf(PaymentScreenState.MonthPicker(year, availableMonths(year)))

            else -> combine(
                modalityRepository.observeAll(),
                studentRepository.observeAll(),
                paymentRepository.observeByMonth(year, month)
            ) { modalities, students, payments ->
                val activeStudents = students.filter { it.active }
                val groups = modalities.mapNotNull { modality ->
                    val enrolled = activeStudents.filter { modality.id in it.modalityIds }
                    if (enrolled.isEmpty()) return@mapNotNull null
                    ModalityPaymentGroup(
                        modality = modality,
                        isExpanded = modality.id !in collapsed,
                        students = enrolled.map { student ->
                            StudentPaymentItem(
                                student = student,
                                modality = modality,
                                payment = payments.find {
                                    it.studentId == student.id && it.modalityId == modality.id
                                }
                            )
                        }
                    )
                }
                val totalPaid = groups.flatMap { it.students }
                    .filter { it.isPaid }
                    .sumOf { it.payment!!.amount }

                PaymentScreenState.Detail(
                    year = year,
                    month = month,
                    groups = groups,
                    totalPaid = totalPaid
                )
            }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        PaymentScreenState.YearPicker(availableYears())
    )

    fun selectYear(year: Int) {
        _selectedYear.value = year
        _selectedMonth.value = null
    }

    fun selectMonth(month: Int) {
        _selectedMonth.value = month
    }

    fun toggleModality(modalityId: String) {
        _collapsedModalityIds.update { current ->
            if (modalityId in current) current - modalityId else current + modalityId
        }
    }

    fun togglePayment(item: StudentPaymentItem) {
        viewModelScope.launch {
            val year = _selectedYear.value ?: return@launch
            val month = _selectedMonth.value ?: return@launch
            if (item.isPaid) {
                item.payment?.let { paymentRepository.delete(it.id) }
            } else {
                val userId = auth.currentUser?.uid ?: return@launch
                paymentRepository.save(
                    Payment(
                        userId = userId,
                        studentId = item.student.id,
                        studentName = item.student.name,
                        modalityId = item.modality.id,
                        modalityName = item.modality.name,
                        amount = item.modality.price,
                        year = year,
                        month = month,
                        paymentDay = item.student.paymentDay,
                        isPaid = true,
                        paidAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    // Returns true if handled internally (go back one level), false if should pop fragment
    fun goBack(): Boolean = when {
        _selectedMonth.value != null -> { _selectedMonth.value = null; true }
        _selectedYear.value != null -> { _selectedYear.value = null; true }
        else -> false
    }

    private fun availableYears(): List<Int> {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        return (START_YEAR..(currentYear + 1)).toList()
    }

    private fun availableMonths(year: Int): List<Int> {
        return if (year == START_YEAR) (START_MONTH..12).toList()
        else (1..12).toList()
    }
}

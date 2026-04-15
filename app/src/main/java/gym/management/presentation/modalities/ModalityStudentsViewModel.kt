package gym.management.presentation.modalities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import gym.management.data.repository.GraduationRepositoryImpl
import gym.management.data.repository.StudentRepositoryImpl
import gym.management.domain.repository.GraduationRepository
import gym.management.domain.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ModalityStudentsViewModel(
    private val modalityId: String,
    private val studentRepository: StudentRepository,
    private val graduationRepository: GraduationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ModalityStudentsUiState>(ModalityStudentsUiState.Loading)
    val uiState: StateFlow<ModalityStudentsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                combine(
                    studentRepository.observeAll(),
                    graduationRepository.observeByModality(modalityId)
                ) { students, graduations ->
                    val filtered = students.filter { it.active && modalityId in it.modalityIds }
                    filtered.map { student ->
                        val latestBelt = graduations
                            .filter { it.studentId == student.id }
                            .maxByOrNull { it.date }
                            ?.belt
                        StudentWithLatestBelt(student = student, latestBelt = latestBelt)
                    }
                }.collect { items ->
                    _uiState.value = ModalityStudentsUiState.Success(items)
                }
            } catch (e: Exception) {
                _uiState.value = ModalityStudentsUiState.Error(e.message ?: "Erro ao carregar alunos")
            }
        }
    }

    companion object {
        fun factory(modalityId: String): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ModalityStudentsViewModel(
                    modalityId,
                    StudentRepositoryImpl(),
                    GraduationRepositoryImpl()
                ) as T
        }
    }
}

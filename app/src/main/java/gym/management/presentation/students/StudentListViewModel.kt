package gym.management.presentation.students

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import gym.management.data.repository.GraduationRepositoryImpl
import gym.management.data.repository.StudentRepositoryImpl
import gym.management.domain.repository.GraduationRepository
import gym.management.domain.repository.StudentRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class StudentListViewModel(
    private val studentRepository: StudentRepository,
    private val graduationRepository: GraduationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<StudentListUiState>(StudentListUiState.Loading)
    val uiState: StateFlow<StudentListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            studentRepository.observeAll()
                .flatMapLatest { students ->
                    val ids = students.map { it.id }
                    graduationRepository.observeByStudentIds(ids)
                        .combine(kotlinx.coroutines.flow.flowOf(students)) { graduations, s -> s to graduations }
                }
                .collect { (students, graduations) ->
                    val items = students.map { student ->
                        val latestBelt = graduations
                            .filter { it.studentId == student.id }
                            .maxByOrNull { it.date }
                            ?.belt
                        StudentListItem(student = student, latestBelt = latestBelt)
                    }
                    _uiState.value = StudentListUiState.Success(items)
                }
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                StudentListViewModel(StudentRepositoryImpl(), GraduationRepositoryImpl()) as T
        }
    }
}

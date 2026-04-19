package gym.management.presentation.students

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import gym.management.data.repository.ModalityRepositoryImpl
import gym.management.data.repository.StudentRepositoryImpl
import gym.management.domain.repository.ModalityRepository
import gym.management.domain.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class StudentListViewModel(
    private val studentRepository: StudentRepository,
    private val modalityRepository: ModalityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<StudentListUiState>(StudentListUiState.Loading)
    val uiState: StateFlow<StudentListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                studentRepository.observeAll(),
                modalityRepository.observeAll()
            ) { students, modalities ->
                val modalityMap = modalities.associateBy { it.id }
                students.map { student ->
                    val names = student.modalityIds.mapNotNull { modalityMap[it]?.name }
                    StudentListItem(student = student, modalityNames = names)
                }
            }.collect { items ->
                _uiState.value = StudentListUiState.Success(items)
            }
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                StudentListViewModel(StudentRepositoryImpl(), ModalityRepositoryImpl()) as T
        }
    }
}

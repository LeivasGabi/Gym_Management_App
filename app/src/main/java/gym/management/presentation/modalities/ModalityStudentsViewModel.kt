package gym.management.presentation.modalities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import gym.management.data.repository.StudentRepositoryImpl
import gym.management.domain.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ModalityStudentsViewModel(
    private val modalityId: String,
    private val studentRepository: StudentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ModalityStudentsUiState>(ModalityStudentsUiState.Loading)
    val uiState: StateFlow<ModalityStudentsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            studentRepository.observeAll().collect { students ->
                val filtered = students.filter { it.active && modalityId in it.modalityIds }
                _uiState.value = ModalityStudentsUiState.Success(filtered)
            }
        }
    }

    companion object {
        fun factory(modalityId: String): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ModalityStudentsViewModel(modalityId, StudentRepositoryImpl()) as T
        }
    }
}

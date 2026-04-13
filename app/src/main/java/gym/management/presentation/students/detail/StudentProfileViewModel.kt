package gym.management.presentation.students.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import gym.management.data.repository.StudentRepositoryImpl
import gym.management.domain.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SaveState {
    object Idle : SaveState()
    object Loading : SaveState()
    object Success : SaveState()
    data class Error(val message: String) : SaveState()
}

class StudentProfileViewModel(
    private val studentRepository: StudentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<StudentProfileUiState>(StudentProfileUiState.Loading)
    val uiState: StateFlow<StudentProfileUiState> = _uiState.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    fun loadStudent(studentId: String) {
        viewModelScope.launch {
            _uiState.value = StudentProfileUiState.Loading
            _uiState.value = studentRepository.getById(studentId).fold(
                onSuccess = { StudentProfileUiState.Success(it) },
                onFailure = { StudentProfileUiState.Error(it.message ?: "Erro ao carregar aluno") }
            )
        }
    }

    fun saveContactInfo(phone: String, address: String, emergencyContact: String) {
        val current = (_uiState.value as? StudentProfileUiState.Success)?.student ?: return
        viewModelScope.launch {
            _saveState.value = SaveState.Loading
            val updated = current.copy(
                phone = phone,
                address = address,
                emergencyContact = emergencyContact
            )
            _saveState.value = studentRepository.update(updated).fold(
                onSuccess = {
                    _uiState.value = StudentProfileUiState.Success(updated)
                    SaveState.Success
                },
                onFailure = { SaveState.Error(it.message ?: "Erro ao salvar") }
            )
        }
    }

    fun toggleActive(active: Boolean) {
        val current = (_uiState.value as? StudentProfileUiState.Success)?.student ?: return
        viewModelScope.launch {
            studentRepository.update(current.copy(active = active))
            _uiState.value = StudentProfileUiState.Success(current.copy(active = active))
        }
    }

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                StudentProfileViewModel(StudentRepositoryImpl()) as T
        }
    }
}

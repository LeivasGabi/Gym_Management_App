package gym.management.presentation.students.detail

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

sealed class SaveState {
    object Idle : SaveState()
    object Loading : SaveState()
    object Success : SaveState()
    data class Error(val message: String) : SaveState()
}

class StudentProfileViewModel(
    private val studentRepository: StudentRepository,
    private val modalityRepository: ModalityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<StudentProfileUiState>(StudentProfileUiState.Loading)
    val uiState: StateFlow<StudentProfileUiState> = _uiState.asStateFlow()

    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    fun loadStudent(studentId: String) {
        viewModelScope.launch {
            val studentResult = studentRepository.getById(studentId)
            if (studentResult.isFailure) {
                _uiState.value = StudentProfileUiState.Error(
                    studentResult.exceptionOrNull()?.message ?: "Erro ao carregar aluno"
                )
                return@launch
            }
            val student = studentResult.getOrThrow()
            modalityRepository.observeAll().collect { modalities ->
                val current = _uiState.value
                _uiState.value = StudentProfileUiState.Success(
                    student = if (current is StudentProfileUiState.Success) current.student else student,
                    availableModalities = modalities
                )
            }
        }
    }

    fun saveContactInfo(
        name: String,
        phone: String,
        address: String,
        emergencyContactName: String,
        emergencyContact: String,
        paymentDay: Int,
        modalityIds: List<String>,
        notes: String,
        birthDate: String,
        registrationDate: Long
    ) {
        val current = (_uiState.value as? StudentProfileUiState.Success)?.student ?: return
        viewModelScope.launch {
            _saveState.value = SaveState.Loading
            val updated = current.copy(
                name = name,
                phone = phone,
                address = address,
                emergencyContactName = emergencyContactName,
                emergencyContact = emergencyContact,
                paymentDay = paymentDay,
                modalityIds = modalityIds,
                notes = notes,
                birthDate = birthDate,
                registrationDate = registrationDate
            )
            _saveState.value = studentRepository.update(updated).fold(
                onSuccess = {
                    _uiState.value = (_uiState.value as? StudentProfileUiState.Success)
                        ?.copy(student = updated) ?: StudentProfileUiState.Success(updated)
                    SaveState.Success
                },
                onFailure = { SaveState.Error(it.message ?: "Erro ao salvar") }
            )
        }
    }

    fun toggleActive(active: Boolean) {
        val current = (_uiState.value as? StudentProfileUiState.Success)?.student ?: return
        viewModelScope.launch {
            val updated = current.copy(active = active)
            studentRepository.update(updated)
            _uiState.value = (_uiState.value as? StudentProfileUiState.Success)
                ?.copy(student = updated) ?: StudentProfileUiState.Success(updated)
        }
    }

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                StudentProfileViewModel(StudentRepositoryImpl(), ModalityRepositoryImpl()) as T
        }
    }
}

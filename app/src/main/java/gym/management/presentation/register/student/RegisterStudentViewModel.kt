package gym.management.presentation.register.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import gym.management.domain.model.Modality
import gym.management.domain.model.Student
import gym.management.domain.repository.ModalityRepository
import gym.management.domain.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterStudentViewModel(
    private val studentRepository: StudentRepository,
    private val modalityRepository: ModalityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterStudentUiState>(RegisterStudentUiState.Idle)
    val uiState: StateFlow<RegisterStudentUiState> = _uiState.asStateFlow()

    private val _modalities = MutableStateFlow<List<Modality>>(emptyList())
    val modalities: StateFlow<List<Modality>> = _modalities.asStateFlow()

    private val _modalitiesLoaded = MutableStateFlow(false)
    val modalitiesLoaded: StateFlow<Boolean> = _modalitiesLoaded.asStateFlow()

    init {
        viewModelScope.launch {
            modalityRepository.observeAll().collect {
                _modalities.value = it
                _modalitiesLoaded.value = true
            }
        }
    }

    fun saveStudent(
        name: String,
        phone: String,
        address: String,
        birthDate: String,
        emergencyContactName: String,
        emergencyContact: String,
        paymentDay: Int,
        modalityIds: List<String>,
        registrationDate: Long
    ) {
        viewModelScope.launch {
            _uiState.value = RegisterStudentUiState.Loading
            val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
            val student = Student(
                userId = userId,
                name = name,
                phone = phone,
                address = address,
                birthDate = birthDate,
                emergencyContactName = emergencyContactName,
                emergencyContact = emergencyContact,
                paymentDay = paymentDay,
                modalityIds = modalityIds,
                registrationDate = registrationDate
            )
            _uiState.value = studentRepository.save(student).fold(
                onSuccess = { RegisterStudentUiState.Success },
                onFailure = { RegisterStudentUiState.Error(it.message ?: "Erro ao salvar aluno") }
            )
        }
    }

    fun resetState() {
        _uiState.value = RegisterStudentUiState.Idle
    }

    companion object {
        fun factory(
            studentRepository: StudentRepository,
            modalityRepository: ModalityRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                RegisterStudentViewModel(studentRepository, modalityRepository) as T
        }
    }
}

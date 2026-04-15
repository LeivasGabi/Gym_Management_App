package gym.management.presentation.graduation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import gym.management.data.repository.GraduationRepositoryImpl
import gym.management.data.repository.ModalityRepositoryImpl
import gym.management.data.repository.StudentRepositoryImpl
import gym.management.domain.model.Graduation
import gym.management.domain.repository.GraduationRepository
import gym.management.domain.repository.ModalityRepository
import gym.management.domain.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class GraduationViewModel(
    private val graduationRepository: GraduationRepository,
    private val studentRepository: StudentRepository,
    private val modalityRepository: ModalityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<GraduationUiState>(GraduationUiState.Loading)
    val uiState: StateFlow<GraduationUiState> = _uiState.asStateFlow()

    private val _saveState = MutableStateFlow<GraduationSaveState>(GraduationSaveState.Idle)
    val saveState: StateFlow<GraduationSaveState> = _saveState.asStateFlow()

    fun load(studentId: String) {
        viewModelScope.launch {
            val studentResult = studentRepository.getById(studentId)
            if (studentResult.isFailure) {
                _uiState.value = GraduationUiState.Error(
                    studentResult.exceptionOrNull()?.message ?: "Erro ao carregar aluno"
                )
                return@launch
            }
            val student = studentResult.getOrThrow()

            try {
                combine(
                    graduationRepository.observeByStudent(studentId),
                    modalityRepository.observeAll()
                ) { graduations, allModalities ->
                    val studentModalities = allModalities.filter { it.id in student.modalityIds }
                    val groups = studentModalities.map { modality ->
                        GraduationGroup(
                            modality = modality,
                            graduations = graduations
                                .filter { it.modalityId == modality.id }
                                .sortedByDescending { it.date }
                        )
                    }
                    GraduationUiState.Success(
                        studentName = student.name,
                        groups = groups,
                        studentModalities = studentModalities
                    )
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.value = GraduationUiState.Error(e.message ?: "Erro ao carregar graduações")
            }
        }
    }

    fun addGraduation(
        studentId: String,
        modalityId: String,
        modalityName: String,
        belt: String,
        generalGrade: String,
        observation: String,
        date: Long
    ) {
        viewModelScope.launch {
            _saveState.value = GraduationSaveState.Loading
            val graduation = Graduation(
                studentId = studentId,
                modalityId = modalityId,
                modalityName = modalityName,
                belt = belt,
                generalGrade = generalGrade,
                observation = observation,
                date = date
            )
            _saveState.value = graduationRepository.save(graduation).fold(
                onSuccess = { GraduationSaveState.Success },
                onFailure = { GraduationSaveState.Error(it.message ?: "Erro ao salvar graduação") }
            )
        }
    }

    fun updateGraduation(graduation: Graduation) {
        viewModelScope.launch {
            _saveState.value = GraduationSaveState.Loading
            _saveState.value = graduationRepository.update(graduation).fold(
                onSuccess = { GraduationSaveState.Success },
                onFailure = { GraduationSaveState.Error(it.message ?: "Erro ao atualizar graduação") }
            )
        }
    }

    fun resetSaveState() {
        _saveState.value = GraduationSaveState.Idle
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                GraduationViewModel(
                    GraduationRepositoryImpl(),
                    StudentRepositoryImpl(),
                    ModalityRepositoryImpl()
                ) as T
        }
    }
}

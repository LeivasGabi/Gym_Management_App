package gym.management.presentation.modalities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import gym.management.data.repository.GraduationRepositoryImpl
import gym.management.data.repository.ModalityRepositoryImpl
import gym.management.data.repository.StudentRepositoryImpl
import gym.management.domain.repository.GraduationRepository
import gym.management.domain.repository.ModalityRepository
import gym.management.domain.repository.StudentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ModalityStudentsViewModel(
    private val modalityId: String,
    private val studentRepository: StudentRepository,
    private val graduationRepository: GraduationRepository,
    private val modalityRepository: ModalityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ModalityStudentsUiState>(ModalityStudentsUiState.Loading)
    val uiState: StateFlow<ModalityStudentsUiState> = _uiState.asStateFlow()

    private val _editSaveState = MutableStateFlow<ModalityEditSaveState>(ModalityEditSaveState.Idle)
    val editSaveState: StateFlow<ModalityEditSaveState> = _editSaveState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                combine(
                    modalityRepository.observeAll().map { list -> list.find { it.id == modalityId } },
                    studentRepository.observeAll(),
                    graduationRepository.observeByModality(modalityId)
                ) { modality, students, graduations ->
                    if (modality == null) {
                        return@combine ModalityStudentsUiState.Error("Modalidade não encontrada")
                    }
                    val filtered = students.filter { it.active && modalityId in it.modalityIds }
                    val items = filtered.map { student ->
                        val latestBelt = graduations
                            .filter { it.studentId == student.id }
                            .maxByOrNull { it.date }
                            ?.belt
                        StudentWithLatestBelt(student = student, latestBelt = latestBelt)
                    }
                    ModalityStudentsUiState.Success(items, modality)
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.value = ModalityStudentsUiState.Error(e.message ?: "Erro ao carregar alunos")
            }
        }
    }

    fun updateModality(name: String, schedules: List<String>, price: String, frequency: String, active: Boolean) {
        val currentModality = (_uiState.value as? ModalityStudentsUiState.Success)?.modality ?: return
        viewModelScope.launch {
            _editSaveState.value = ModalityEditSaveState.Loading
            val updated = currentModality.copy(
                name = name,
                schedule = "",
                schedules = schedules,
                price = price.replace(",", ".").toDoubleOrNull() ?: 0.0,
                frequency = frequency,
                active = active
            )
            _editSaveState.value = modalityRepository.update(updated).fold(
                onSuccess = { ModalityEditSaveState.Success },
                onFailure = { ModalityEditSaveState.Error(it.message ?: "Erro ao atualizar modalidade") }
            )
        }
    }

    fun resetEditState() {
        _editSaveState.value = ModalityEditSaveState.Idle
    }

    companion object {
        fun factory(modalityId: String): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ModalityStudentsViewModel(
                    modalityId,
                    StudentRepositoryImpl(),
                    GraduationRepositoryImpl(),
                    ModalityRepositoryImpl()
                ) as T
        }
    }
}

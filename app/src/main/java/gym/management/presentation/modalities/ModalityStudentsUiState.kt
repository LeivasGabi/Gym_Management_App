package gym.management.presentation.modalities

import gym.management.domain.model.Student

sealed class ModalityStudentsUiState {
    object Loading : ModalityStudentsUiState()
    data class Success(val students: List<Student>) : ModalityStudentsUiState()
    data class Error(val message: String) : ModalityStudentsUiState()
}

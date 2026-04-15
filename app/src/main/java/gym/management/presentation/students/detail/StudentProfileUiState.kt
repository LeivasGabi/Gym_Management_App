package gym.management.presentation.students.detail

import gym.management.domain.model.Modality
import gym.management.domain.model.Student

sealed class StudentProfileUiState {
    object Loading : StudentProfileUiState()
    data class Success(
        val student: Student,
        val availableModalities: List<Modality> = emptyList()
    ) : StudentProfileUiState()
    data class Error(val message: String) : StudentProfileUiState()
}

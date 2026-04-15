package gym.management.presentation.modalities

import gym.management.domain.model.Student

data class StudentWithLatestBelt(
    val student: Student,
    val latestBelt: String?
)

sealed class ModalityStudentsUiState {
    object Loading : ModalityStudentsUiState()
    data class Success(val students: List<StudentWithLatestBelt>) : ModalityStudentsUiState()
    data class Error(val message: String) : ModalityStudentsUiState()
}

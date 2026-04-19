package gym.management.presentation.modalities

import gym.management.domain.model.Modality
import gym.management.domain.model.Student

data class StudentWithLatestBelt(
    val student: Student,
    val latestBelt: String?
)

sealed class ModalityStudentsUiState {
    object Loading : ModalityStudentsUiState()
    data class Success(
        val students: List<StudentWithLatestBelt>,
        val modality: Modality
    ) : ModalityStudentsUiState()
    data class Error(val message: String) : ModalityStudentsUiState()
}

sealed class ModalityEditSaveState {
    object Idle : ModalityEditSaveState()
    object Loading : ModalityEditSaveState()
    object Success : ModalityEditSaveState()
    data class Error(val message: String) : ModalityEditSaveState()
}

sealed class ModalityDeleteState {
    object Idle : ModalityDeleteState()
    object Loading : ModalityDeleteState()
    object Success : ModalityDeleteState()
    data class Error(val message: String) : ModalityDeleteState()
}

package gym.management.presentation.graduation

import gym.management.domain.model.Graduation
import gym.management.domain.model.Modality

data class GraduationGroup(
    val modality: Modality,
    val graduations: List<Graduation>
)

sealed class GraduationUiState {
    object Loading : GraduationUiState()
    data class Success(
        val studentName: String,
        val groups: List<GraduationGroup>,
        val studentModalities: List<Modality>
    ) : GraduationUiState()
    data class Error(val message: String) : GraduationUiState()
}

sealed class GraduationSaveState {
    object Idle : GraduationSaveState()
    object Loading : GraduationSaveState()
    object Success : GraduationSaveState()
    data class Error(val message: String) : GraduationSaveState()
}

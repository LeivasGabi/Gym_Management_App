package gym.management.presentation.register.modality

sealed class RegisterModalityUiState {
    object Idle : RegisterModalityUiState()
    object Loading : RegisterModalityUiState()
    object Success : RegisterModalityUiState()
    data class Conflict(val conflictingNames: List<String>) : RegisterModalityUiState()
    data class Error(val message: String) : RegisterModalityUiState()
}

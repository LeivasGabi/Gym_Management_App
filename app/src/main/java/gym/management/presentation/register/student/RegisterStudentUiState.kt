package gym.management.presentation.register.student

sealed class RegisterStudentUiState {
    object Idle : RegisterStudentUiState()
    object Loading : RegisterStudentUiState()
    object Success : RegisterStudentUiState()
    data class Error(val message: String) : RegisterStudentUiState()
}

package gym.management.presentation.students

import gym.management.domain.model.Student

sealed class StudentListUiState {
    object Loading : StudentListUiState()
    data class Success(val students: List<Student>) : StudentListUiState()
    data class Error(val message: String) : StudentListUiState()
}

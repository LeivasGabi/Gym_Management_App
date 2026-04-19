package gym.management.presentation.students

import gym.management.domain.model.Student

data class StudentListItem(
    val student: Student,
    val modalityNames: List<String>
)

sealed class StudentListUiState {
    object Loading : StudentListUiState()
    data class Success(val students: List<StudentListItem>) : StudentListUiState()
    data class Error(val message: String) : StudentListUiState()
}

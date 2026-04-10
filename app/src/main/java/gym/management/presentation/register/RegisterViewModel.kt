package gym.management.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import gym.management.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(email: String, password: String, confirmPassword: String) {
        if (password != confirmPassword) {
            _uiState.value = RegisterUiState.Error("As senhas não coincidem")
            return
        }
        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading
            val result = authRepository.register(email, password)
            _uiState.value = result.fold(
                onSuccess = { RegisterUiState.Success },
                onFailure = { RegisterUiState.Error(it.message ?: "Erro ao criar conta") }
            )
        }
    }

    fun resetState() {
        _uiState.value = RegisterUiState.Idle
    }

    companion object {
        fun factory(authRepository: AuthRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    RegisterViewModel(authRepository) as T
            }
    }
}

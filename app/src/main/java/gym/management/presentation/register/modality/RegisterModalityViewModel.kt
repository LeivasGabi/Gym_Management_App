package gym.management.presentation.register.modality

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import gym.management.domain.model.Modality
import gym.management.domain.repository.ModalityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterModalityViewModel(
    private val modalityRepository: ModalityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterModalityUiState>(RegisterModalityUiState.Idle)
    val uiState: StateFlow<RegisterModalityUiState> = _uiState.asStateFlow()

    fun saveModality(name: String, schedule: String, price: String, frequency: String) {
        viewModelScope.launch {
            _uiState.value = RegisterModalityUiState.Loading
            val userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
            val modality = Modality(
                userId = userId,
                name = name,
                schedule = schedule,
                price = price.replace(",", ".").toDoubleOrNull() ?: 0.0,
                frequency = frequency
            )
            _uiState.value = modalityRepository.save(modality).fold(
                onSuccess = { RegisterModalityUiState.Success },
                onFailure = { RegisterModalityUiState.Error(it.message ?: "Erro ao salvar modalidade") }
            )
        }
    }

    fun resetState() {
        _uiState.value = RegisterModalityUiState.Idle
    }

    companion object {
        fun factory(modalityRepository: ModalityRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    RegisterModalityViewModel(modalityRepository) as T
            }
    }
}

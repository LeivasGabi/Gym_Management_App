package gym.management.presentation.modalities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import gym.management.data.repository.ModalityRepositoryImpl
import gym.management.domain.repository.ModalityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ModalityListViewModel(
    private val modalityRepository: ModalityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ModalityListUiState>(ModalityListUiState.Loading)
    val uiState: StateFlow<ModalityListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            modalityRepository.observeAll().collect { modalities ->
                _uiState.value = ModalityListUiState.Success(modalities)
            }
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                ModalityListViewModel(ModalityRepositoryImpl()) as T
        }
    }
}
